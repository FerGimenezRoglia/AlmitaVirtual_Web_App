package s05.t02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s05.t02.model.Environment;

import java.util.List;

public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    List<Environment> findByUserId(Long userId);
}
