package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {
    private WashingIO io;
    int preferredTemp = 0;
    boolean firstCycle = true;
    ActorThread<WashingMessage> sender;
    boolean heatOn = false;

    int dt = 10;
    double mu = dt*0.0478;
    double ml = dt*0.00952;



    public TemperatureController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            System.out.println("TemperatureController started");
            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);

                // if m is null, it means a minute passed and no message was received
                if (m != null) {
                    System.out.println("TemperatureController: got " + m);

                    switch (m.order()) {
                        case TEMP_SET_40:
                            preferredTemp = 40;
                            sender = m.sender();
                            firstCycle = true;
                            heatOn = true;
                            break;
                        case TEMP_SET_60:
                            preferredTemp = 60;
                            sender = m.sender();
                            firstCycle = true;
                            heatOn = true;
                            break;
                        case TEMP_IDLE:
                            preferredTemp = 0;
                            sender= m.sender(); 
                            heatOn = false;
                            break;
                    }
                }
                if(heatOn){
                    double T = io.getTemperature();

                    // Stäng av om vi riskerar att bli för varma
                    if (T > preferredTemp - mu ) {
                        io.heat(false);
                    
                    }else if (T < preferredTemp - 2.1 + ml ) {
                        io.heat(true);
                        
                    }
                    if(io.getTemperature() >= preferredTemp-2 && firstCycle){
                        firstCycle = false;
                        
                        sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                    
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
