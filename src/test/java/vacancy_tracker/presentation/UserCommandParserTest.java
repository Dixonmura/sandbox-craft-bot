package vacancy_tracker.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.IncomingUpdateDto;
import vacancy_tracker.presentation.dto.UserCommandDto;

import static org.assertj.core.api.Assertions.assertThat;

class UserCommandParserTest {

    Long USER_ID = 65L;
    UserCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new UserCommandParser();
    }

    @Test
    @DisplayName("Проверка возвращения UserCommandDto с корректными данными")
    void parse_shouldReturnUserCommandDtoWithAppropriateData_whenDataIsValid() {
        UserCommandDto dto = parser.parse(new IncomingUpdateDto(USER_ID, "/start"));
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.START);

        dto = parser.parse(new IncomingUpdateDto(USER_ID, "Изменить часовой пояс UTC"));
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_UTC);

        dto = parser.parse(new IncomingUpdateDto(USER_ID, "Регион"));
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_REGION);

        dto = parser.parse(new IncomingUpdateDto(USER_ID, "Опыт"));
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_MIN_EXPERIENCE);

        dto = parser.parse(new IncomingUpdateDto(USER_ID, "Минимальная зарплата"));
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_MIN_SALARY);

        dto = parser.parse(new IncomingUpdateDto(USER_ID, "Ключевое слово"));
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_KEYWORD);

        dto = parser.parse(new IncomingUpdateDto(USER_ID, "Уведомление"));
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_NOTIFY_TIME);

        dto = parser.parse(new IncomingUpdateDto(USER_ID, "Готово"));
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.READY);

        dto = parser.parse(new IncomingUpdateDto(USER_ID, "Стоп"));
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.STOP);
    }

    @Test
    @DisplayName("Проверка возвращения UserCommandDto, когда текст не соответствует команде")
    void parse_shouldReturnUserCommandDtoWithAppropriateData_whenInputTextInvalid() {
        UserCommandDto dto = parser.parse(new IncomingUpdateDto(USER_ID, "Привет"));

        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.UNKNOWN);
    }

    @Test
    @DisplayName("Проверка возвращения UNKNOWN, когда текст null")
    void parse_shouldReturnUnknownCommand_whenInputTextIsNull() {
        UserCommandDto dto = parser.parse(new IncomingUpdateDto(USER_ID, null));

        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.UNKNOWN, null);
    }
}