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
import static vacancy_tracker.bot.types.StartStopBotOption.*;

class UserCommandParserTest {

    private UserCommandParser parser;
    private final Long USER_ID = 123L;

    @BeforeEach
    void setUp() {
        parser = new UserCommandParser();
    }

    private IncomingUpdateDto createDto(String text) {
        return new IncomingUpdateDto(USER_ID, text);
    }

    @Test
    @DisplayName("START команда по кнопке")
    void parse_shouldReturnStart_whenStartButtonPressed() {
        IncomingUpdateDto dto = createDto(START_BOT.getTitle());

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.START);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("STOP команда по кнопке")
    void parse_shouldReturnStop_whenStopButtonPressed() {
        IncomingUpdateDto dto = createDto(STOP_BOT.getTitle());

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.STOP);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("HOME команда по кнопке")
    void parse_shouldReturnHome_whenHomeButtonPressed() {
        IncomingUpdateDto dto = createDto(OUT_IN_ROUTER.getTitle());

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.HOME);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("SETTING: UTC команда")
    void parse_shouldReturnSetUtc_whenUtcSettingSelected() {
        IncomingUpdateDto dto = createDto(SETTING_PREFIX + "🕐 Изменить часовой пояс UTC");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_UTC);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("SETTING: REGION команда")
    void parse_shouldReturnSetRegion_whenRegionSettingSelected() {
        IncomingUpdateDto dto = createDto(SETTING_PREFIX + "🌍 Регион");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_REGION);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("SETTING: EXPERIENCE команда")
    void parse_shouldReturnSetExperience_whenExperienceSettingSelected() {
        IncomingUpdateDto dto = createDto(SETTING_PREFIX + "💼 Минимальный опыт");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_MIN_EXPERIENCE);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("SETTING: SALARY команда")
    void parse_shouldReturnSetSalary_whenSalarySettingSelected() {
        IncomingUpdateDto dto = createDto(SETTING_PREFIX + "💰 Минимальная зарплата");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_MIN_SALARY);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("SETTING: KEYWORD команда")
    void parse_shouldReturnSetKeyword_whenKeywordSettingSelected() {
        IncomingUpdateDto dto = createDto(SETTING_PREFIX + "🔍 Слово для поиска");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_KEYWORD);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("SETTING: NOTIFY TIME команда")
    void parse_shouldReturnSetNotifyTime_whenNotifySettingSelected() {
        IncomingUpdateDto dto = createDto(SETTING_PREFIX + "🔔 Настройки нотификации");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_NOTIFY_TIME);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("SETTING: READY команда")
    void parse_shouldReturnReady_whenReadySettingSelected() {
        IncomingUpdateDto dto = createDto(SETTING_PREFIX + "🚀 Запустить планировщик");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.READY);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("SETTING: неизвестная команда")
    void parse_shouldReturnUnknown_whenUnknownSettingSelected() {
        IncomingUpdateDto dto = createDto(SETTING_PREFIX + "Что-то странное");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.UNKNOWN);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("WAITING_SET_UTC: выбор со страницы")
    void parse_whenWaitingUtc_shouldHandlePageSelection() {
        IncomingUpdateDto dto = createDto(UTC_OFFSET_PAGE_PREFIX + "2");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_UTC);

