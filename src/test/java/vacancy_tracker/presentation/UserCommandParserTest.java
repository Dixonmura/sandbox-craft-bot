package vacancy_tracker.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.core.UserSettingState;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.IncomingUpdateDto;
import vacancy_tracker.presentation.dto.UserCommandDto;

import static org.assertj.core.api.Assertions.assertThat;
import static vacancy_tracker.bot.CallbackPrefixes.*;
import static vacancy_tracker.bot.types.SettingOptions.*;
import static vacancy_tracker.bot.types.StartSchedulerOptions.START_SCHEDULER;
import static vacancy_tracker.bot.types.StartStopBotOption.*;

class UserCommandParserTest {

    Long USER_ID = 65L;
    UserCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new UserCommandParser();
    }

    @Test
    @DisplayName("Проверка парсинга кнопок настроек с эмодзи (SETTING_PREFIX)")
    void parse_shouldReturnUserCommandDtoWithAppropriateData_whenSettingButtonsWithEmojis() {

        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, SETTING_PREFIX + UTC_OPTION.getTitle()),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_UTC);

        dto = parser.parse(new IncomingUpdateDto(
                        USER_ID, SETTING_PREFIX + REGION.getTitle()),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_REGION);

        dto = parser.parse(new IncomingUpdateDto(
                        USER_ID, SETTING_PREFIX + MIN_EXPERIENCE.getTitle()),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_MIN_EXPERIENCE);

        dto = parser.parse(new IncomingUpdateDto(
                        USER_ID, SETTING_PREFIX + MIN_SALARY.getTitle()),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_MIN_SALARY);

        dto = parser.parse(new IncomingUpdateDto(
                        USER_ID, SETTING_PREFIX + WORD_FOR_SEARCH.getTitle()),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_KEYWORD);

        dto = parser.parse(new IncomingUpdateDto(
                        USER_ID, SETTING_PREFIX + SETTINGS_NOTIFICATION.getTitle()),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.SET_NOTIFY_TIME);
    }

    @Test
    @DisplayName("Проверка START/STOP кнопок в состоянии CLEAN")
    void parse_shouldParseStartStopButtons_whenCleanState() {
        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, START_BOT.getTitle()),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.START);

        dto = parser.parse(
                new IncomingUpdateDto(USER_ID, STOP_BOT.getTitle()),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.STOP);
    }

    @Test
    @DisplayName("Проверка префиксов с аргументами")
    void parse_shouldParsePrefixesWithArguments_whenCleanState() {
        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, REGION_CODE_PREFIX + "77"),
                UserSettingState.WAITING_SET_REGION);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_REGION, "77");

        dto = parser.parse(
                new IncomingUpdateDto(USER_ID, EXPERIENCE_PREFIX + "3"),
                UserSettingState.WAITING_SET_MIN_EXPERIENCE);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_MIN_EXPERIENCE, "3");

        dto = parser.parse(
                new IncomingUpdateDto(USER_ID, SALARY_PREFIX + "100000"),
                UserSettingState.WAITING_SET_MIN_SALARY);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_MIN_SALARY, "100000");

        dto = parser.parse(
                new IncomingUpdateDto(USER_ID, UTC_OFFSET_PREFIX + "0300"),
                UserSettingState.WAITING_SET_UTC);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_UTC, "0300");
    }

    @Test
    @DisplayName("Проверка навигации по страницам")
    void parse_shouldParsePageNavigation_whenCleanState() {
        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, REGION_PAGE_PREFIX + "1"),
                UserSettingState.WAITING_SET_REGION);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.CHANGE_REGION_PAGE, "1");

        dto = parser.parse(
                new IncomingUpdateDto(USER_ID, UTC_OFFSET_PAGE_PREFIX + "0"),
                UserSettingState.WAITING_SET_UTC);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.CHANGE_UTC_PAGE, "0");
    }

    @Test
    @DisplayName("Проверка READY команды")
    void parse_shouldParseReadyCommand_whenCompleteTitle() {
        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, START_SCHEDULER.getTitle()),
                UserSettingState.CLEAN);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType)
                .containsExactly(USER_ID, CommandType.READY);
    }

    @Test
    @DisplayName("Проверка инференса по состоянию ожидания")
    void parse_shouldInferCommandType_whenWaitingState() {
        UserCommandDto dto = parser.parse(
                new IncomingUpdateDto(USER_ID, REGION_CODE_PREFIX + "65"),
                UserSettingState.WAITING_SET_REGION);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_REGION, "65");

        dto = parser.parse(
                new IncomingUpdateDto(USER_ID, SALARY_PREFIX + "90000"),
                UserSettingState.WAITING_SET_MIN_SALARY);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_MIN_SALARY, "90000");

        dto = parser.parse(
                new IncomingUpdateDto(USER_ID, EXPERIENCE_PREFIX + "3"),
                UserSettingState.WAITING_SET_MIN_EXPERIENCE);
        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.SET_MIN_EXPERIENCE, "3");
    }

    @Test
    @DisplayName("Проверка неизвестной команды")
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
    @DisplayName("Проверка null текста")
    void parse_shouldReturnUnknownCommand_whenInputTextIsNull() {
        UserCommandDto dto = parser.parse(new IncomingUpdateDto(
                        USER_ID, null),
                UserSettingState.CLEAN);

        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.UNKNOWN, null);
    }

    @Test
    @DisplayName("Проверка пустого текста")
    void parse_shouldReturnUnknownCommand_whenInputTextIsBlank() {
        UserCommandDto dto = parser.parse(new IncomingUpdateDto(
                        USER_ID, "   "),
                UserSettingState.CLEAN);

        assertThat(dto)
                .isNotNull()
                .extracting(UserCommandDto::userId, UserCommandDto::commandType, UserCommandDto::arguments)
                .containsExactly(USER_ID, CommandType.UNKNOWN, null);
    }
}