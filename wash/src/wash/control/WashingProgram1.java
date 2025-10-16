package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

import static wash.control.WashingMessage.Order.*;

/**
 * Program 3 for washing machine. This also serves as an example of how washing
 * programs can be structured.
 * 
 * This short program stops all regulation of temperature and water levels,
 * stops the barrel from spinning, and drains the machine of water.
 * 
 * It can be used after an emergency stop (program 0) or a power failure.
 */
public class WashingProgram1 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;
    
    public WashingProgram1(WashingIO io,
                           ActorThread<WashingMessage> temp,
                           ActorThread<WashingMessage> water,
                           ActorThread<WashingMessage> spin) 
    {
        this.io = io;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }
    
    @Override
    public void run() {
        try {
            // Lock the hatch
            io.lock(true);

            water.send(new WashingMessage(this, WATER_FILL));
            WashingMessage ack0 = receive();
            System.out.println("washing program 1 got " + ack0);
            
            temp.send(new WashingMessage(this, TEMP_SET_40));
            WashingMessage ackTemp = receive();
             System.out.println("washing program 1 got " + ackTemp);
          

            for(int i = 0; i < 30; i++){
                spin.send(new WashingMessage(this, SPIN_SLOW));
                WashingMessage ack1 = receive();
                System.out.println("washing program 1 got " + ack1);
                Thread.sleep(1 * 60000 / Settings.SPEEDUP);
            }
            
            temp.send(new WashingMessage(this, TEMP_IDLE));
            WashingMessage ackTempIdle = receive();
            System.out.println("washing program 1 got " + ackTempIdle);

            spin.send(new WashingMessage(this, SPIN_OFF));
            WashingMessage ack2 = receive();
            System.out.println("washing program 1 got " + ack2);


            water.send(new WashingMessage(this, WATER_DRAIN)); 
            WashingMessage ackDrain = receive();
            System.out.println("washing program 1 got " + ackDrain);

            for(int i = 0; i < 5; i++){
                water.send(new WashingMessage(this, WATER_FILL)); receive();
                spin.send(new WashingMessage(this, SPIN_SLOW)); receive();

                Thread.sleep(2 * 60000 / Settings.SPEEDUP);
                spin.send(new WashingMessage(this, SPIN_OFF)); receive();
                water.send(new WashingMessage(this, WATER_DRAIN)); receive();
            }

            spin.send(new WashingMessage(this, SPIN_FAST)); receive();
            Thread.sleep(5 * 60000 / Settings.SPEEDUP);

            io.lock(false);

        } catch (InterruptedException e) {
            System.out.println("Washing program 1 interrupted");
        }
    }
}
