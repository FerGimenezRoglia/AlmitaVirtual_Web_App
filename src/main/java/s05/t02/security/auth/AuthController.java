package s05.t02.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s05.t02.model.dto.UserLoginRequest;
import s05.t02.model.dto.UserRegisterRequest;
import s05.t02.service.UserService;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with a username and password.")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or username already exists")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegisterRequest request) {
        userService.registerUser(request.username(), request.password());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns a JWT token upon success.")
    @ApiResponse(responseCode = "200", description = "Login successful, token returned")
    @ApiResponse(responseCode = "400", description = "Invalid username or password")
    @ApiResponse(responseCode = "401", description = "Unauthorized – JWT token invalid or missing")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest loginRequest) {
        String token = userService.authenticateUser(loginRequest.username(), loginRequest.password());
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}