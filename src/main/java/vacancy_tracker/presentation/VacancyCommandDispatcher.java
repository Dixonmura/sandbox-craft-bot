package vacancy_tracker.presentation;

import markups.VacancyKeyboardKey;
import vacancy_tracker.bot.VacancyReply;
import vacancy_tracker.core.UserService;
import vacancy_tracker.core.UserSettingState;
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
                userService.updateSettingState(userId, UserSettingState.CLEAN);
                return new VacancyReply(userId, """
                        Vacancy tracker bot приветствует Вас!
                        Для удобства, в боте будет использоваться часовой пояс UTC""",
                        VacancyKeyboardKey.SETTING_KEYBOARD);
            }
            case SET_UTC -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_UTC);
                    return new VacancyReply(userId, """
                            Введите смещение часового пояса в формате UTC.
                            Вот пример: +07:00 или -11:30""", VacancyKeyboardKey.UTC_KEYBOARD);
                } else {
                    try {
                        ZoneOffset zone = ZoneOffset.of(arguments);
                        userService.updateUtcOffset(userId, zone);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, """
                                Смещение часового пояса для пользователя обновлено""",
                                VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (RuntimeException e) {
                        return new VacancyReply(userId, """
                                Неверный формат ввода зоны времени UTC. Ожидается: UTC+3, UTC+3:30, UTC-5""",
                                VacancyKeyboardKey.UTC_KEYBOARD);
                    }
                }
            }
            case SET_REGION -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_REGION);
                    return new VacancyReply(userId, """
                            Выберите регион из списка или введите номер региона в виде целого числа""",
                            VacancyKeyboardKey.REGION_KEYBOARD);
                } else {
                    try {
                        int regionCode = Integer.parseInt(arguments);
                        userService.updateRegionCode(userId, regionCode);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, """
                                Регион для поиска вакансий обновлён""",
                                VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (NumberFormatException e) {
                        return new VacancyReply(userId, """
                                Неверный формат ввода региона, введите целое число, например: 65, 77, 05""",
                                VacancyKeyboardKey.REGION_KEYBOARD);
                    }
                }
            }
            case SET_MIN_EXPERIENCE -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_MIN_EXPERIENCE);
                    return new VacancyReply(userId, """
                            Введите минимальный опыт работы в виде целого числа (лет).
                            Например: 5""",
                            VacancyKeyboardKey.MIN_EXPERIENCE_KEYBOARD);
                } else {
                    try {
                        int experienceFrom = Integer.parseInt(arguments);
                        userService.updateExperienceFrom(userId, experienceFrom);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, """
                                Минимальный опыт работы для поиска вакансий обновлён""",
                                VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (NumberFormatException e) {
                        return new VacancyReply(userId, """
                                Неверный формат ввода минимального опыта работы, ожидается например: 1, 3, 5""",
                                VacancyKeyboardKey.MIN_EXPERIENCE_KEYBOARD);
                    }
                }
            }
            case SET_MIN_SALARY -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_MIN_SALARY);
                    return new VacancyReply(userId, """
                            Введите минимальную ожидаемую заработную плату в виде целого числа.
                            Например: 70000""",
                            VacancyKeyboardKey.MIN_SALARY_KEYBOARD);
                } else {
                    try {
                        int minSalary = Integer.parseInt(arguments);
                        userService.updateSalaryFrom(userId, minSalary);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, """
                                Минимальная ожидаемая зарплата для поиска вакансий обновлёна""",
                                VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (NumberFormatException e) {
                        return new VacancyReply(userId, """
                                Неверный формат ввода минимальной заработной платы,
                                ожидается например: 70000 или 90000""",
                                VacancyKeyboardKey.MIN_SALARY_KEYBOARD);
                    }
                }
            }
            case SET_KEYWORD -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_KEYWORD);
                    return new VacancyReply(userId, """
                            Введите ключевое слово для поиска соответствующих вакансий.
                            Например: Java Developer""",
                            VacancyKeyboardKey.NONE);
                } else {
                    userService.updateWordForSearch(userId, arguments);
                    userService.updateSettingState(userId, UserSettingState.CLEAN);
                    return new VacancyReply(userId, """
                            Ключевое слово для поиска соответствующих вакансий обновлёно""",
                            VacancyKeyboardKey.SETTING_KEYBOARD);
                }
            }
            case SET_NOTIFY_TIME -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_NOTIFY_TIME);
                    return new VacancyReply(userId, """
                            Введите время нотификации (это обязательное поле).
                            Например: 13:35 или 18 55""",
                            VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
                } else {
                    try {
                        LocalTime time = parseNotificationTime(arguments);
                        userService.updateNotificationTime(userId, time);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, """
                                Время нотификации для ежедневного оповещения обновлёно""",
                                VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (RuntimeException e) {
                        return new VacancyReply(userId, """
                                неверный формат времени нотификации,
                                ожидается например: 17:00 или 02 33""",
                                VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
                    }
                }
            }
            case READY -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_READY_COMMAND);
                    return new VacancyReply(userId, """
                            Настройки завершены, для старта планировщика уведомлений нажмите кнопку "Начать"
                            или вернитесь в меню настроек.
                            """,
                            VacancyKeyboardKey.READY_KEYBOARD);
                } else {
                    if ("Начать".equals(arguments)) {
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, """
                                Планировщик запущен! Удачного поиска и до встречи! =)""",
                                VacancyKeyboardKey.STOP_KEYBOARD);
                    } else if ("Вернуться".equals(arguments)) {
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, """
                                Возврат в главное меню настроек.""",
                                VacancyKeyboardKey.SETTING_KEYBOARD);
                    } else {
                        return new VacancyReply(userId, """
                                Похоже был введён некорректный ответ, для старта нажмите "Начать"
                                """,
                                VacancyKeyboardKey.READY_KEYBOARD);
                    }
                }
            }
            case STOP -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_STOP_COMMAND);
                    return new VacancyReply(userId, """
                            Вы уверены, что хотите остановить работу бота и удалить данные поиска? Введите: Да или Нет
                            """,
                            VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
                } else {
                    if ("Да".equals(arguments)) {
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, """
                                Планирование завершено, все данные удалены, было приятно работать!
                                Надеемся было полезно и продуктивно! Возвращайтесь! =)""",
                                VacancyKeyboardKey.START_KEYBOARD);
                    } else if ("Нет".equals(arguments)) {
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, """
                                Благодарим, что продолжаете пользоваться VacancyTrackerBot =)""",
                                VacancyKeyboardKey.STOP_KEYBOARD);
                    } else {
                        return new VacancyReply(userId, """
                                Похоже был введён некорректный ответ, выберите ответ на клавиатуре
                                или напишите самостоятельно: Да или Нет""",
                                VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
                    }
                }
            }
            case UNKNOWN -> {
                return new VacancyReply(userId, """
                        Неизвестная команда, попробуйте воспользоваться клавиатурой выше или напечатать команду корректно.""",
                        VacancyKeyboardKey.NONE);
            }
            default -> throw new IllegalStateException("Неизвестная ошибка при обработке типа команды" + type);
        }
    }

    private LocalTime parseNotificationTime(String input) {
        String trimmed = input.trim();
        if (trimmed.contains(":")) {
            return LocalTime.parse(trimmed);
        }
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 2) {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return LocalTime.of(hour, minute);
        }
        throw new IllegalArgumentException("Неверный формат времени");
    }

    private boolean checkCurrentState(Long userId) {
        return userService.getSettingState(userId) == UserSettingState.CLEAN;
    }
}
