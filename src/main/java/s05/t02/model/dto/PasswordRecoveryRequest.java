package s05.t02.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordRecoveryRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Recovery key is required")
        String recoveryKey,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String newPassword

) {}