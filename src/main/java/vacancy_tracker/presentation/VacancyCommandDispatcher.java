package vacancy_tracker.presentation;

import vacancy_tracker.bot.VacancyReply;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.UserCommandDto;

public class VacancyCommandDispatcher {

    public VacancyReply commandDispatch(UserCommandDto commandDto) {
        Long userId = commandDto.userId();
        CommandType type = commandDto.commandType();
        String arguments = commandDto.arguments();

        switch (type) {
            case START -> {
                return new VacancyReply(userId, """
                        Vacancy tracker bot приветствует Вас!
                        Для удобства, в боте будет использоваться часовой пояс UTC""");

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру и UserService.appropriateCommand
            }
            case SET_UTC -> {
                return new VacancyReply(userId, """
                        Введите смещение часового пояса в формате UTC.
                        Вот пример: +07:00 или -11:30""");

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру и UserService.appropriateCommand
            }
            case SET_REGION -> {
                return new VacancyReply(userId, """
                        Выберите регион из списка или введите номер региона в виде целого числа""");

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру и UserService.appropriateCommand
            }
            case SET_MIN_EXPERIENCE -> {
                return new VacancyReply(userId, """
                        Введите минимальный опыт работы в виде целого числа (лет).
                        Например: 5""");

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру и UserService.appropriateCommand
            }
            case SET_MIN_SALARY -> {
                return new VacancyReply(userId, """
                        Введите минимальную ожидаемую заработную плату в виде целого числа.
                        Например: 70000""");

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру и UserService.appropriateCommand
            }
            case SET_KEYWORD -> {
                return new VacancyReply(userId, """
                        Введите ключевое слова для поиска соответствующих вакансий.
                        Например: Java Developer""");

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру и UserService.appropriateCommand
            }
            case SET_NOTIFY_TIME -> {
                return new VacancyReply(userId, """
                        Введите время нотификации (это обязательное поле).
                        Например: 13:00""");

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру и UserService.appropriateCommand
            }
            case READY -> {
                return new VacancyReply(userId, """
                        Настройки завершены, для старта планировщика уведомлений нажмите кнопку "Начать\"""");

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру
            }
            case STOP -> {
                return new VacancyReply(userId, """
                        Планирование завершено, все данные удалены, было приятно работать! 
                        Надеемся было полезно и продуктивно! Возвращайтесь! =)""");

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру и UserService.appropriateCommand
            }
            case UNKNOWN -> {
                return new VacancyReply(userId, """
                        Неизвестная команда, попробуйте воспользоваться клавиатурой или напечатать команду корректно.""");
            }
            default -> throw new IllegalStateException("Неизвестная ошибка при обработке типа команды" + type);
        }
    }
}
