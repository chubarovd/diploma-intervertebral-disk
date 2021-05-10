package utils;

public class Timer {

    private int currentTimestamp = 0;
    private int limit = -1;
    private int step = 1;

    /**
     * Simple timer: non-limited and with step = 1.
     */
    public Timer() {}

    /**
     * Limited timer with specific limit.
     */
    public Timer(int limit) {
        this.limit = limit;
    }

    /**
     * Limited timer with specific limit and step.
     */
    public Timer(int limit, int step) {
        this.limit = limit;
        this.step = step;
    }

    public double tick() throws TimerLimitException {
        if (limit != -1 && currentTimestamp == limit) {
            throw new TimerLimitException();
        }
        return (currentTimestamp += step);
    }

    public double current() {
        return currentTimestamp;
    }
}
