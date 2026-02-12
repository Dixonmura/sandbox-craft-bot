package vacancy_tracker.presentation;

import vacancy_tracker.bot.VacancyReply;
import vacancy_tracker.core.UserService;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.UserCommandDto;

import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * Обрабатывает команды пользователя высокого уровня и
 * маршрутизирует их в UserService.
 * <p>
 * Отвечает за:
 * <ul>
 *   <li>Проверку входных данных и аргументов команд.</li>
 *   <li>Парсинг строковых аргументов в доменные типы (ZoneOffset, LocalTime, числа).</li>
 *   <li>Формирование текстовых ответов VacancyReply для бота.</li>
 * </ul>
 * Не содержит бизнес-логики поиска вакансий, только координирует работу сервисов.
 */
public class VacancyCommandDispatcher {

    private final UserService userService;

    public VacancyCommandDispatcher(UserService userService) {
        if (userService == null) {
            throw new IllegalArgumentException("userService не может быть null");
        }
        this.userService = userService;
    }

    /**
     * Обрабатывает одну пользовательскую команду.
     *
     * @param commandDto команда пользователя с типом и аргументами
     * @return VacancyReply с текстом ответа пользователю
     * @throws IllegalArgumentException если commandDto равен null
     */
    public VacancyReply commandDispatch(UserCommandDto commandDto) {
        if (commandDto == null) {
            throw new IllegalArgumentException("commandDto не может быть null");
        }

        Long userId = commandDto.userId();
        CommandType type = commandDto.commandType();
        String arguments = commandDto.arguments();

        switch (type) {
            case START -> {
                userService.getOrCreateUser(userId);

                //TODO добавить к выводу актуальную для текущего этапа клавиатуру

                return new VacancyReply(userId, """
                        Vacancy tracker bot приветствует Вас!
                        Для удобства, в боте будет использоваться часовой пояс UTC""");
            }
            case SET_UTC -> {
                if (arguments == null || arguments.isBlank()) {

                    //TODO установить состояние ожидания для текущего типа команды и убрать клавиатуру

                    return new VacancyReply(userId, """
                            Введите смещение часового пояса в формате UTC.
                            Вот пример: +07:00 или -11:30"""); // убрали клавиатуру
                } else {
                    try {
                        ZoneOffset zone = ZoneOffset.of(arguments);
                        userService.updateUtcOffset(userId, zone);

                        //TODO добавить к выводу актуальную для текущего этапа клавиатуру очистить состояние

                        return new VacancyReply(userId, """
                                Смещение часового пояса для пользователя обновлено""");
                    } catch (RuntimeException e) {
                        return new VacancyReply(userId, """
                                Неверный формат ввода зоны времени UTC. Ожидается: UTC+3, UTC+3:30, UTC-5""");
                    }
                }
            }
            case SET_REGION -> {
                if (arguments == null || arguments.isBlank()) {

                    //TODO установить состояние ожидания для текущего типа команды и убрать клавиатуру

                    return new VacancyReply(userId, """
                            Выберите регион из списка или введите номер региона в виде целого числа"""); // убрали клавиатуру
                } else {
                    try {
                        int regionCode = Integer.parseInt(arguments);
                        userService.updateRegionCode(userId, regionCode);

                        //TODO добавить к выводу актуальную для текущего этапа клавиатуру, очистить состояние

                        return new VacancyReply(userId, """
                                Регион для поиска вакансий обновлён""");
                    } catch (NumberFormatException e) {
                        return new VacancyReply(userId, """
                                Неверный формат ввода региона, введите целое число, например: 65, 77, 05""");
                    }
                }
            }
            case SET_MIN_EXPERIENCE -> {
                if (arguments == null || arguments.isBlank()) {

                    //TODO установить состояние ожидания для текущего типа команды и убрать клавиатуру

                    return new VacancyReply(userId, """
                            Введите минимальный опыт работы в виде целого числа (лет).
                            Например: 5"""); // убрали клавиатуру
                } else {
                    try {
                        int experienceFrom = Integer.parseInt(arguments);
                        userService.updateExperienceFrom(userId, experienceFrom);

                        //TODO добавить к выводу актуальную для текущего этапа клавиатуру, очистить состояние

                        return new VacancyReply(userId, """
                                Минимальный опыт работы для поиска вакансий обновлён""");
                    } catch (NumberFormatException e) {
                        return new VacancyReply(userId, """
                                Неверный формат ввода минимального опыта работы, ожидается например: 1, 3, 5""");
                    }
                }
            }
            case SET_MIN_SALARY -> {
                if (arguments == null || arguments.isBlank()) {

                    //TODO установить состояние ожидания для текущего типа команды и убрать клавиатуру

                    return new VacancyReply(userId, """
                            Введите минимальную ожидаемую заработную плату в виде целого числа.
                            Например: 70000""");
                } else {
                    try {
                        int minSalary = Integer.parseInt(arguments);
                        userService.updateSalaryFrom(userId, minSalary);

                        //TODO добавить к выводу актуальную для текущего этапа клавиатуру, очистить состояние

                        return new VacancyReply(userId, """
                                Минимальная ожидаемая зарплата для поиска вакансий обновлёна""");
                    } catch (NumberFormatException e) {
                        return new VacancyReply(userId, """
                                Неверный формат ввода минимальной заработной платы,
                                ожидается например: 70000 или 90000""");
                    }
                }
            }
            case SET_KEYWORD -> {
                if (arguments == null || arguments.isBlank()) {

                    //TODO установить состояние ожидания для текущего типа команды и убрать клавиатуру

                    return new VacancyReply(userId, """
                            Введите ключевое слово для поиска соответствующих вакансий.
                            Например: Java Developer""");
                } else {
                    userService.updateWordForSearch(userId, arguments);

                    //TODO добавить к выводу актуальную для текущего этапа клавиатуру, очистить состояние

                    return new VacancyReply(userId, """
                            Ключевое слово для поиска соответствующих вакансий обновлёно""");
                }
            }
            case SET_NOTIFY_TIME -> {
                if (arguments == null || arguments.isBlank()) {

                    //TODO установить состояние ожидания для текущего типа команды и убрать клавиатуру

                    return new VacancyReply(userId, """
                            Введите время нотификации (это обязательное поле).
                            Например: 13:35 или 18 55""");
                } else {
                    try {
                        LocalTime time = LocalTime.parse(arguments);
                        userService.updateNotificationTime(userId, time);

                        //TODO добавить парсер формата для LocalTime
                        //TODO добавить к выводу актуальную для текущего этапа клавиатуру, очистить состояние

                        return new VacancyReply(userId, """
                                Время нотификации для ежедневного оповещения обновлёно""");
                    } catch (RuntimeException e) {
                        return new VacancyReply(userId, """
                                неверный формат времени нотификации,
                                ожидается например: 17:00 или 02 33""");
                    }
                }
            }
            case READY -> {
                if (arguments == null || arguments.isBlank()) {
                    //TODO проверяем готовность в UserService
                    //TODO установить состояние ожидания для текущего типа команды и показать клавиатуру для старта

                    return new VacancyReply(userId, """
                            Настройки завершены, для старта планировщика уведомлений нажмите кнопку "Начать"
                            """);
                } else {
                    if (arguments.equals("Начать")) {
                        //TODO очищаем состояние ожидания типа команды и запускаем планировщик
                        return new VacancyReply(userId, """
                                Планировщик запущен! Удачного поиска и до встречи! =)""");
                    }
                    return new VacancyReply(userId, """
                            Похоже был введён некорректный ответ, для старта нажмите "Начать"
                            """);
                }
            }
            case STOP -> {
                if (arguments == null || arguments.isBlank()) {

                    //TODO установить состояние ожидания для текущего типа команды и показать клавиатуру с вариантами "Да" и "Нет"

                    return new VacancyReply(userId, """
                            Вы уверены, что хотите остановить работу бота и удалить данные поиска? Введите: Да или Нет
                            """);
                } else {
                    switch (arguments) {
                        case "Да" -> {

                            //TODO убираем клавиатуру

                            return new VacancyReply(userId, """
                                    Планирование завершено, все данные удалены, было приятно работать!
                                    Надеемся было полезно и продуктивно! Возвращайтесь! =)""");
                        }
                        case "Нет" -> {

                            //TODO добавить к выводу актуальную для текущего этапа клавиатуру и UserService.appropriateCommand

                            return new VacancyReply(userId, """
                                    Благодарим, что продолжаете пользоваться VacancyTrackerBot =)""");
                        }
                        default -> {
                            return new VacancyReply(userId, """
                                    Похоже был введён некорректный ответ, выберите ответ на клавиатуре
                                    или напишите самостоятельно: Да или Нет""");
                        }
                    }
                }
            }
            case UNKNOWN -> {
                return new VacancyReply(userId, """
                        Неизвестная команда, попробуйте воспользоваться клавиатурой или напечатать команду корректно.""");
            }
            default -> throw new IllegalStateException("Неизвестная ошибка при обработке типа команды" + type);
        }
    }
}
