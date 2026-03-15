package vacancy_tracker.presentation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCommandDtoTest {

    @Test
    @DisplayName("Проверка корректного создания экземпляра UserCommandDto")
    void constructor_shouldCreateUserCommandDto_whenDataIsValid() {
        UserCommandDto dto = new UserCommandDto(55L, CommandType.START, "Старт");
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(55L, CommandType.START, "Старт");
    }

    @Test
    @DisplayName("Проверка создания экземпляра, когда аргумент null")
    void arguments_shouldCreateUserCommandDto_whenArgumentsIsNull() {
        UserCommandDto dto = new UserCommandDto(55L, CommandType.START, null);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(55L, CommandType.START, null);
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, когда userId null")
    void userId_shouldThrowsIllegalArgumentException_whenUserIdIsNull() {
        assertThatThrownBy(() ->
                new UserCommandDto(null, CommandType.READY, "Готов"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, когда CommandType null")
    void commandType_shouldThrowsIllegalArgumentException_whenCommandTypeIsNull() {
        assertThatThrownBy(() ->
                new UserCommandDto(77L, null, "Готов"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("commandType не может быть null");
    }
}