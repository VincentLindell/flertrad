import clock.AlarmClockEmulator;
import clock.io.Choice;
import clock.io.ClockInput;
import clock.io.ClockInput.UserInput;
import clock.io.ClockOutput;
import java.util.concurrent.Semaphore;
import java.util.Arrays;

public class ClockMain {
    public static void main(String[] args) throws InterruptedException {
        AlarmClockEmulator emulator = new AlarmClockEmulator();

        ClockInput  in  = emulator.getInput();
        ClockOutput out = emulator.getOutput();
        Semaphore sem = in.getSemaphore();

        ClockMonitor monitor = new ClockMonitor(0, 0, 0);
        AlarmMonitor alarmMonitor = new AlarmMonitor();

        boolean alarmOn = false;
        int[] alarmTime = new int[]{0, 0, 0};

        Thread tickingClock = new Thread(() -> {

            while (true) {  
                try {
                    Thread.sleep(1000); 
                    monitor.tick();
                    int[] time = monitor.getTime();
                    out.displayTime(time[0], time[1], time[2]);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; 
                }

            }
        });

        Thread alarmRing = new Thread(() -> {

            while (true) {  
                try {

                    if(Arrays.equals(alarmMonitor.getAlarmTime(), monitor.getTime()) && alarmMonitor.isAlarmOn() && alarmMonitor.isRinging() == false){
                        System.out.println("Alarm ringing!");
                        alarmMonitor.setRinging(true);
                        int remaining = 40;
                        while(remaining > 0 && alarmMonitor.isAlarmOn()){
                            out.setAlarmIndicator(remaining % 2 == 0);
                            Thread.sleep(500);
                            remaining--;
                        }
                        out.setAlarmIndicator(false);
                        alarmMonitor.toggleAlarm();
                        alarmMonitor.setRinging(false);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; 
                }

            }
        });

        tickingClock.start();
        alarmRing.start();

        while(true){
            sem.acquire();
            UserInput userInput = in.getUserInput();
            Choice c = userInput.choice();
            int h = userInput.hours();
            int m = userInput.minutes();
            int s = userInput.seconds();
            if(c == Choice.SET_TIME){
                monitor.setTime(h, m, s);
                out.displayTime(h, m, s);
            }
            if(c == Choice.TOGGLE_ALARM){
                alarmMonitor.toggleAlarm();
                out.setAlarmIndicator(alarmMonitor.isAlarmOn());
            }
            if(c==Choice.SET_ALARM){
                alarmMonitor.setAlarm(h, m, s);
            }
            System.out.println("choice=" + c + " h=" + h + " m=" + m + " s=" + s);
        }
        
    }

}

    
