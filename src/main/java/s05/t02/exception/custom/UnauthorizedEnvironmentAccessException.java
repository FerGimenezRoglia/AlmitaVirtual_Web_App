package s05.t02.exception.custom;

public class UnauthorizedEnvironmentAccessException extends RuntimeException {
    public UnauthorizedEnvironmentAccessException(String message) {
        super(message);
    }
}
