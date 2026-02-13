package vacancy_tracker.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.core.UserSettingState;
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

        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, "Изменить часовой пояс UTC"),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_UTC);

        dto = parser.parse(new IncomingUpdateDto(
                USER_ID, "Регион"),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_REGION);

        dto = parser.parse(new IncomingUpdateDto(
                USER_ID, "Опыт"),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_MIN_EXPERIENCE);

        dto = parser.parse(new IncomingUpdateDto(
                USER_ID, "Минимальная зарплата"),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_MIN_SALARY);

        dto = parser.parse(new IncomingUpdateDto(
                USER_ID, "Ключевое слово"),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_KEYWORD);

        dto = parser.parse(new IncomingUpdateDto(
                USER_ID, "Уведомление"),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_NOTIFY_TIME);

        dto = parser.parse(new IncomingUpdateDto(
                USER_ID, "Готово"),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.READY);

        dto = parser.parse(new IncomingUpdateDto(
                USER_ID, "Стоп"),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.STOP);
    }

    @Test
    @DisplayName("Во время ожидания региона любой текст парсится как SET_REGION с аргументом")
    void parse_shouldInferSetRegion_whenStateWaitingSetRegion() {
        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, "65"),
                UserSettingState.WAITING_SET_REGION
        );

        assertThat(dto)
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_REGION, "65");
    }

    @Test
    @DisplayName("Во время ожидания минимальной зарплаты любой текст парсится как SET_MIN_SALARY с аргументом")
    void parse_shouldInferSetMinSalary_whenStateWaitingSetMinSalary() {
        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, "90000"),
                UserSettingState.WAITING_SET_MIN_SALARY
        );

        assertThat(dto)
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_MIN_SALARY, "90000");
    }

    @Test
    @DisplayName("Во время ожидания минимального опыта любой текст парсится как SET_MIN_EXPERIENCE с аргументом")
    void parse_shouldInferSetMinExperience_whenStateWaitingSetMinExperience() {
        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, "3"),
                UserSettingState.WAITING_SET_MIN_EXPERIENCE
        );

        assertThat(dto)
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_MIN_EXPERIENCE, "3");
    }

    @Test
    @DisplayName("Проверка возвращения UserCommandDto, когда текст не соответствует команде")
    void parse_shouldReturnUserCommandDtoWithAppropriateData_whenInputTextInvalid() {
        UserCommandDto dto = parser.parse(new IncomingUpdateDto(
                USER_ID, "Привет"),
                UserSettingState.CLEAN);

        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.UNKNOWN, "Привет");
    }

    @Test
    @DisplayName("Проверка возвращения UNKNOWN, когда текст null")
    void parse_shouldReturnUnknownCommand_whenInputTextIsNull() {
        UserCommandDto dto = parser.parse(new IncomingUpdateDto(
                USER_ID, null),
                UserSettingState.CLEAN);

        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.UNKNOWN, null);
    }
}