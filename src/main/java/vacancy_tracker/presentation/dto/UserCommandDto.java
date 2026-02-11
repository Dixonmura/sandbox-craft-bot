package vacancy_tracker.presentation.dto;

/**
 * Представление класса принятой от пользователя команды для передачи диспетчеру
 */
public record UserCommandDto(Long userId, CommandType commandType, String arguments) {
}
