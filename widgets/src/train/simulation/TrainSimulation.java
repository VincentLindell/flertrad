package train.simulation;

import train.model.Route;
import train.model.Segment;
import train.view.TrainView;
import java.util.LinkedList;
import java.util.Queue;

public class TrainSimulation {

    public static void main(String[] args) throws InterruptedException {

        TrainView view = new TrainView();
        
        
        TrainMonitor monitor = new TrainMonitor();
        
        
        int trainLength = 8; // cancellation point

        for (int i = 0; i < 20; i++) {              
            Route route = view.loadRoute();  
            Queue<Segment> queue = new LinkedList<>();       
            Thread t = new Thread(() -> {
                try { // cancellation point 2
                    
                    for (int j = 0; j < trainLength; j++) {
                        Segment next = route.next();
                        monitor.enterSegment(route, next);
                        queue.add(next);
                    }
                    while (true) {
                        Segment next = route.next();
                        monitor.enterSegment(route, next); // cancellation point
                        queue.add(next);

                        Segment last = queue.poll();
                        monitor.exitSegment(route, last);
                    }
                } catch (InterruptedException e) {
                    while (!queue.isEmpty()) {
                        Segment seg = queue.poll();
                        monitor.exitSegment(route, seg); // frigör segment
    }
                    e.printStackTrace();
                }
            });
            t.start();
        }

       
        
    }

}
