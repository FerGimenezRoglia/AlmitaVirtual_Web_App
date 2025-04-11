package s05.t02.model.enums;

/**
 * Enum que define los tipos de interacciones que pueden afectar el estado de un entorno(Environment).
 * Se utilizan para centralizar la lógica de actualización del estado del entorno(EnvironmentStatus) tras las acciones del usuario.
 */

public enum InteractionType {
    FILE_UPLOAD,
    FILE_DELETE,
    FILE_VIEW,
    MARK_AS_INTERESTED
}