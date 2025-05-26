package s05.t02.service.Impl;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import s05.t02.exception.custom.*;
import s05.t02.model.Environment;
import s05.t02.model.User;
import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.service.logic.EnvironmentStatusResolver;
import s05.t02.model.enums.InteractionType;
import s05.t02.model.enums.UserRole;
import s05.t02.repository.EnvironmentRepository;
import s05.t02.repository.UserRepository;
import s05.t02.service.EnvironmentFileService;
import com.amazonaws.services.s3.AmazonS3;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EnvironmentFileServiceImpl implements EnvironmentFileService {

    private final EnvironmentRepository environmentRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public EnvironmentFileServiceImpl(EnvironmentRepository environmentRepository, UserRepository userRepository, AmazonS3 amazonS3) {
        this.environmentRepository = environmentRepository;
        this.userRepository = userRepository;
        this.amazonS3 = amazonS3;
    }

    @Override
    public EnvironmentDTO uploadFile(Long environmentId, MultipartFile file, String username) {
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

        if (file == null || file.isEmpty()) {
            log.warn("No file was provided for upload");
            throw new FileValidationException("A file must be provided for upload");
        }
        String contentType = file.getContentType();
        if (!("application/pdf".equals(contentType) ||
                "image/jpeg".equals(contentType) ||
                "image/png".equals(contentType))) {

            log.warn("Invalid file type: {}", contentType);
            throw new FileValidationException("Only PDF, JPG or PNG files are allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            log.warn("File size exceeds limit: {} bytes", file.getSize());
            throw new FileValidationException("The file exceeds the maximum allowed size of 5MB");
        }

        try {
            String bucketName = "almita-virtual-archivos";
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata);
            amazonS3.putObject(request);

            log.debug("File '{}' uploaded to bucket '{}'", fileName, bucketName);

            String previousUrl = environment.getUrl();
            if (previousUrl != null && !previousUrl.isBlank()) {
                try {
                    String key = URLDecoder.decode(previousUrl.substring(previousUrl.lastIndexOf("/") + 1), StandardCharsets.UTF_8);
                    amazonS3.deleteObject(bucketName, key);
                    log.info("Previous file '{}' successfully deleted from S3", key);
                } catch (Exception e) {
                    log.error("Error deleting previous file from S3: {}", e.getMessage(), e);
                    throw new FileStorageException("Failed to delete previous file from S3");
                }
            }

            String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();
            environment.setUrl(fileUrl);

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

        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
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

        String fileUrl = environment.getUrl();
        if (fileUrl != null && !fileUrl.isBlank()) {
            try {
                String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                amazonS3.deleteObject(bucketName, key);
                log.info("File '{}' successfully deleted from S3", key);
            } catch (Exception e) {
                log.error("Error deleting file from S3: {}", e.getMessage(), e);
                throw new FileStorageException("Failed to delete file from S3");
            }
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
