package utils;

public class TimerLimitException extends Exception {
    public TimerLimitException() {
        super("Timer reached limit.");
    }
}
