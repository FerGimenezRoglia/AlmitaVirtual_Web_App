package s05.t02.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.service.PublicEnvironmentInteractionService;

@RestController
@RequestMapping("/public/environments")
@Tag(name = "Public Environment Access", description = "Public endpoints to access shared environments")
public class PublicEnvironmentInteractionController {

    private final PublicEnvironmentInteractionService publicService;

    public PublicEnvironmentInteractionController(PublicEnvironmentInteractionService publicService) {
        this.publicService = publicService;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "View a public environment",
            description = "Returns the environment with the specified ID. No authentication required.")
    @ApiResponse(responseCode = "200", description = "Environment retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Environment not found")
    public ResponseEntity<EnvironmentDTO> getPublicEnvironment(@PathVariable Long id) {
        EnvironmentDTO dto = publicService.getPublicEnvironmentById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/file")
    @Operation(
            summary = "Access environment file",
            description = "Returns the file URL from a public environment. Also updates the status to EXCITED.")
    @ApiResponse(responseCode = "200", description = "File URL returned successfully")
    @ApiResponse(responseCode = "404", description = "Environment not found or file not present")
    public ResponseEntity<String> getFileUrl(@PathVariable Long id) {
        String fileUrl = publicService.getFileUrl(id);
        return ResponseEntity.ok(fileUrl);
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Mark environment as interested",
            description = "Updates the environment status to INSPIRED. No authentication required.")
    @ApiResponse(responseCode = "200", description = "Environment marked as interested successfully")
    @ApiResponse(responseCode = "404", description = "Environment not found")
    public ResponseEntity<EnvironmentDTO> markAsInterested(@PathVariable Long id) {
        EnvironmentDTO updated = publicService.markAsInterested(id);
        return ResponseEntity.ok(updated);
    }
}