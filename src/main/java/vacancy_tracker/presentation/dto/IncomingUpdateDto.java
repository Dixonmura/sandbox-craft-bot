package vacancy_tracker.presentation.dto;

/**
 * Представление класса, который получает нужную информацию от поступившего обновления.
 */
public record IncomingUpdateDto(Long userId, String text) {
    public IncomingUpdateDto {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
    }
}
