
import lift.LiftView;
import lift.Passenger;

public class manyPeopleRidesLift {
    public static void main(String[] args) {

        final int NBR_FLOORS = 7, MAX_PASSENGERS = 1;
        int amount = 1;

        LiftView  view = new LiftView(NBR_FLOORS, MAX_PASSENGERS);

        LiftMonitor monitor = new LiftMonitor(view, NBR_FLOORS, MAX_PASSENGERS);
        

        for(int i = 0; i < amount; i++) {
            Thread t = new Thread(() -> {
                try {
                    while(true){
                        Passenger pass = view.createPassenger();
                        int fromFloor = pass.getStartFloor();
                        int toFloor = pass.getDestinationFloor();

                        pass.begin();
                        monitor.passengerArrives(fromFloor); 
                        monitor.addPassToQueue(pass);

                        monitor.enter(fromFloor, toFloor, pass); 

                        monitor.exit(fromFloor, toFloor, pass);
                        
                        pass.end();
                    }
                    
                    
                    

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            
            t.start();
        
        }

        

        Thread lift = new Thread(() -> {
            try {
                int direction = 1; // 1 = uppåt, -1 = nedåt
                int floor = 0;

                while (true) {
                    int nextFloor = floor + direction;

                    // byt riktning om du nått toppen eller botten
                    if (nextFloor == NBR_FLOORS || nextFloor < 0) {
                        direction = -direction;
                        nextFloor = floor + direction;
                    }
                    
                    monitor.startMoving(floor, nextFloor);
                    floor = nextFloor; // uppdatera aktuell våning
                    monitor.openDoorsIfPassengersWaiting();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


            lift.start();

                                
    }

    
            

}
