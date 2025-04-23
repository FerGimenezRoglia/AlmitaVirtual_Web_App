package s05.t02.service;

import s05.t02.model.Environment;
import s05.t02.model.dto.EnvironmentCreateRequest;
import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.model.dto.EnvironmentUpdateRequest;

import java.util.List;

public interface EnvironmentService {
    EnvironmentDTO createEnvironment(EnvironmentCreateRequest request, String username);
    List<Environment> getEnvironments();
    List<Environment> getUserEnvironments(String username);
    Environment getEnvironmentById(Long id, String username);
    Environment updateEnvironment(Long id, EnvironmentUpdateRequest request, String username);
    void deleteEnvironment(Long id, String username);
}