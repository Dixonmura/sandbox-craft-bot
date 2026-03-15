package vacancy_tracker.presentation;

import markups.VacancyKeyboardKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vacancy_tracker.bot.VacancyReply;
import vacancy_tracker.bot.types.ReadyAction;
import vacancy_tracker.core.*;
import vacancy_tracker.data.json.JsonSessionStateRepository;
import vacancy_tracker.data.json.JsonUserRepository;
import vacancy_tracker.data.repository.SessionStateRepository;
import vacancy_tracker.data.repository.UserRepository;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.UserCommandDto;

import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static vacancy_tracker.bot.VacancyMessages.*;

@ExtendWith(MockitoExtension.class)
class VacancyCommandDispatcherTest {

    private final Long USER_ID = 18L;

    private UserRepository userRepository;
    private UserService userService;
    private VacancyCommandDispatcher dispatcher;

    @Mock
    private ScheduledNotificationService notificationService;

    @BeforeEach
    void setUp() {
        userRepository = new JsonUserRepository();
        SessionStateRepository sessionStateRepository = new JsonSessionStateRepository();
        userService = new UserService(userRepository, sessionStateRepository);
        dispatcher = new VacancyCommandDispatcher(userService, notificationService);

        UserSettings settings = new UserSettings(
                65,
                3,
                80000,
                "Java developer",
                LocalTime.parse("07:00")
        );
        userService.updateUserSettings(USER_ID, settings);
        userService.updateUtcOffset(USER_ID, ZoneOffset.ofHours(3));
        userService.updateSettingState(USER_ID, UserSettingState.CLEAN);
    }

    private UserCommandDto createCommand(CommandType type, String args) {
        return new UserCommandDto(USER_ID, type, args);
    }

