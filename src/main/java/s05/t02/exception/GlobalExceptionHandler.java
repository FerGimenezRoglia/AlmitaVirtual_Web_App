package s05.t02.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import s05.t02.exception.custom.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.error("Invalid credentials attempt: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Credentials", ex.getMessage());
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwt(InvalidJwtTokenException ex) {
        log.error("Invalid or expired JWT token: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Token", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameExists(UsernameAlreadyExistsException ex) {
        log.warn("Username already exists: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Username Already Exists", ex.getMessage());
    }

    @ExceptionHandler(EnvironmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEnvironmentNotFound(EnvironmentNotFoundException ex) {
        log.error("Environment not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Environment Not Found", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedEnvironmentAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedEnvironment(UnauthorizedEnvironmentAccessException ex) {
        log.warn("Unauthorized Environment access attempt: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Unauthorized Environment Access", ex.getMessage());
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageError(FileStorageException ex) {
        log.error("File handling error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "File Error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("Validation failed: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", message);
    }

    @ExceptionHandler(InvalidEnvironmentActionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAction(InvalidEnvironmentActionException ex) {
        log.error("Invalid Environment action: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Environment Action", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message) {
        ErrorResponse errorResponse = new ErrorResponse(status, error, message);
        return ResponseEntity.status(status).body(errorResponse);
    }
}
