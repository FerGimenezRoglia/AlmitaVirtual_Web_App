package s05.t02.model.dto;

import jakarta.validation.constraints.Size;
import s05.t02.model.enums.EnvironmentColor;

public record EnvironmentUpdateRequest(
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        EnvironmentColor color

) {}