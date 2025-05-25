package s05.t02.service.Impl;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import s05.t02.exception.custom.EnvironmentNotFoundException;
import s05.t02.exception.custom.FileStorageException;
import s05.t02.exception.custom.UnauthorizedEnvironmentAccessException;
import s05.t02.exception.custom.UserNotFoundException;
import s05.t02.model.Environment;
import s05.t02.model.User;
import s05.t02.model.dto.EnvironmentCreateRequest;
import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.model.dto.EnvironmentUpdateRequest;
import s05.t02.model.enums.EnvironmentColor;
import s05.t02.model.enums.EnvironmentStatus;
import s05.t02.model.enums.UserRole;
import s05.t02.repository.EnvironmentRepository;
import s05.t02.repository.UserRepository;
import s05.t02.service.EnvironmentService;

import java.util.List;

@Slf4j
@Service
public class EnvironmentServiceImpl implements EnvironmentService {

    private final EnvironmentRepository environmentRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public EnvironmentServiceImpl(EnvironmentRepository environmentRepository, UserRepository userRepository, AmazonS3 amazonS3) {
        this.environmentRepository = environmentRepository;
        this.userRepository = userRepository;
        this.amazonS3 = amazonS3;

    }

    @Override
    public EnvironmentDTO createEnvironment(EnvironmentCreateRequest request, String username) {
        log.info("Creating environment for user '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found while creating environment", username);
                    return new UserNotFoundException("User not found");
                });

        EnvironmentColor color = request.color() != null ? request.color() : EnvironmentColor.NEUTRAL;

        EnvironmentStatus status = (request.url() != null && !request.url().isBlank())
                ? EnvironmentStatus.ACTIVE
                : EnvironmentStatus.IDLE;

        Environment environment = Environment.builder()
                .title(request.title())
                .description(request.description())
                .color(color)
                .url(request.url())
                .status(status)
                .user(user)
                .build();

        Environment saved = environmentRepository.save(environment);
        log.info("Environment '{}' created successfully with ID {} for user '{}'", saved.getTitle(), saved.getId(), username);

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
    public List<Environment> getEnvironments() {
        log.info("Fetching environments");

        List<Environment> environments = environmentRepository.findAll();

        if (environments.isEmpty()) {
            log.info("No environments found");
        } else {
            log.info("Found {} environments", environments.size());
        }
        return environments;
    }

    @Override
    public List<Environment> getUserEnvironments(String username) {
        log.info("Fetching environments for user '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found while fetching environments", username);
                    return new UserNotFoundException("User not found");
                });

        List<Environment> environments = environmentRepository.findByUserId(user.getId());

        if (environments.isEmpty()) {
            log.info("No environments found for user '{}'", username);
        } else {
            log.info("Found {} environments for user '{}'", environments.size(), username);
        }
        return environments;
    }

    @Override
    public Environment getEnvironmentById(Long id, String username) {
        log.info("Fetching environment with ID {} for user '{}'", id, username);

        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Environment with ID {} not found", id);
                    return new EnvironmentNotFoundException("Environment not found");
                });

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found while accessing environment ID {}", username, id);
                    return new UserNotFoundException("User not found");
                });

        if (!environment.getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ROLE_ADMIN) {
            log.warn("Unauthorized access attempt by user '{}' to environment ID {}", username, id);
            throw new UnauthorizedEnvironmentAccessException("Access denied to this environment");
        }

        log.info("Environment with ID {} retrieved successfully for user '{}'", id, username);
        return environment;
    }

    @Override
    public Environment updateEnvironment(Long id, EnvironmentUpdateRequest request, String username) {
        log.info("Updating environment with ID {} for user '{}'", id, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found while updating environment", username);
                    return new UserNotFoundException("User not found");
                });

        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Environment with ID {} not found", id);
                    return new EnvironmentNotFoundException("Environment not found");
                });

        boolean isOwner = environment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals(UserRole.ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            log.warn("User '{}' is not authorized to update environment with ID {}", username, id);
            throw new UnauthorizedEnvironmentAccessException("Unauthorized to update this environment");
        }

        if (request.title() != null) {
            environment.setTitle(request.title());
            log.debug("Updated title to '{}'", request.title());
        }

        if (request.description() != null) {
            environment.setDescription(request.description());
            log.debug("Updated description to '{}'", request.description());
        }

        if (request.color() != null) {
            environment.setColor(request.color());
            log.debug("Updated color to '{}'", request.color());
        }

        Environment updated = environmentRepository.save(environment);
        log.info("Environment with ID {} updated successfully", updated.getId());

        return updated;
    }

    @Override
    public void deleteEnvironment(Long id, String username) {
        log.info("Attempting to delete environment with ID {} for user '{}'", id, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found while deleting environment", username);
                    return new UserNotFoundException("User not found");
                });

        Environment environment = environmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Environment with ID {} not found", id);
                    return new EnvironmentNotFoundException("Environment not found");
                });

        boolean isOwner = environment.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().equals(UserRole.ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            log.warn("User '{}' is not authorized to delete environment with ID {}", username, id);
            throw new UnauthorizedEnvironmentAccessException("Unauthorized to delete this environment");
        }

        if (environment.getUrl() != null && !environment.getUrl().isBlank()) {
            try {
                String key = environment.getUrl().substring(environment.getUrl().lastIndexOf("/") + 1);
                amazonS3.deleteObject(bucketName, key);
                log.info("Deleted file '{}' from S3 before deleting environment", key);
            } catch (Exception e) {
                log.error("Failed to delete file from S3 before deleting environment", e);
                throw new FileStorageException("Could not delete associated file from S3");
            }
        }

        environmentRepository.delete(environment);
        log.info("Environment with ID {} deleted successfully by user '{}'", id, username);
    }

}
