package s05.t02.service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import s05.t02.exception.custom.InvalidCredentialsException;
import s05.t02.exception.custom.UsernameAlreadyExistsException;
import s05.t02.model.User;
import s05.t02.model.dto.UserDTO;
import s05.t02.model.enums.UserRole;
import s05.t02.repository.UserRepository;
import s05.t02.security.jwt.JwtUtil;
import s05.t02.service.UserService;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registra un nuevo usuario con el rol por defecto ROLE_USER.
     * Valida si el username ya existe, encripta la contraseña y guarda el usuario.
     */
    @Override
    public UserDTO registerUser(String username, String password, String recoveryKey) {
        log.info("Attempting to register new user");

        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: username already exists");
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);
        String hashedRecoveryKey = passwordEncoder.encode(recoveryKey);

        User user = new User(username, hashedPassword, hashedRecoveryKey, UserRole.ROLE_USER);
        user = userRepository.save(user);

        log.info("User registered successfully");
        return new UserDTO(user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * Autentica al usuario con username y password.
     * Si es válido, devuelve un token JWT.
     */
    @Override
    public String authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: invalid credentials");
                    return new InvalidCredentialsException("Invalid username or password");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed: invalid credentials");
            throw new InvalidCredentialsException("Invalid username or password");
        }

        log.info("User authenticated successfully");
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    /**
     * Recupera la contraseña de un usuario validando la clave secreta.
     * Si es válida, actualiza la contraseña por una nueva.
     */
    @Override
    public void recoverPassword(String username, String recoveryKey, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Password recovery failed: invalid credentials");
                    return new InvalidCredentialsException("Invalid username or recovery key");
                });

        if (!passwordEncoder.matches(recoveryKey, user.getRecoveryKey())) {
            log.warn("Password recovery failed: invalid credentials");
            throw new InvalidCredentialsException("Invalid username or recovery key");
        }

        String hashedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedNewPassword);
        userRepository.save(user);

        log.info("Password updated successfully");
    }

}