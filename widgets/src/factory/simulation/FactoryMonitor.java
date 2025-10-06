package factory.simulation;

import factory.model.Conveyor;
import factory.model.Tool;

public class FactoryMonitor {

    private final Tool press;
    private final Tool paint;
    private final Conveyor conveyor;

    private boolean pressInUse = false;
    private boolean paintInUse = false;
    private boolean conveyorRunning = true;

    public FactoryMonitor(Tool press, Tool paint, Conveyor conveyor) {
        this.press = press;
        this.paint = paint;
        this.conveyor = conveyor;
    }

    
    public void pressWidget() throws InterruptedException {
        waitUntilSafeToPress();
        press.performAction();    
        donePressing();
    }

    private synchronized void waitUntilSafeToPress() throws InterruptedException {
        while (pressInUse) {
            wait();
        }
        conveyor.off();
        conveyorRunning = false;
        pressInUse = true;
    }

    private synchronized void donePressing() {
        pressInUse = false;
   
        if(!paintInUse) {
            conveyor.on();
            conveyorRunning = true;
        }
        
        notifyAll();
    }


    public void paintWidget() throws InterruptedException {
        waitUntilSafeToPaint();
        paint.performAction();     
        donePainting();
    }

    private synchronized void waitUntilSafeToPaint() throws InterruptedException {
        while (paintInUse ) {
            wait();
        }
        conveyor.off();
        conveyorRunning = false;
        paintInUse = true;
    }

    private synchronized void donePainting() {
        paintInUse = false;
        
        if(!pressInUse) {
            conveyor.on();
            conveyorRunning = true;
        }
        notifyAll();
    }
}
