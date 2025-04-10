package s05.t02.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import s05.t02.model.enums.EnvironmentColor;

public record EnvironmentCreateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        EnvironmentColor color,

        @Size(max = 255, message = "URL must be at most 255 characters")
        String url
) {}