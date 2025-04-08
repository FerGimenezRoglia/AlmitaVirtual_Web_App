package s05.t02.model.dto;

import s05.t02.model.enums.UserRole;

public record UserDTO(
        Long id,
        String username,
        UserRole role
) {}