package vacancy_tracker.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.bot.VacancyReply;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.UserCommandDto;

import static org.assertj.core.api.Assertions.assertThat;

class VacancyCommandDispatcherTest {

    Long USER_ID = 18L;
    VacancyCommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new VacancyCommandDispatcher();
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
                new UserCommandDto(USER_ID, CommandType.SET_UTC, "Изменить часовой пояс"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите смещение часового пояса в формате UTC.
                        Вот пример: +07:00 или -11:30""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_REGION, "Изменить регион"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Выберите регион из списка или введите номер региона в виде целого числа""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_EXPERIENCE, "Установить опыт работы"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите минимальный опыт работы в виде целого числа (лет).
                        Например: 5""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_MIN_SALARY, "Установить заработную плату"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите минимальную ожидаемую заработную плату в виде целого числа.
                        Например: 70000""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_KEYWORD, "Установить ключевое слово для поиска"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите ключевое слова для поиска соответствующих вакансий.
                        Например: Java Developer""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.SET_NOTIFY_TIME, "Установить время уведомления"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Введите время нотификации (это обязательное поле).
                        Например: 13:00""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.READY, "Настройки готовы"));
        System.out.println(reply.text());
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Настройки завершены, для старта планировщика уведомлений нажмите кнопку "Начать\"""");

        reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.STOP, "Остановить работу бота"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Планирование завершено, все данные удалены, было приятно работать!
                        Надеемся было полезно и продуктивно! Возвращайтесь! =)""");

    }

    @Test
    @DisplayName("Проверка диспетчера на некорректную команду")
    void commandDispatch_shouldReturnUnknownTypeMessage_whenCommandTypeIsUnknown() {
        VacancyReply reply = dispatcher.commandDispatch(
                new UserCommandDto(USER_ID, CommandType.UNKNOWN, "Привет бот"));
        assertThat(reply).isNotNull().extracting(VacancyReply::userId, VacancyReply::text)
                .containsExactly(USER_ID, """
                        Неизвестная команда, попробуйте воспользоваться клавиатурой или напечатать команду корректно.""");
    }
}