        assertThat(result.commandType()).isEqualTo(CommandType.CHANGE_UTC_PAGE);
        assertThat(result.arguments()).isEqualTo("2");
    }

    @Test
    @DisplayName("WAITING_SET_UTC: выбор значения с префиксом")
    void parse_whenWaitingUtc_shouldHandlePrefixedValue() {
        IncomingUpdateDto dto = createDto(UTC_OFFSET_PREFIX + "+03:00");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_UTC);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_UTC);
        assertThat(result.arguments()).isEqualTo("+03:00");
    }

    @Test
    @DisplayName("WAITING_SET_UTC: ручной ввод")
    void parse_whenWaitingUtc_shouldHandleManualInput() {
        IncomingUpdateDto dto = createDto("+05:30");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_UTC);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_UTC);
        assertThat(result.arguments()).isEqualTo("+05:30");
    }

    @Test
    @DisplayName("WAITING_SET_REGION: выбор со страницы")
    void parse_whenWaitingRegion_shouldHandlePageSelection() {
        IncomingUpdateDto dto = createDto(REGION_PAGE_PREFIX + "1");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_REGION);

        assertThat(result.commandType()).isEqualTo(CommandType.CHANGE_REGION_PAGE);
        assertThat(result.arguments()).isEqualTo("1");
    }

    @Test
    @DisplayName("WAITING_SET_REGION: выбор кода с префиксом")
    void parse_whenWaitingRegion_shouldHandlePrefixedCode() {
        IncomingUpdateDto dto = createDto(REGION_CODE_PREFIX + "77");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_REGION);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_REGION);
        assertThat(result.arguments()).isEqualTo("77");
    }

    @Test
    @DisplayName("WAITING_SET_REGION: ручной ввод")
    void parse_whenWaitingRegion_shouldHandleManualInput() {
        IncomingUpdateDto dto = createDto("78");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_REGION);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_REGION);
        assertThat(result.arguments()).isEqualTo("78");
    }

    @Test
    @DisplayName("WAITING_SET_MIN_EXPERIENCE: выбор с префиксом")
    void parse_whenWaitingExperience_shouldHandlePrefixedValue() {
        IncomingUpdateDto dto = createDto(EXPERIENCE_PREFIX + "3");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_MIN_EXPERIENCE);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_MIN_EXPERIENCE);
        assertThat(result.arguments()).isEqualTo("3");
    }

    @Test
    @DisplayName("WAITING_SET_MIN_EXPERIENCE: ручной ввод")
    void parse_whenWaitingExperience_shouldHandleManualInput() {
        IncomingUpdateDto dto = createDto("5");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_MIN_EXPERIENCE);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_MIN_EXPERIENCE);
        assertThat(result.arguments()).isEqualTo("5");
    }

    @Test
    @DisplayName("WAITING_SET_MIN_SALARY: выбор с префиксом")
    void parse_whenWaitingSalary_shouldHandlePrefixedValue() {
        IncomingUpdateDto dto = createDto(SALARY_PREFIX + "150000");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_MIN_SALARY);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_MIN_SALARY);
        assertThat(result.arguments()).isEqualTo("150000");
    }

    @Test
    @DisplayName("WAITING_SET_MIN_SALARY: ручной ввод")
    void parse_whenWaitingSalary_shouldHandleManualInput() {
        IncomingUpdateDto dto = createDto("200000");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_MIN_SALARY);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_MIN_SALARY);
        assertThat(result.arguments()).isEqualTo("200000");
    }

    @Test
    @DisplayName("WAITING_SET_KEYWORD: выбор с префиксом")
    void parse_whenWaitingKeyword_shouldHandlePrefixedValue() {
        IncomingUpdateDto dto = createDto(KEY_WORD_PREFIX + "Java Developer");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_KEYWORD);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_KEYWORD);
        assertThat(result.arguments()).isEqualTo("Java Developer");
    }

    @Test
    @DisplayName("WAITING_SET_KEYWORD: ручной ввод")
    void parse_whenWaitingKeyword_shouldHandleManualInput() {
        IncomingUpdateDto dto = createDto("Python Developer");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_KEYWORD);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_KEYWORD);
        assertThat(result.arguments()).isEqualTo("Python Developer");
    }

    @Test
    @DisplayName("WAITING_SET_NOTIFY_TIME: выбор со страницы")
    void parse_whenWaitingNotifyTime_shouldHandlePageSelection() {
        IncomingUpdateDto dto = createDto(NOTIFY_PAGE_PREFIX + "2");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_NOTIFY_TIME);

        assertThat(result.commandType()).isEqualTo(CommandType.CHANGE_NOTIFY_PAGE);
        assertThat(result.arguments()).isEqualTo("2");
    }

    @Test
    @DisplayName("WAITING_SET_NOTIFY_TIME: выбор с префиксом")
    void parse_whenWaitingNotifyTime_shouldHandlePrefixedValue() {
        IncomingUpdateDto dto = createDto(NOTIFY_TIME_PREFIX + "09:00");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_NOTIFY_TIME);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_NOTIFY_TIME);
        assertThat(result.arguments()).isEqualTo("09:00");
    }

    @Test
    @DisplayName("WAITING_SET_NOTIFY_TIME: ручной ввод")
    void parse_whenWaitingNotifyTime_shouldHandleManualInput() {
        IncomingUpdateDto dto = createDto("10:30");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_SET_NOTIFY_TIME);

        assertThat(result.commandType()).isEqualTo(CommandType.SET_NOTIFY_TIME);
        assertThat(result.arguments()).isEqualTo("10:30");
    }

    @Test
    @DisplayName("WAITING_READY_COMMAND: выбор действия")
    void parse_whenWaitingReady_shouldHandleChoice() {
        IncomingUpdateDto dto = createDto(READY_PREFIX + "COMPLETE");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_READY_COMMAND);

        assertThat(result.commandType()).isEqualTo(CommandType.READY);
        assertThat(result.arguments()).isEqualTo("COMPLETE");
    }

    @Test
    @DisplayName("WAITING_STOP_COMMAND: подтверждение")
    void parse_whenWaitingStop_shouldHandleConfirmation() {
        IncomingUpdateDto dto = createDto(YES_OR_NO_PREFIX + "YES");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_STOP_COMMAND);

        assertThat(result.commandType()).isEqualTo(CommandType.STOP);
        assertThat(result.arguments()).isEqualTo("YES");
    }

    @Test
    @DisplayName("WAITING_HOME_COMMAND: подтверждение")
    void parse_whenWaitingHome_shouldHandleConfirmation() {
        IncomingUpdateDto dto = createDto(YES_OR_NO_PREFIX + "YES");

        UserCommandDto result = parser.parse(dto, UserSettingState.WAITING_HOME_COMMAND);

        assertThat(result.commandType()).isEqualTo(CommandType.HOME);
        assertThat(result.arguments()).isEqualTo("YES");
    }

    @Test
    @DisplayName("Пустой текст возвращает UNKNOWN")
    void parse_shouldReturnUnknown_whenTextIsBlank() {
        IncomingUpdateDto dto = createDto("   ");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.UNKNOWN);
        assertThat(result.arguments()).isNull();
    }

    @Test
    @DisplayName("Неизвестный текст в чистом состоянии возвращает UNKNOWN")
    void parse_shouldReturnUnknown_whenUnknownTextInCleanState() {
        IncomingUpdateDto dto = createDto("какой-то левый текст");

        UserCommandDto result = parser.parse(dto, UserSettingState.CLEAN);

        assertThat(result.commandType()).isEqualTo(CommandType.UNKNOWN);
        assertThat(result.arguments()).isEqualTo("какой-то левый текст");
    }
}