package s05.t02.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import s05.t02.exception.custom.EnvironmentNotFoundException;
import s05.t02.exception.custom.InvalidEnvironmentActionException;
import s05.t02.exception.custom.UnauthorizedEnvironmentAccessException;
import s05.t02.exception.custom.UserNotFoundException;
import s05.t02.model.Environment;
import s05.t02.model.User;
import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.service.logic.EnvironmentStatusResolver;
import s05.t02.model.enums.InteractionType;
import s05.t02.model.enums.UserRole;
import s05.t02.repository.EnvironmentRepository;
import s05.t02.repository.UserRepository;
import s05.t02.service.EnvironmentFileService;

@Slf4j
@Service
public class EnvironmentFileServiceImpl implements EnvironmentFileService {

    private final EnvironmentRepository environmentRepository;
    private final UserRepository userRepository;

    public EnvironmentFileServiceImpl(EnvironmentRepository environmentRepository, UserRepository userRepository) {
        this.environmentRepository = environmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public EnvironmentDTO uploadFile(Long environmentId, String fileUrl, String username) {
        log.info("User '{}' is uploading file to environment ID {}", username, environmentId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found", username);
                    return new UserNotFoundException("User not found");
                });

        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> {
                    log.warn("Environment with ID {} not found", environmentId);
                    return new EnvironmentNotFoundException("Environment not found");
                });

        boolean isOwner = environment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == UserRole.ROLE_ADMIN;
        if (!isOwner && !isAdmin) {
            log.warn("User '{}' is not authorized to upload file to environment ID {}", username, environmentId);
            throw new UnauthorizedEnvironmentAccessException("Unauthorized to upload file");
        }

        environment.setUrl(fileUrl);
        log.debug("Updated environment URL to '{}'", fileUrl);

        EnvironmentStatusResolver.updateStatus(environment, InteractionType.FILE_UPLOAD);

        Environment saved = environmentRepository.save(environment);
        log.info("File uploaded successfully to environment ID {}", saved.getId());

        return new EnvironmentDTO(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getColor(),
                saved.getUrl(),
                saved.getStatus(),
                saved.getUser().getId()
        );
    }

    @Override
    public EnvironmentDTO deleteFile(Long environmentId, String username) {
        log.info("User '{}' is attempting to delete file from environment ID {}", username, environmentId);

        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> {
                    log.warn("Environment with ID {} not found", environmentId);
                    return new EnvironmentNotFoundException("Environment not found");
                });

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found while deleting file", username);
                    return new UserNotFoundException("User not found");
                });

        boolean isOwner = environment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals(UserRole.ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            log.warn("User '{}' is not authorized to delete file from environment ID {}", username, environmentId);
            throw new UnauthorizedEnvironmentAccessException("Unauthorized to delete file from this environment");
        }

        if (environment.getUrl() == null || environment.getUrl().isBlank()) {
            log.warn("Environment ID {} has no file to delete", environmentId);
            throw new InvalidEnvironmentActionException("No file to delete in this environment");
        }

        environment.setUrl(null);
        EnvironmentStatusResolver.updateStatus(environment, InteractionType.FILE_DELETE);

        Environment updated = environmentRepository.save(environment);
        log.info("File deleted successfully from environment ID {} by user '{}'", environmentId, username);

        return new EnvironmentDTO(
                updated.getId(),
                updated.getTitle(),
                updated.getDescription(),
                updated.getColor(),
                updated.getUrl(),
                updated.getStatus(),
                updated.getUser().getId()
        );
    }

}
