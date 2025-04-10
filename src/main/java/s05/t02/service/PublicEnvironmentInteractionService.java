package s05.t02.service;

import s05.t02.model.dto.EnvironmentDTO;

public interface PublicEnvironmentInteractionService {

    EnvironmentDTO getPublicEnvironmentById(Long environmentId);
    String getFileUrl(Long environmentId);
    EnvironmentDTO markAsInterested(Long environmentId);
}
