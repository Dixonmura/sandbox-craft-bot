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
import vacancy_tracker.bot.types.StartStopBotOption;
import vacancy_tracker.core.*;
import vacancy_tracker.data.InMemoryUserRepository;
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

    Long USER_ID = 18L;
    UserRepository repository;
    UserService userService;
    VacancyCommandDispatcher dispatcher;
    @Mock
    ScheduledNotificationService notificationService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        userService = new UserService(repository);
        dispatcher = new VacancyCommandDispatcher(userService, notificationService);
        userService.updateUserSettings(USER_ID, new UserSettings(
                65,
                3,
                80000,
                "Java developer",
                LocalTime.parse("07:00")));
    }

    @Test
    @DisplayName("Проверка диспетчера на возвращение соответствующего команде ответа")
    void commandDispatch_shouldReturnAppropriateAnswer_whenDataIsValid() {
        VacancyReply reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.START, StartStopBotOption.START_BOT.getTitle()));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, AFTER_START_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_UTC, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, ENTER_UTC_OFFSET, VacancyKeyboardKey.UTC_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_UTC)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_UTC, "+05:30"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, UPDATED_UTC_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, REGION_MESSAGE, VacancyKeyboardKey.REGION_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_REGION)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, "65"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, UPDATE_REGION_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, EXPERIENCE_MESSAGE, VacancyKeyboardKey.MIN_EXPERIENCE_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_MIN_EXPERIENCE)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, "3"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, UPDATE_EXPERIENCE_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, SALARY_MESSAGE, VacancyKeyboardKey.MIN_SALARY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_MIN_SALARY)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, "90000"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, UPDATE_SALARY_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_KEYWORD, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, KEYWORD_MESSAGE, VacancyKeyboardKey.KEY_WORD_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_KEYWORD)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_KEYWORD, "Java Developer"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, UPDATE_KEYWORD_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, NOTIFY_TIME_MESSAGE, VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_NOTIFY_TIME)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "03:00"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, UPDATE_NOTIFY_TIME_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, NOTIFY_TIME_MESSAGE, VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_NOTIFY_TIME)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "17 55"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, UPDATE_NOTIFY_TIME_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, READY_MESSAGE, VacancyKeyboardKey.READY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_READY_COMMAND)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, ReadyAction.COMPLETE.getTitle()));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, READY_START_MESSAGE, VacancyKeyboardKey.STOP_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, READY_MESSAGE, VacancyKeyboardKey.READY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_READY_COMMAND)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, ReadyAction.GO_BACK.getTitle()));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, BACK_INTO_SETTINGS_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, STOP_MESSAGE, VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_STOP_COMMAND)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ReadyAction.YES.getTitle()));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, SUCCESSFUL_STOP_MESSAGE, VacancyKeyboardKey.START_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, STOP_MESSAGE, VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_STOP_COMMAND)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ReadyAction.NO.getTitle()));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, CONTINUE_MESSAGE, VacancyKeyboardKey.STOP_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();
    }

    @Test
    @DisplayName("Проверка запуска планировщика и отмены задачи при соответствующей команде")
    void checkNotificationService_shouldRunOrCloseTask_whenCallStartOrStop() {
        userService.updateUtcOffset(USER_ID, ZoneOffset.ofHours(5));
        dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, ""));
        dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "18:00"));
        dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, ""));
        dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, ReadyAction.COMPLETE.getTitle()));
        verify(notificationService, times(1)).scheduleNotifications(any(User.class));

        dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ""));
        dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ReadyAction.YES.getTitle()));
        verify(notificationService, times(1)).cancelNotifications(USER_ID);

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Проверка диспетчера на некорректные команды")
    void commandDispatch_shouldReturnUnknownTypeMessage_whenCommandTypeIsUnknown() {

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.SET_UTC, ""));
        VacancyReply reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_UTC, "27:46"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, ERROR_UTC_MESSAGE, VacancyKeyboardKey.UTC_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_UTC)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.SET_REGION, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, "Сахалин"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, ERROR_REGION_MESSAGE, VacancyKeyboardKey.REGION_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_REGION)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, "Минимум"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, ERROR_EXPERIENCE_MESSAGE, VacancyKeyboardKey.MIN_EXPERIENCE_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_MIN_EXPERIENCE)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, "Много"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, ERROR_SALARY_MESSAGE, VacancyKeyboardKey.MIN_SALARY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_MIN_SALARY)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "Где-то в обед"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, ERROR_NOTIFY_TIME_MESSAGE, VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_NOTIFY_TIME)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.READY, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, "Ну поехали"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, ERROR_READY_MESSAGE, VacancyKeyboardKey.READY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_READY_COMMAND)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.STOP, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, "Сам не знаю"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, ERROR_STOP_MESSAGE, VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_STOP_COMMAND)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.UNKNOWN, "Привет бот"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, UNKNOWN_MESSAGE, VacancyKeyboardKey.NONE);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();
    }

    @Test
    @DisplayName("Проверка выброса исключения при создании экземпляра, когда userService null")
    void constructor_shouldThrowsIllegalArgumentException_whenUserServiceIsNull() {
        assertThatThrownBy(() ->
                new VacancyCommandDispatcher(null, notificationService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userService не может быть null");

        assertThatThrownBy(() ->
                new VacancyCommandDispatcher(userService, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("notificationService не может быть null");
    }

    @Test
    @DisplayName("Проверка выброса исключения, когда в commandDispatch подаётся null")
    void commandDispatch_shouldThrowsIllegalArgumentException_whenInputValuesInvalid() {
        assertThatThrownBy(() ->
                dispatcher.commandDispatch(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("commandDto не может быть null");

    }

    private boolean equalsCurrentSettingStateWith(UserSettingState checkingState) {
        return userService.getSettingState(USER_ID) == checkingState;
    }
}