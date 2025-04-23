package s05.t02.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import s05.t02.model.Environment;
import s05.t02.model.dto.EnvironmentCreateRequest;
import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.model.dto.EnvironmentUpdateRequest;
import s05.t02.service.EnvironmentService;

import java.util.List;

@RestController
@RequestMapping("/environments")
@Tag(name = "Environments", description = "Private endpoints for managing user environments")
public class EnvironmentController {
    private final EnvironmentService environmentService;

    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping
    @Operation(
            summary = "Get all environments",
            description = "Returns all environments of the authenticated user, or all environments if admin"
    )
    @ApiResponse(responseCode = "200", description = "List of environments retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized – JWT token is missing or invalid")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<List<EnvironmentDTO>> getUserEnvironments(Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        List<Environment> environments;

        if (isAdmin) {
            environments = environmentService.getEnvironments();
        } else {
            environments = environmentService.getUserEnvironments(username);
        }

        List<EnvironmentDTO> dtoList = environments.stream()
                .map(env -> new EnvironmentDTO(
                        env.getId(),
                        env.getTitle(),
                        env.getDescription(),
                        env.getColor(),
                        env.getUrl(),
                        env.getStatus(),
                        env.getUser().getId()
                ))
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get environment by ID",
            description = "Returns a specific environment if the user is the owner or an admin")
    @ApiResponse(responseCode = "200", description = "Environment retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized – JWT token is missing or invalid")
    @ApiResponse(responseCode = "403", description = "Forbidden – User does not have access to this environment")
    @ApiResponse(responseCode = "404", description = "Environment or user not found")
    public ResponseEntity<EnvironmentDTO> getEnvironmentById(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();

        Environment environment = environmentService.getEnvironmentById(id, username);
        EnvironmentDTO dto = new EnvironmentDTO(
                environment.getId(),
                environment.getTitle(),
                environment.getDescription(),
                environment.getColor(),
                environment.getUrl(),
                environment.getStatus(),
                environment.getUser().getId()
        );
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @Operation(
            summary = "Create a new environment",
            description = "Creates a new environment for the authenticated user. Sets status to ACTIVE if a file URL is included, otherwise IDLE.")
    @ApiResponse(responseCode = "201", description = "Environment created successfully")
    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid input data")
    @ApiResponse(responseCode = "401", description = "Unauthorized – JWT token is missing or invalid")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<EnvironmentDTO> createEnvironment(@Valid @RequestBody EnvironmentCreateRequest request, Authentication authentication) {
        String username = authentication.getName();
        EnvironmentDTO created = environmentService.createEnvironment(request, username);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an environment",
            description = "Allows the authenticated user (or admin) to update title, description, or color of an environment. Only non-null fields will be updated."
    )
    @ApiResponse(responseCode = "200", description = "Environment updated successfully")
    @ApiResponse(responseCode = "400", description = "Bad Request – Invalid input data")
    @ApiResponse(responseCode = "401", description = "Unauthorized – JWT token is missing or invalid")
    @ApiResponse(responseCode = "403", description = "Forbidden – User does not have permission to update this environment")
    @ApiResponse(responseCode = "404", description = "Environment or user not found")
    public ResponseEntity<EnvironmentDTO> updateEnvironment(@PathVariable Long id, @Valid @RequestBody EnvironmentUpdateRequest request, Authentication authentication) {
        String username = authentication.getName();

        Environment updated = environmentService.updateEnvironment(id, request, username);
        EnvironmentDTO dto = new EnvironmentDTO(
                updated.getId(),
                updated.getTitle(),
                updated.getDescription(),
                updated.getColor(),
                updated.getUrl(),
                updated.getStatus(),
                updated.getUser().getId()
        );
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an environment",
            description = "Deletes the environment if the authenticated user is the owner or an admin."
    )
    @ApiResponse(responseCode = "204", description = "Environment deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized – JWT token is missing or invalid")
    @ApiResponse(responseCode = "403", description = "Forbidden – User does not have permission to delete this environment")
    @ApiResponse(responseCode = "404", description = "Environment or user not found")
    public ResponseEntity<Void> deleteEnvironment(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        environmentService.deleteEnvironment(id, username);
        return ResponseEntity.noContent().build();
    }

}
