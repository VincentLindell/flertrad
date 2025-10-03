package train.model;

/**
 * A short track section. The segment can by occupied by at most one train at a given time.
 * A segment is connected to a TrainView by its Route, so when enter()/exit() are called,
 * this is reflected in the TrainView.
 */
public interface Segment {
    
    /**
     * Displays this segment as occupied by the current thread (train). The segment must not be occupied already.
     * This method also makes an initial delay, before marking the segment, to reflect the motion of the train.
     */
    void enter() throws InterruptedException;

    /**
     * Displays this segment as free. The segment must not be occupied by another thread (train).
     * (Exiting an already free segment is permitted, however.)
     */
    void exit();
}
