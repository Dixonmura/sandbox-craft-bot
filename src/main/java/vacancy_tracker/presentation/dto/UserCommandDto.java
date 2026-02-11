package vacancy_tracker.presentation.dto;

/**
 * Представление класса принятой от пользователя команды для передачи диспетчеру
 */
public record UserCommandDto(Long userId, CommandType commandType, String arguments) {
    public UserCommandDto {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        if (commandType == null) {
            throw new IllegalArgumentException("commandType не может быть null");
        }
    }
}
