package s05.t02.service;

import org.springframework.web.multipart.MultipartFile;
import s05.t02.model.dto.EnvironmentDTO;

public interface EnvironmentFileService {

    EnvironmentDTO uploadFile(Long environmentId, MultipartFile file, String username);
    EnvironmentDTO deleteFile(Long environmentId, String username);
}
