package s05.t02.service.logic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import s05.t02.model.Environment;
import s05.t02.model.enums.EnvironmentStatus;
import s05.t02.model.enums.InteractionType;

@Slf4j
@Component
public class EnvironmentStatusResolver {

    /**
     * Actualiza el estado del entorno según el tipo de interacción recibida.
     *
     * @param environment     El entorno a modificar.
     * @param interactionType El tipo de interacción realizada.
     */
    public static void updateStatus(Environment environment, InteractionType interactionType) {
        switch (interactionType) {
            case FILE_UPLOAD -> {
                environment.setStatus(EnvironmentStatus.ACTIVE);
                log.debug("Environment status updated to 'ACTIVE' due to FILE_UPLOAD");
            }
            case FILE_DELETE -> {
                environment.setStatus(EnvironmentStatus.REFLECTIVE);
                log.debug("Environment status updated to 'REFLECTIVE' due to FILE_DELETE");
            }
            case FILE_VIEW -> {
                environment.setStatus(EnvironmentStatus.EXCITED);
                log.debug("Environment status updated to 'EXCITED' due to FILE_VIEW");
            }
            case MARK_AS_INTERESTED -> {
                environment.setStatus(EnvironmentStatus.INSPIRED);
                log.debug("Environment status updated to 'INSPIRED' due to MARK_AS_INTERESTED");
            }
            default -> log.warn("Unknown interaction type: {}", interactionType);
        }
    }
}