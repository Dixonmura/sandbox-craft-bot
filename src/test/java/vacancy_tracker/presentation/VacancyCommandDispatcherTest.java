package vacancy_tracker.presentation;

import markups.VacancyKeyboardKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vacancy_tracker.bot.VacancyReply;
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
                new UserCommandDto(USER_ID, CommandType.START, "Старт"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Vacancy tracker bot приветствует Вас!
                                Для удобства, в боте будет использоваться часовой пояс UTC""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_UTC, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Введите смещение часового пояса в формате UTC.
                                Вот пример: +07:00 или -11:30""",
                        VacancyKeyboardKey.UTC_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_UTC)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_UTC, "+05:30"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Смещение часового пояса для пользователя обновлено""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Выберите регион из списка или введите номер региона в виде целого числа""",
                        VacancyKeyboardKey.REGION_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_REGION)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, "65"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Регион для поиска вакансий обновлён""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Введите минимальный опыт работы в виде целого числа (лет).
                                Например: 5""",
                        VacancyKeyboardKey.MIN_EXPERIENCE_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_MIN_EXPERIENCE)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, "3"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Минимальный опыт работы для поиска вакансий обновлён""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Введите минимальную ожидаемую заработную плату в виде целого числа.
                                Например: 70000""",
                        VacancyKeyboardKey.MIN_SALARY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_MIN_SALARY)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, "90000"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Минимальная ожидаемая зарплата для поиска вакансий обновлёна""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_KEYWORD, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Введите ключевое слово для поиска соответствующих вакансий.
                                Например: Java Developer""",
                        VacancyKeyboardKey.NONE);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_KEYWORD)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_KEYWORD, "Java Developer"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Ключевое слово для поиска соответствующих вакансий обновлёно""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Введите время нотификации (это обязательное поле).
                                Например: 13:35 или 18 55""",
                        VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_NOTIFY_TIME)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "03:00"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Время нотификации для ежедневного оповещения обновлёно""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Введите время нотификации (это обязательное поле).
                                Например: 13:35 или 18 55""",
                        VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_NOTIFY_TIME)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "17 55"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Время нотификации для ежедневного оповещения обновлёно""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Настройки завершены, для старта планировщика уведомлений нажмите кнопку "Начать"
                                или вернитесь в меню настроек.
                                """,
                        VacancyKeyboardKey.READY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_READY_COMMAND)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, "Начать"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Планировщик запущен! Удачного поиска и до встречи! =)""",
                        VacancyKeyboardKey.STOP_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Настройки завершены, для старта планировщика уведомлений нажмите кнопку "Начать"
                                или вернитесь в меню настроек.
                                """,
                        VacancyKeyboardKey.READY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_READY_COMMAND)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, "Вернуться"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Возврат в главное меню настроек.""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Вы уверены, что хотите остановить работу бота и удалить данные поиска? Введите: Да или Нет
                                """,
                        VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_STOP_COMMAND)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, "Да"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Планирование завершено, все данные удалены, было приятно работать!
                                Надеемся было полезно и продуктивно! Возвращайтесь! =)""",
                        VacancyKeyboardKey.START_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.CLEAN)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Вы уверены, что хотите остановить работу бота и удалить данные поиска? Введите: Да или Нет
                                """,
                        VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_STOP_COMMAND)).isTrue();

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, "Нет"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Благодарим, что продолжаете пользоваться VacancyTrackerBot =)""",
                        VacancyKeyboardKey.STOP_KEYBOARD);
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
                new UserCommandDto(USER_ID, CommandType.READY, "Начать"));
        verify(notificationService, times(1)).scheduleNotifications(any(User.class));

        dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ""));
        dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, "Да"));
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
                .containsExactly(USER_ID, """
                                Неверный формат ввода зоны времени UTC. Ожидается: UTC+3, UTC+3:30, UTC-5""",
                        VacancyKeyboardKey.UTC_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_UTC)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.SET_REGION, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, "Сахалин"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Неверный формат ввода региона, введите целое число, например: 65, 77, 05""",
                        VacancyKeyboardKey.REGION_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_REGION)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, "Минимум"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Неверный формат ввода минимального опыта работы, ожидается например: 1, 3, 5""",
                        VacancyKeyboardKey.MIN_EXPERIENCE_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_MIN_EXPERIENCE)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, "Много"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Неверный формат ввода минимальной заработной платы,
                                ожидается например: 70000 или 90000""",
                        VacancyKeyboardKey.MIN_SALARY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_MIN_SALARY)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "Где-то в обед"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                неверный формат времени нотификации,
                                ожидается например: 17:00 или 02 33""",
                        VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_SET_NOTIFY_TIME)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.READY, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, "Ну поехали"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Похоже был введён некорректный ответ, для старта нажмите "Начать"
                                """,
                        VacancyKeyboardKey.READY_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_READY_COMMAND)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.STOP, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, "Сам не знаю"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Похоже был введён некорректный ответ, выберите ответ на клавиатуре
                                или напишите самостоятельно: Да или Нет""",
                        VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
        assertThat(equalsCurrentSettingStateWith(UserSettingState.WAITING_STOP_COMMAND)).isTrue();

        dispatcher.commandDispatch(new UserCommandDto(USER_ID, CommandType.START, ""));
        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.UNKNOWN, "Привет бот"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text, VacancyReply::keyboardKey)
                .containsExactly(USER_ID, """
                                Неизвестная команда, попробуйте воспользоваться клавиатурой выше или напечатать команду корректно.""",
                        VacancyKeyboardKey.NONE);
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