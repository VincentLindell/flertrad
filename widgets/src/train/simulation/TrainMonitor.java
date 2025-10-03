package train.simulation;

import train.model.Route;
import train.model.Segment;
import train.view.TrainView;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TrainMonitor {
    private final Set<Segment> busy = new HashSet<>();


    public  void enterSegment(Route route, Segment segment) throws InterruptedException {
        synchronized (this) {
            while (busy.contains(segment)) {
                wait();
            }
            busy.add(segment);
            
        }
        segment.enter();
    }

    public synchronized void exitSegment(Route route, Segment segment){
        busy.remove(segment);
        segment.exit();
        notifyAll();
    }

    
}