package s05.t02.service;

import s05.t02.model.dto.UserDTO;

public interface UserService {
    UserDTO registerUser(String username, String password, String recoveryKey);
    String authenticateUser(String username, String password);
    void recoverPassword(String username, String recoveryKey, String newPassword);
}
