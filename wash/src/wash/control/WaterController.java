package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WaterController extends ActorThread<WashingMessage> {

    private WashingIO io;
    int preferredLevel = 0;
    String action = "none";

    ActorThread<WashingMessage> sender;
    ActorThread<WashingMessage> origin;

    public WaterController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            while(true){
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);
                
                if(m != null){
                   
                    sender = m.sender();
                    System.out.println("WaterController: got " + m);

                    switch (m.order()) {
                        case WATER_FILL:
                            preferredLevel = 10;
                            action = "filling";
                            io.fill(true);
                            io.drain(false);
                            origin = m.sender();
                            
                            break;
                        case WATER_DRAIN:
                            preferredLevel = 0;
                            action = "draining";
                            io.fill(false);
                            io.drain(true);
                            origin = m.sender();
                            break;
                        case WATER_IDLE:
                            action = "idle";
                            io.fill(false);
                            io.drain(false);
                            origin.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        
                            break;
                    }
                } else{
                    
                    if(action.equals("filling")){
                        if(io.getWaterLevel() >= preferredLevel){
                            
                            this.send(new WashingMessage(this, WashingMessage.Order.WATER_IDLE));
                            
                        }
                    } else if (action.equals("draining")){
                        if(io.getWaterLevel() <= preferredLevel){
                            this.send(new WashingMessage(this, WashingMessage.Order.WATER_IDLE));
                        }
                    }
                    
                }
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
