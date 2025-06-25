package s05.t02.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.service.EnvironmentFileService;

@RestController
@RequestMapping("/environments")
@Tag(name = "File Management", description = "Private endpoints for uploading and deleting files from environments")
public class EnvironmentFileController {

    private final EnvironmentFileService environmentFileService;

    public EnvironmentFileController(EnvironmentFileService environmentFileService) {
        this.environmentFileService = environmentFileService;
    }

    @PostMapping("/{id}/file")
    @Operation(
            summary = "Upload a file to an environment",
            description = "Allows the authenticated user (or admin) to upload or replace a file. Updates environment status to ACTIVE.")
    @ApiResponse(responseCode = "200", description = "File uploaded successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized – JWT token is missing or invalid")
    @ApiResponse(responseCode = "403", description = "Forbidden – User not authorized to upload file")
    @ApiResponse(responseCode = "404", description = "Environment or user not found")
    public ResponseEntity<EnvironmentDTO> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file, Authentication authentication) {
        String username = authentication.getName();
        EnvironmentDTO updated = environmentFileService.uploadFile(id, file, username);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/file")
    @Operation(
            summary = "Delete file from an environment",
            description = "Allows the authenticated user (or admin) to delete a file from an environment. Updates environment status to REFLECTIVE.")
    @ApiResponse(responseCode = "200", description = "File deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized – JWT token is missing or invalid")
    @ApiResponse(responseCode = "403", description = "Forbidden – User not authorized to delete file")
    @ApiResponse(responseCode = "404", description = "Environment or user not found, or file is not present")
    public ResponseEntity<EnvironmentDTO> deleteFile(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        EnvironmentDTO updated = environmentFileService.deleteFile(id, username);
        return ResponseEntity.ok(updated);
    }

}