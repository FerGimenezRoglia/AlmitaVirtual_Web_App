package s05.t02.service;

import s05.t02.model.dto.EnvironmentDTO;

public interface EnvironmentFileService {

    EnvironmentDTO uploadFile(Long environmentId, String fileUrl, String username);
    EnvironmentDTO deleteFile(Long environmentId, String username);
}
