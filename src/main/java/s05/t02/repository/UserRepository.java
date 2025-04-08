package s05.t02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s05.t02.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}

