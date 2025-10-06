package factory.simulation;

import factory.model.Conveyor;
import factory.model.Tool;
import factory.model.Widget;

public class FactoryController {
    
    public static void main(String[] args) {
        Factory factory = new Factory();

        Conveyor conveyor = factory.getConveyor();
        
        Tool press = factory.getPressTool();
        Tool paint = factory.getPaintTool();

        FactoryMonitor mon = new FactoryMonitor(press, paint, conveyor);

       

        Thread greenBlob = new Thread(() -> {
            try {
                while (true) {
                    press.waitFor(Widget.GREEN_BLOB);
                    mon.pressWidget();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        });
        Thread blueMarbel = new Thread(() -> {
            try {
                while (true) {
                    paint.waitFor(Widget.BLUE_MARBLE);
                    mon.paintWidget();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        });
        greenBlob.start();
        blueMarbel.start();
        
    }
}
