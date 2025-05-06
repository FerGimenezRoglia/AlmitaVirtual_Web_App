package s05.t02.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import s05.t02.exception.custom.EnvironmentNotFoundException;
import s05.t02.model.Environment;
import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.model.dto.PublicEnvironmentDTO;
import s05.t02.model.enums.InteractionType;
import s05.t02.repository.EnvironmentRepository;
import s05.t02.service.PublicEnvironmentInteractionService;
import s05.t02.service.logic.EnvironmentStatusResolver;

@Slf4j
@Service
public class PublicEnvironmentInteractionServiceImpl implements PublicEnvironmentInteractionService {

    private final EnvironmentRepository environmentRepository;

    public PublicEnvironmentInteractionServiceImpl(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
    }

    @Override
    public PublicEnvironmentDTO getPublicEnvironmentById(Long environmentId) {
        log.info("Public request to fetch environment with ID {}", environmentId);

        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> {
                    log.warn("Environment with ID {} not found in public access", environmentId);
                    return new EnvironmentNotFoundException("Environment not found");
                });

        return new PublicEnvironmentDTO(
                environment.getId(),
                environment.getTitle(),
                environment.getDescription(),
                environment.getColor(),
                environment.getUrl(),
                environment.getStatus(),
                environment.getUser().getUsername()
        );
    }

    @Override
    public String getFileUrl(Long environmentId) {
        log.info("Public request to access file URL from environment ID {}", environmentId);

        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> {
                    log.warn("Environment with ID {} not found when requesting file URL", environmentId);
                    return new EnvironmentNotFoundException("Environment not found");
                });

        if (environment.getUrl() == null || environment.getUrl().isBlank()) {
            log.warn("Environment with ID {} has no file to provide", environmentId);
            throw new EnvironmentNotFoundException("No file available for this environment");
        }

        EnvironmentStatusResolver.updateStatus(environment, InteractionType.FILE_VIEW);

        environmentRepository.save(environment);
        log.info("File view recorded and status updated for environment ID {}", environmentId);

        return environment.getUrl();
    }

    @Override
    public EnvironmentDTO markAsInterested(Long environmentId) {
        log.info("Public request to mark environment ID {} as 'interested'", environmentId);

        Environment environment = environmentRepository.findById(environmentId)
                .orElseThrow(() -> {
                    log.warn("Environment with ID {} not found when marking as interested", environmentId);
                    return new EnvironmentNotFoundException("Environment not found");
                });

        EnvironmentStatusResolver.updateStatus(environment, InteractionType.MARK_AS_INTERESTED);

        Environment updated = environmentRepository.save(environment);
        log.info("Environment ID {} marked as interested successfully", environmentId);

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
