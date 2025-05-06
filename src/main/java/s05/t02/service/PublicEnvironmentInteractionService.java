package s05.t02.service;

import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.model.dto.PublicEnvironmentDTO;

public interface PublicEnvironmentInteractionService {

    PublicEnvironmentDTO getPublicEnvironmentById(Long environmentId);
    String getFileUrl(Long environmentId);
    EnvironmentDTO markAsInterested(Long environmentId);
}
