
import java.util.Queue;

import lift.LiftView;
import lift.Passenger;

public class LiftMonitor {

    LiftView  view;
    boolean doorsOpen = false;
    boolean liftMoving = false;
    int currentFloor = 0;
    int passengersInLift = 0;
    int[] toEnter;
    int[] toExit;
    private Queue<Passenger>[] waitingQueues;

    int MAX_PASSENGERS;
    int currentlyEntering = 0;
    int currentlyExiting = 0;
    
        public LiftMonitor(LiftView view, int NBR_FLOORS, int MAX_PASSENGERS) {
            this.view = view;
            this.toEnter = new int[NBR_FLOORS];
            this.toExit = new int[NBR_FLOORS];
            this.MAX_PASSENGERS = MAX_PASSENGERS;
            this.waitingQueues = new Queue[NBR_FLOORS];
            for (int i = 0; i < NBR_FLOORS; i++) {
                waitingQueues[i] = new java.util.LinkedList<Passenger>();
            }
        }

        

        public void startMoving(int fromFloor, int toFloor) {
            synchronized (this) {
                while (doorsOpen || liftMoving) {
                    try { wait(); } catch (InterruptedException e) { }
                }
                liftMoving = true;
                doorsOpen = false;
                notifyAll();
            }

            
            view.moveLift(fromFloor, toFloor);

            synchronized (this) {
                liftMoving = false;
                currentFloor = toFloor;
                notifyAll();
            }
        }


        public synchronized void doneMoving() {
            liftMoving = false;
            notifyAll(); 
        }


        public synchronized void closeDoors(int cf) {
            if (!doorsOpen) return;

          
            while (currentlyEntering > 0 || currentlyExiting > 0 ||
                (toEnter[currentFloor] > 0 && passengersInLift < MAX_PASSENGERS) || toExit[currentFloor] > 0) {

                if (cf != currentFloor) return; 
                try { wait(); } catch (InterruptedException e) {}
            }

           
            if (!doorsOpen) return;
            view.closeDoors();
            doorsOpen = false;
            notifyAll();
        }



        public synchronized void openDoors(){
            if(doorsOpen) return;
            view.openDoors(currentFloor);
            doorsOpen = true;
            notifyAll();
            
        }

        public synchronized void enterLift(int fromFloor, int toFloor) {  
            currentlyEntering++;
            
            notifyAll(); 

                 
        }

        

        public synchronized void exitLift(int fromFloor, int toFloor) {

            while (!doorsOpen || liftMoving || currentFloor != toFloor) {
                try { wait(); } catch (InterruptedException e) {}
            }
            currentlyExiting++;
            
            notifyAll();
        }

        

        public synchronized void hasEntered() {
            currentlyEntering--;
            if(currentlyEntering <= 0 && currentlyExiting <= 0) {
                closeDoors(currentFloor);
            }
            
            notifyAll();
            
        }

        public synchronized void hasExited(){
            currentlyExiting--;
            if(currentlyEntering <= 0 && currentlyExiting <= 0) {
                closeDoors(currentFloor);
            }
            
            notifyAll();
        }


        

        public synchronized void waitUntilSafeToEnter(int fromFloor, int toFloor, Passenger p) {
            try {
                while(!doorsOpen || liftMoving || passengersInLift >= MAX_PASSENGERS || currentFloor != fromFloor || waitingQueues[fromFloor].peek() != p) {
                    wait();
                }  

                waitingQueues[fromFloor].poll();
                toEnter[fromFloor]--;
                toExit[toFloor]++;
                passengersInLift++;
                notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public synchronized void waitUntilSafeToExit(int myDestination) {
            try {
                while (!doorsOpen || currentFloor != myDestination) {
                    wait();
                }
            
                toExit[myDestination]--;
                passengersInLift--;
                
                notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



        public synchronized int getCurrentFloor(){
            return currentFloor;
        }

        public synchronized void openDoorsIfPassengersWaiting() {
            if ((toEnter[currentFloor] > 0 && passengersInLift < MAX_PASSENGERS) || toExit[currentFloor] > 0) {
                openDoors();
            } 
        }


        public synchronized void passengerArrives(int floor) {
            toEnter[floor]++;
            notifyAll();
        }

        public synchronized void addPassToQueue(Passenger p) {
            waitingQueues[p.getStartFloor()].add(p);
        }

        public synchronized void performEnterLift(int from, int to, Passenger p) {
            while (!doorsOpen || liftMoving || passengersInLift >= MAX_PASSENGERS
                || currentFloor != from || waitingQueues[from].peek() != p) {
                try { wait(); } catch (InterruptedException e) {}
            }

            waitingQueues[from].poll();
            toEnter[from]--;
            toExit[to]++;
            passengersInLift++;
            notifyAll();    
        }

        public synchronized void exit(int from, int to, Passenger p){
            waitUntilSafeToExit(to);
            exitLift(from, to);
            p.exitLift();
            hasExited();
        }

        public synchronized void enter(int from, int to, Passenger p){
            waitUntilSafeToEnter(from, to, p); 
            enterLift(from, to); 
            p.enterLift();
            hasEntered();
        }
    
        
}
