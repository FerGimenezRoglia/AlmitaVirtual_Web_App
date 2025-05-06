package s05.t02.model.dto;

import s05.t02.model.enums.EnvironmentColor;
import s05.t02.model.enums.EnvironmentStatus;

public record PublicEnvironmentDTO(
        Long id,
        String title,
        String description,
        EnvironmentColor color,
        String url,
        EnvironmentStatus status,
        String username
) {}