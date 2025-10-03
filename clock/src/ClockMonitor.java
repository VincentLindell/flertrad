import java.util.concurrent.Semaphore;

public class ClockMonitor{
    private int h, m, s;
    private final Semaphore sem = new Semaphore(1);

    public ClockMonitor(int h, int m, int s) {
        this.h = h;
        this.m = m;
        this.s = s;
    }

    public void tick() throws InterruptedException{
        sem.acquire();
        s++;
        if (s == 60) { s = 0; m++; }
        if (m == 60) { m = 0; h++; }
        if (h == 24) { h = 0; }
        sem.release();
    }

    public void setTime(int h, int m, int s) throws InterruptedException{
        sem.acquire();
        this.h = h;
        this.m = m;
        this.s = s;
        sem.release();
    }

    public int[] getTime() throws InterruptedException{
        sem.acquire();
        int[] time = {h, m, s};
        sem.release();
        return time;
    }

    public void alarmON(){
        // To be implemented in future exercises
    }
}