package service;

public class TimeValidationException extends RuntimeException {
    public TimeValidationException(String message) {
        super(message);
    }
}