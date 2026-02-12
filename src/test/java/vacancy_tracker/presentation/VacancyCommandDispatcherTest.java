package vacancy_tracker.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.bot.VacancyReply;
import vacancy_tracker.core.UserRepository;
import vacancy_tracker.core.UserService;
import vacancy_tracker.core.UserSettings;
import vacancy_tracker.data.InMemoryUserRepository;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.UserCommandDto;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VacancyCommandDispatcherTest {

    Long USER_ID = 18L;
    UserRepository repository;
    UserService userService;
    VacancyCommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        userService = new UserService(repository);
        dispatcher = new VacancyCommandDispatcher(userService);
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
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Vacancy tracker bot приветствует Вас!
                        Для удобства, в боте будет использоваться часовой пояс UTC""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_UTC, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите смещение часового пояса в формате UTC.
                        Вот пример: +07:00 или -11:30""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_UTC, "+05:30"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Смещение часового пояса для пользователя обновлено""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Выберите регион из списка или введите номер региона в виде целого числа""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, "65"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Регион для поиска вакансий обновлён""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите минимальный опыт работы в виде целого числа (лет).
                        Например: 5""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, "3"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Минимальный опыт работы для поиска вакансий обновлён""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите минимальную ожидаемую заработную плату в виде целого числа.
                        Например: 70000""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, "90000"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Минимальная ожидаемая зарплата для поиска вакансий обновлёна""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_KEYWORD, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите ключевое слово для поиска соответствующих вакансий.
                        Например: Java Developer""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_KEYWORD, "Java Developer"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Ключевое слово для поиска соответствующих вакансий обновлёно""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите время нотификации (это обязательное поле).
                        Например: 13:35 или 18 55""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "03:00"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Время нотификации для ежедневного оповещения обновлёно""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, ""));
        System.out.println(reply.text());
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Настройки завершены, для старта планировщика уведомлений нажмите кнопку "Начать"
                        """);

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, "Начать"));
        System.out.println(reply.text());
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Планировщик запущен! Удачного поиска и до встречи! =)""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, ""));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Вы уверены, что хотите остановить работу бота и удалить данные поиска? Введите: Да или Нет
                        """);

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, "Да"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Планирование завершено, все данные удалены, было приятно работать!
                        Надеемся было полезно и продуктивно! Возвращайтесь! =)""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, "Нет"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Благодарим, что продолжаете пользоваться VacancyTrackerBot =)""");
    }

    @Test
    @DisplayName("Проверка диспетчера на некорректные команды")
    void commandDispatch_shouldReturnUnknownTypeMessage_whenCommandTypeIsUnknown() {

        VacancyReply reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_UTC, "27:46"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Неверный формат ввода зоны времени UTC. Ожидается: UTC+3, UTC+3:30, UTC-5""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, "Сахалин"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Неверный формат ввода региона, введите целое число, например: 65, 77, 05""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, "Минимум"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Неверный формат ввода минимального опыта работы, ожидается например: 1, 3, 5""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, "Много"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Неверный формат ввода минимальной заработной платы,
                        ожидается например: 70000 или 90000""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "Где-то в обед"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        неверный формат времени нотификации,
                        ожидается например: 17:00 или 02 33""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, "Ну поехали"));
        System.out.println(reply.text());
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Похоже был введён некорректный ответ, для старта нажмите "Начать"
                        """);

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, "Сам не знаю"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Похоже был введён некорректный ответ, выберите ответ на клавиатуре
                        или напишите самостоятельно: Да или Нет""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.UNKNOWN, "Привет бот"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Неизвестная команда, попробуйте воспользоваться клавиатурой или напечатать команду корректно.""");
    }

    @Test
    @DisplayName("Проверка выброса исключения при создании экземпляра, когда userService null")
    void constructor_shouldThrowsIllegalArgumentException_whenUserServiceIsNull() {
        assertThatThrownBy(() ->
                new VacancyCommandDispatcher(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userService не может быть null");
    }

    @Test
    @DisplayName("Проверка выброса исключения, когда в commandDispatch подаётся null")
    void commandDispatch_shouldThrowsIllegalArgumentException_whenInputValuesInvalid() {
        assertThatThrownBy(() ->
                dispatcher.commandDispatch(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("commandDto не может быть null");

    }
}