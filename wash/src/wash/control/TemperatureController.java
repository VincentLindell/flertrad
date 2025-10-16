package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {
    private final WashingIO io;

    private boolean running = true;
    private boolean heating = false;
    private int targetTemp = 0;
    private ActorThread<WashingMessage> sender = null;

    private static final int dt = 10 * 1000 / Settings.SPEEDUP; // 10 s period
    // finjusteringar
private final double mu = 0.9;  // stäng av lite senare
private final double ml = 0.25; // slå på lite tidigare


    public TemperatureController(WashingIO io) {
        this.io = io;
    }

    @Override
    public void run() {
        try {
            while (running) {
                WashingMessage m = receiveWithTimeout(dt);
                
                if (m != null) {
                    System.out.println("TemperatureController: got " + m);
                    switch (m.order()) {
                        case TEMP_SET_40:
                            targetTemp = 40;
                            sender = m.sender();
                            break;

                        case TEMP_SET_60:
                            targetTemp = 60;
                            sender = m.sender();
                            break;

                        case TEMP_IDLE:
                            targetTemp = 0;
                            io.heat(false);
                            heating = false;
                            sender = m.sender();
                            sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                            break;
                    }
                }

                if (targetTemp > 0 && io.getWaterLevel() > 0) { // SR1: heat only if water present
                    double T = io.getTemperature();
                    double lowerBound = targetTemp - 2;
                    double upperBound = targetTemp;

                    // Heat ON: only if we are below safe lower margin
                    if (!heating && T <= (lowerBound - ml+0.5 )) {
                        io.heat(true);
                        heating = true;
                        
                    }

                    // Heat OFF: early enough to avoid overshoot
                    else if (heating && T >= (upperBound - mu)) {
                        io.heat(false);
                        heating = false;
                        
                    }

                    // Send ACK once when first reaching the valid range
                    if (sender != null && T >= lowerBound && T < upperBound) {
                        sender.send(new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT));
                        sender = null;
                        
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }
}
