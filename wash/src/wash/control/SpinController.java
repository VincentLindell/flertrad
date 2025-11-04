package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.io.WashingIO.Spin;


public class SpinController extends ActorThread<WashingMessage> {

    private WashingIO io;
    private Spin currentMode = Spin.IDLE;
    

    public SpinController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        

        try {


            while (true) {
            
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);

               
                if (m != null) {
                    System.out.println("SpinningController: got " + m);

                    switch (m.order()) {
                        case SPIN_OFF:
                            io.setSpinMode(Spin.IDLE);
                            m.sender().send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                            break;
                        case SPIN_SLOW:
                            if(currentMode == Spin.LEFT) {
                                currentMode = Spin.RIGHT;
                            } else {
                                currentMode = Spin.LEFT;
                            }
                            io.setSpinMode(currentMode);
                            m.sender().send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                            break;
                        case SPIN_FAST:
                            io.drain(true);
                            io.setSpinMode(Spin.FAST);
                            m.sender().send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT)); 
                            break;
                    }
                }

            
            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}
