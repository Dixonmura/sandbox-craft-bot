package vacancy_tracker.presentation.dto;

/**
 * Представление класса, который получает нужную информацию от поступившего обновления.
 */
public record IncomingUpdateDto(Long userId, String text) {
}
