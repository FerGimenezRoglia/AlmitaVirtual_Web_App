package s05.t02.exception.custom;

public class InvalidEnvironmentActionException extends RuntimeException {
    public InvalidEnvironmentActionException(String message) {
        super(message);
    }
}
