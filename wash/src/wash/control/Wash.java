package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.simulation.WashingSimulator;

public class Wash {

    public static void main(String[] args) throws InterruptedException {
        WashingSimulator sim = new WashingSimulator(Settings.SPEEDUP);

        WashingIO io = sim.startSimulation();

        ActorThread<WashingMessage> temp = new TemperatureController(io);
        ActorThread<WashingMessage> water = new WaterController(io);
        ActorThread<WashingMessage> spin = new SpinController(io);

        temp.start();
        water.start();
        spin.start();

      
        ActorThread<WashingMessage> wp = null;
        

        while (true) {
            int n = io.awaitButton();
            System.out.println("user selected program " + n);

            // Om ett program har avslutats naturligt, nollställ referensen
            if (wp != null && !wp.isAlive()) {
                wp = null;
            }

            if (n == 0 && wp != null) {
                System.out.println("STOP pressed – interrupting current program");
                wp.interrupt();
                wp = null;

            } else if (n == 1 && wp == null) {
                wp = new WashingProgram1(io, temp, water, spin);
                wp.start();

            } else if (n == 2 && wp == null) {
                wp = new WashingProgram2(io, temp, water, spin);
                wp.start();

            } else if (n == 3 && wp == null) {
                wp = new WashingProgram3(io, temp, water, spin);
                wp.start();

            } else if (wp != null) {
                System.out.println("A program is already running, press 0 to stop it first!");
            }
        }

    }
}

    

