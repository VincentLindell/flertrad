public class AlarmMonitor{
    private boolean alarmOn = false;;
    private int[] alarmTime = new int[]{0, 0, 0};
    private boolean ringing = false;

    public AlarmMonitor(){
        this.alarmOn = false;
    }

    public void setAlarm(int h, int m, int s){
        this.alarmTime = new int[]{h, m, s};
    }

    public boolean isAlarmOn(){
        return alarmOn;
    }

    public void toggleAlarm(){
        alarmOn = !alarmOn;
    }

    public void setRinging(boolean ringing){
        this.ringing = ringing;
    }

    public boolean isRinging(){
        return ringing;
    }

    public int[] getAlarmTime(){
        // To be implemented in future exercises
        return alarmTime;
    }
}