    @Test
    @DisplayName("START: устанавливает состояние CLEAN и клавиатуру настроек")
    void start_shouldSetCleanStateAndSettingsKeyboard() {
        VacancyReply reply = dispatcher.commandDispatch(createCommand(CommandType.START, ""));

        assertThat(reply.userId()).isEqualTo(USER_ID);
        assertThat(reply.text()).isEqualTo(AFTER_START_MESSAGE);
        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.CLEAN);
        assertThat(userService.getStateSessionOrDefault(USER_ID))
                .isEqualTo(VacancySessionState.CONFIGURING);
    }

    @Test
    @DisplayName("SET_UTC: при CLEAN состоянии переводит в WAITING_SET_UTC")
    void setUtc_whenClean_shouldSetWaitingState() {
        VacancyReply reply = dispatcher.commandDispatch(createCommand(CommandType.SET_UTC, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.UTC_KEYBOARD);
        assertThat(reply.text()).isEqualTo(ENTER_UTC_OFFSET);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.WAITING_SET_UTC);
    }

    @Test
    @DisplayName("SET_UTC: с валидным аргументом обновляет часовой пояс")
    void setUtc_withValidArg_shouldUpdateOffset() {

        dispatcher.commandDispatch(createCommand(CommandType.SET_UTC, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_UTC, "+05:30"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(reply.text()).isEqualTo(UPDATED_UTC_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.CLEAN);
        assertThat(userService.getOrCreateUser(USER_ID).getUtcOffset())
                .isEqualTo(ZoneOffset.of("+05:30"));
    }

    @Test
    @DisplayName("SET_UTC: с невалидным аргументом показывает ошибку")
    void setUtc_withInvalidArg_shouldShowError() {
        dispatcher.commandDispatch(createCommand(CommandType.SET_UTC, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_UTC, "27:46"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.UTC_KEYBOARD);
        assertThat(reply.text()).isEqualTo(ERROR_UTC_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.WAITING_SET_UTC);
    }

    @Test
    @DisplayName("CHANGE_UTC_PAGE: передаёт номер страницы")
    void changeUtcPage_shouldPassPageNumber() {
        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.CHANGE_UTC_PAGE, "2"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.UTC_KEYBOARD);
        assertThat(reply.text()).isEqualTo("2");
    }

    @Test
    @DisplayName("SET_REGION: при CLEAN состоянии переводит в WAITING_SET_REGION")
    void setRegion_whenClean_shouldSetWaitingState() {
        VacancyReply reply = dispatcher.commandDispatch(createCommand(CommandType.SET_REGION, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.REGION_KEYBOARD);
        assertThat(reply.text()).isEqualTo(REGION_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.WAITING_SET_REGION);
    }

    @Test
    @DisplayName("SET_REGION: с валидным аргументом обновляет регион")
    void setRegion_withValidArg_shouldUpdateRegion() {
        dispatcher.commandDispatch(createCommand(CommandType.SET_REGION, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_REGION, "77"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(reply.text()).isEqualTo(UPDATE_REGION_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.CLEAN);
        assertThat(userService.getOrCreateUser(USER_ID).getSettings().getRegionCode())
                .isEqualTo(77);
    }

    @Test
    @DisplayName("SET_REGION: с невалидным аргументом показывает ошибку")
    void setRegion_withInvalidArg_shouldShowError() {
        dispatcher.commandDispatch(createCommand(CommandType.SET_REGION, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_REGION, "Москва"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.REGION_KEYBOARD);
        assertThat(reply.text()).isEqualTo(ERROR_REGION_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.WAITING_SET_REGION);
    }

    @Test
    @DisplayName("CHANGE_REGION_PAGE: передаёт номер страницы")
    void changeRegionPage_shouldPassPageNumber() {
        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.CHANGE_REGION_PAGE, "1"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.REGION_KEYBOARD);
        assertThat(reply.text()).isEqualTo("1");
    }

    @Test
    @DisplayName("SET_MIN_EXPERIENCE: при CLEAN состоянии переводит в WAITING")
    void setExperience_whenClean_shouldSetWaitingState() {
        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_MIN_EXPERIENCE, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.MIN_EXPERIENCE_KEYBOARD);
        assertThat(reply.text()).isEqualTo(EXPERIENCE_MESSAGE);
        assertThat(userService.getSettingState(USER_ID))
                .isEqualTo(UserSettingState.WAITING_SET_MIN_EXPERIENCE);
    }

    @Test
    @DisplayName("SET_MIN_EXPERIENCE: с валидным аргументом обновляет опыт")
    void setExperience_withValidArg_shouldUpdateExperience() {
        dispatcher.commandDispatch(createCommand(CommandType.SET_MIN_EXPERIENCE, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_MIN_EXPERIENCE, "5"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(reply.text()).isEqualTo(UPDATE_EXPERIENCE_MESSAGE);
        assertThat(userService.getOrCreateUser(USER_ID).getSettings().getExperienceFrom())
                .isEqualTo(5);
    }

    @Test
    @DisplayName("SET_MIN_SALARY: при CLEAN состоянии переводит в WAITING")
    void setSalary_whenClean_shouldSetWaitingState() {
        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_MIN_SALARY, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.MIN_SALARY_KEYBOARD);
        assertThat(reply.text()).isEqualTo(SALARY_MESSAGE);
        assertThat(userService.getSettingState(USER_ID))
                .isEqualTo(UserSettingState.WAITING_SET_MIN_SALARY);
    }

    @Test
    @DisplayName("SET_MIN_SALARY: с валидным аргументом обновляет зарплату")
    void setSalary_withValidArg_shouldUpdateSalary() {
        dispatcher.commandDispatch(createCommand(CommandType.SET_MIN_SALARY, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_MIN_SALARY, "150000"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(reply.text()).isEqualTo(UPDATE_SALARY_MESSAGE);
        assertThat(userService.getOrCreateUser(USER_ID).getSettings().getSalaryFrom())
                .isEqualTo(150000);
    }

    @Test
    @DisplayName("SET_KEYWORD: при CLEAN состоянии переводит в WAITING")
    void setKeyword_whenClean_shouldSetWaitingState() {
        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_KEYWORD, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.KEY_WORD_KEYBOARD);
        assertThat(reply.text()).isEqualTo(KEYWORD_MESSAGE);
        assertThat(userService.getSettingState(USER_ID))
                .isEqualTo(UserSettingState.WAITING_SET_KEYWORD);
    }

    @Test
    @DisplayName("SET_KEYWORD: с аргументом обновляет ключевое слово")
    void setKeyword_withArg_shouldUpdateKeyword() {
        dispatcher.commandDispatch(createCommand(CommandType.SET_KEYWORD, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_KEYWORD, "Python Developer"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(reply.text()).isEqualTo(UPDATE_KEYWORD_MESSAGE);
        assertThat(userService.getOrCreateUser(USER_ID).getSettings().getWordForSearch())
                .isEqualTo("Python Developer");
    }

    @Test
    @DisplayName("SET_NOTIFY_TIME: при CLEAN состоянии переводит в WAITING")
    void setNotifyTime_whenClean_shouldSetWaitingState() {
        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_NOTIFY_TIME, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
        assertThat(reply.text()).isEqualTo(NOTIFY_TIME_MESSAGE);
        assertThat(userService.getSettingState(USER_ID))
                .isEqualTo(UserSettingState.WAITING_SET_NOTIFY_TIME);
    }

    @Test
    @DisplayName("SET_NOTIFY_TIME: с валидным временем HH:MM обновляет время")
    void setNotifyTime_withValidTime_shouldUpdateTime() {
        dispatcher.commandDispatch(createCommand(CommandType.SET_NOTIFY_TIME, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_NOTIFY_TIME, "14:30"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(reply.text()).isEqualTo(UPDATE_NOTIFY_TIME_MESSAGE);
        assertThat(userService.getOrCreateUser(USER_ID).getSettings().getNotificationTime())
                .isEqualTo(LocalTime.parse("14:30"));
    }

    @Test
    @DisplayName("SET_NOTIFY_TIME: с валидным временем H M обновляет время")
    void setNotifyTime_withValidTimeWithSpace_shouldUpdateTime() {
        dispatcher.commandDispatch(createCommand(CommandType.SET_NOTIFY_TIME, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_NOTIFY_TIME, "14 30"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(reply.text()).isEqualTo(UPDATE_NOTIFY_TIME_MESSAGE);
        assertThat(userService.getOrCreateUser(USER_ID).getSettings().getNotificationTime())
                .isEqualTo(LocalTime.parse("14:30"));
    }

    @Test
    @DisplayName("SET_NOTIFY_TIME: с невалидным временем показывает ошибку")
    void setNotifyTime_withInvalidTime_shouldShowError() {
        dispatcher.commandDispatch(createCommand(CommandType.SET_NOTIFY_TIME, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.SET_NOTIFY_TIME, "полдень"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
        assertThat(reply.text()).isEqualTo(ERROR_NOTIFY_TIME_MESSAGE);
    }

    @Test
    @DisplayName("CHANGE_NOTIFY_PAGE: передаёт номер страницы")
    void changeNotifyPage_shouldPassPageNumber() {
        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.CHANGE_NOTIFY_PAGE, "3"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
        assertThat(reply.text()).isEqualTo("3");
    }

    @Test
    @DisplayName("READY: при CLEAN и готовых настройках переводит в WAITING_READY")
    void ready_whenCleanAndSettingsReady_shouldSetWaitingReady() {
        VacancyReply reply = dispatcher.commandDispatch(createCommand(CommandType.READY, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.READY_KEYBOARD);
        assertThat(reply.text()).isEqualTo(READY_MESSAGE);
        assertThat(userService.getSettingState(USER_ID))
                .isEqualTo(UserSettingState.WAITING_READY_COMMAND);
    }

    @Test
    @DisplayName("READY: с COMPLETE запускает планировщик")
    void ready_withComplete_shouldStartScheduler() {
        dispatcher.commandDispatch(createCommand(CommandType.READY, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.READY, ReadyAction.COMPLETE.getTitle()));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.STOP_KEYBOARD);
        assertThat(reply.text()).isEqualTo(READY_START_MESSAGE);
        assertThat(userService.getStateSessionOrDefault(USER_ID))
                .isEqualTo(VacancySessionState.ACTIVE);

        verify(notificationService).scheduleNotifications(any(User.class));
    }

    @Test
    @DisplayName("READY: с GO_BACK возвращает в настройки")
    void ready_withGoBack_shouldReturnToSettings() {
        dispatcher.commandDispatch(createCommand(CommandType.READY, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.READY, ReadyAction.GO_BACK.getTitle()));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(reply.text()).isEqualTo(BACK_INTO_SETTINGS_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.CLEAN);
    }

    @Test
    @DisplayName("STOP: при CLEAN переводит в WAITING_STOP")
    void stop_whenClean_shouldSetWaitingStop() {
        VacancyReply reply = dispatcher.commandDispatch(createCommand(CommandType.STOP, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(reply.text()).isEqualTo(STOP_MESSAGE);
        assertThat(userService.getSettingState(USER_ID))
                .isEqualTo(UserSettingState.WAITING_STOP_COMMAND);
    }

    @Test
    @DisplayName("STOP: с YES удаляет пользователя")
    void stop_withYes_shouldDeleteUser() {
        dispatcher.commandDispatch(createCommand(CommandType.STOP, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.STOP, ReadyAction.YES.getTitle()));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.START_KEYBOARD);
        assertThat(reply.text()).isEqualTo(SUCCESSFUL_STOP_MESSAGE);
        verify(notificationService).cancelNotifications(USER_ID);
        assertThat(userRepository.findById(USER_ID)).isEmpty();
    }

    @Test
    @DisplayName("STOP: с NO возвращает в активное состояние")
    void stop_withNo_shouldReturnToActive() {
        dispatcher.commandDispatch(createCommand(CommandType.STOP, ""));

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.STOP, ReadyAction.NO.getTitle()));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.STOP_KEYBOARD);
        assertThat(reply.text()).isEqualTo(CONTINUE_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.CLEAN);
    }

    @Test
    @DisplayName("HOME: при CLEAN и CONFIGURING переводит в WAITING_HOME")
    void home_whenCleanAndConfiguring_shouldSetWaitingHome() {
        userService.setStateSession(USER_ID, VacancySessionState.CONFIGURING);

        VacancyReply reply = dispatcher.commandDispatch(createCommand(CommandType.HOME, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(reply.text()).isEqualTo(HOME_MESSAGE_WHEN_CONFIGURING);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.WAITING_HOME_COMMAND);
    }

    @Test
    @DisplayName("HOME: при CLEAN и ACTIVE отправляет сообщение без изменения состояния")
    void home_whenCleanAndActive_shouldSendMessageWithoutStateChange() {
        userService.setStateSession(USER_ID, VacancySessionState.ACTIVE);
        UserSettingState stateBefore = userService.getSettingState(USER_ID);

        VacancyReply reply = dispatcher.commandDispatch(createCommand(CommandType.HOME, ""));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.ROUTER_MENU_KEYBOARD);
        assertThat(reply.text()).isEqualTo(HOME_MESSAGE_WHEN_ACTIVE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(stateBefore);
    }

    @Test
    @DisplayName("HOME: с YES удаляет пользователя и возвращает в главное меню")
    void home_withYes_shouldDeleteUserAndReturnToMainMenu() {

        userService.updateSettingState(USER_ID, UserSettingState.WAITING_HOME_COMMAND);

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.HOME, ReadyAction.YES.getTitle()));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.ROUTER_MENU_KEYBOARD);
        assertThat(reply.text()).isEqualTo(SUCCESSFUL_HOME_MESSAGE);
        assertThat(userService.getStateSessionOrDefault(USER_ID)).isEqualTo(VacancySessionState.INACTIVE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.NOT_INITIALIZED);
        assertThat(userRepository.findById(USER_ID)).isEmpty();
        verify(notificationService).cancelNotifications(USER_ID);
    }

    @Test
    @DisplayName("HOME: с NO возвращает в настройки")
    void home_withNo_shouldReturnToSettings() {

        userService.updateSettingState(USER_ID, UserSettingState.WAITING_HOME_COMMAND);

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.HOME, ReadyAction.NO.getTitle()));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(reply.text()).isEqualTo(CONTINUE_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.CLEAN);
        verify(notificationService, never()).cancelNotifications(any());
    }

    @Test
    @DisplayName("HOME: с неизвестным аргументом показывает ошибку")
    void home_withUnknownArg_shouldShowError() {

        userService.updateSettingState(USER_ID, UserSettingState.WAITING_HOME_COMMAND);

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.HOME, "что-то странное"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(reply.text()).isEqualTo(ERROR_STOP_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.WAITING_HOME_COMMAND);
    }

    @Test
    @DisplayName("HOME: с неизвестным аргументом в CLEAN/INACTIVE возвращает ошибку")
    void home_withUnknownArg_whenCleanAndInactive_shouldReturnError() {
        userService.updateSettingState(USER_ID, UserSettingState.CLEAN);
        userService.setStateSession(USER_ID, VacancySessionState.INACTIVE);

        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.HOME, "что-то странное"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(reply.text()).isEqualTo(ERROR_STOP_MESSAGE);
        assertThat(userService.getSettingState(USER_ID)).isEqualTo(UserSettingState.CLEAN);
    }

    @Test
    @DisplayName("UNKNOWN: возвращает сообщение о неизвестной команде")
    void unknown_shouldReturnUnknownMessage() {
        VacancyReply reply = dispatcher.commandDispatch(
                createCommand(CommandType.UNKNOWN, "что-то странное"));

        assertThat(reply.keyboardKey()).isEqualTo(VacancyKeyboardKey.NONE);
        assertThat(reply.text()).isEqualTo(UNKNOWN_MESSAGE);
    }

    @Test
    @DisplayName("Конструктор бросает исключение при null параметрах")
    void constructor_shouldThrowException_whenParamsNull() {
        assertThatThrownBy(() -> new VacancyCommandDispatcher(null, notificationService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userService не может быть null");

        assertThatThrownBy(() -> new VacancyCommandDispatcher(userService, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("notificationService не может быть null");
    }

    @Test
    @DisplayName("commandDispatch бросает исключение при null commandDto")
    void commandDispatch_shouldThrowException_whenCommandDtoNull() {
        assertThatThrownBy(() -> dispatcher.commandDispatch(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("commandDto не может быть null");
    }
}