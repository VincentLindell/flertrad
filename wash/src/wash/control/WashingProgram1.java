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
            System.out.println("washing program 1 got " + receive());
            //System.out.println("washing program 1 got " + receive());
            
            temp.send(new WashingMessage(this, TEMP_SET_40));
            System.out.println("washing program 1 got " + receive());

            for(int i = 0; i < 30; i++){
                System.out.println(io.getWaterLevel());
                spin.send(new WashingMessage(this, SPIN_SLOW));
                
                System.out.println("washing program 1 got " + receive());
                Thread.sleep(1 * 60000 / Settings.SPEEDUP);
       
            }
            System.out.println("main wash done");
 
            temp.send(new WashingMessage(this, TEMP_IDLE));
            System.out.println("washing program 1 got " + receive());

            spin.send(new WashingMessage(this, SPIN_OFF));
            System.out.println("washing program 1 got " + receive());


            water.send(new WashingMessage(this, WATER_DRAIN)); 
            System.out.println("washing program 1 got " + receive());
            //System.out.println("washing program 1 got " + receive());

            for(int i = 0; i < 5; i++){
                System.out.println(io.getWaterLevel());
                water.send(new WashingMessage(this, WATER_FILL)); 
                System.out.println("washing program 1 got " + receive());
                //System.out.println("washing program 1 got " + receive());
                spin.send(new WashingMessage(this, SPIN_SLOW)); 
                System.out.println("washing program 1 got " + receive());

                Thread.sleep(2 * 60000 / Settings.SPEEDUP);
                spin.send(new WashingMessage(this, SPIN_OFF)); 
                System.out.println("washing program 1 got " + receive());
                water.send(new WashingMessage(this, WATER_DRAIN)); 
                System.out.println("washing program 1 got " + receive());
                //System.out.println("washing program 1 got " + receive());
            }

            io.drain(true);
            spin.send(new WashingMessage(this, SPIN_FAST)); 
            System.out.println("washing program 1 got " + receive());
            Thread.sleep(5 * 60000 / Settings.SPEEDUP);
            spin.send(new WashingMessage(this, SPIN_OFF)); 
            System.out.println("washing program 1 got " + receive());
            io.drain(false);
            io.lock(false);
            this.interrupt();
            

        } catch (InterruptedException e) {
            System.out.println("Washing program 1 interrupted");
        }
    }
}
