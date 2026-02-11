package vacancy_tracker.presentation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncomingUpdateDtoTest {

    @Test
    @DisplayName("Проверка корректного создания экземпляра IncomingUpdateDto")
    void constructor_shouldCreateIncomingUpdateDto_whenDataIsValid() {
        IncomingUpdateDto updateDto = new IncomingUpdateDto(55L, "Старт");
        assertThat(updateDto)
                .isNotNull()
                .extracting(IncomingUpdateDto::userId, IncomingUpdateDto::text)
                .containsExactly(55L, "Старт");
    }


    @Test
    @DisplayName("Проверка создания экземпляра, когда текст null")
    void text_shouldCreateIncomingUpdateDto_whenTextIsNull() {
        IncomingUpdateDto updateDto = new IncomingUpdateDto(55L, null);
        assertThat(updateDto)
                .isNotNull()
                .extracting(IncomingUpdateDto::userId, IncomingUpdateDto::text)
                .containsExactly(55L, null);
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, когда userId null")
    void userId_shouldThrowsIllegalArgumentException_whenUserIdIsNull() {
        assertThatThrownBy(() ->
                new IncomingUpdateDto(null, "Готов"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }
}