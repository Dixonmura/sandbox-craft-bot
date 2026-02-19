package vacancy_tracker.presentation;

import markups.VacancyKeyboardKey;
import vacancy_tracker.bot.VacancyReply;
import vacancy_tracker.bot.types.NavigationAction;
import vacancy_tracker.bot.types.ReadyAction;
import vacancy_tracker.core.*;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.UserCommandDto;

import java.time.LocalTime;
import java.time.ZoneOffset;

import static vacancy_tracker.bot.VacancyMessages.*;

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
    private final ScheduledNotificationService notificationService;

    public VacancyCommandDispatcher(UserService userService, ScheduledNotificationService notificationService) {
        if (userService == null) {
            throw new IllegalArgumentException("userService не может быть null");
        }
        if (notificationService == null) {
            throw new IllegalArgumentException("notificationService не может быть null");
        }
        this.userService = userService;
        this.notificationService = notificationService;
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
                return new VacancyReply(userId, AFTER_START_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
            }
            case SET_UTC -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_UTC);
                    return new VacancyReply(userId, ENTER_UTC_OFFSET, VacancyKeyboardKey.UTC_KEYBOARD);
                } else {
                    try {
                        ZoneOffset zone = ZoneOffset.of(arguments);
                        userService.updateUtcOffset(userId, zone);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, UPDATED_UTC_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (RuntimeException e) {
                        return new VacancyReply(userId, ERROR_UTC_MESSAGE, VacancyKeyboardKey.UTC_KEYBOARD);
                    }
                }
            }
            case SET_REGION -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_REGION);
                    return new VacancyReply(userId, REGION_MESSAGE, VacancyKeyboardKey.REGION_KEYBOARD);
                } else {
                    try {
                        int regionCode = Integer.parseInt(arguments);
                        userService.updateRegionCode(userId, regionCode);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, UPDATE_REGION_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (NumberFormatException e) {
                        return new VacancyReply(userId, ERROR_REGION_MESSAGE, VacancyKeyboardKey.REGION_KEYBOARD);
                    }
                }
            }
            case SET_MIN_EXPERIENCE -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_MIN_EXPERIENCE);
                    return new VacancyReply(userId, EXPERIENCE_MESSAGE, VacancyKeyboardKey.MIN_EXPERIENCE_KEYBOARD);
                } else {
                    try {
                        int experienceFrom = Integer.parseInt(arguments);
                        userService.updateExperienceFrom(userId, experienceFrom);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, UPDATE_EXPERIENCE_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (NumberFormatException e) {
                        return new VacancyReply(userId, ERROR_EXPERIENCE_MESSAGE, VacancyKeyboardKey.MIN_EXPERIENCE_KEYBOARD);
                    }
                }
            }
            case SET_MIN_SALARY -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_MIN_SALARY);
                    return new VacancyReply(userId, SALARY_MESSAGE, VacancyKeyboardKey.MIN_SALARY_KEYBOARD);
                } else {
                    try {
                        int minSalary = Integer.parseInt(arguments);
                        userService.updateSalaryFrom(userId, minSalary);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, UPDATE_SALARY_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (NumberFormatException e) {
                        return new VacancyReply(userId, ERROR_SALARY_MESSAGE, VacancyKeyboardKey.MIN_SALARY_KEYBOARD);
                    }
                }
            }
            case SET_KEYWORD -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_KEYWORD);
                    return new VacancyReply(userId, KEYWORD_MESSAGE, VacancyKeyboardKey.NONE);
                } else {
                    userService.updateWordForSearch(userId, arguments);
                    userService.updateSettingState(userId, UserSettingState.CLEAN);
                    return new VacancyReply(userId, UPDATE_KEYWORD_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
                }
            }
            case SET_NOTIFY_TIME -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_SET_NOTIFY_TIME);
                    return new VacancyReply(userId, NOTIFY_TIME_MESSAGE, VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
                } else {
                    try {
                        LocalTime time = parseNotificationTime(arguments);
                        userService.updateNotificationTime(userId, time);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, UPDATE_NOTIFY_TIME_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
                    } catch (RuntimeException e) {
                        return new VacancyReply(userId, ERROR_NOTIFY_TIME_MESSAGE, VacancyKeyboardKey.NOTIFY_TIME_KEYBOARD);
                    }
                }
            }
            case READY -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_READY_COMMAND);
                    return new VacancyReply(userId, READY_MESSAGE, VacancyKeyboardKey.READY_KEYBOARD);
                } else {
                    if (ReadyAction.COMPLETE.getTitle().equals(arguments)) {
                        User user = userService.getOrCreateUser(userId);
                        userService.setStateSession(userId, VacancySessionState.ACTIVE);
                        notificationService.scheduleNotifications(user);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, READY_START_MESSAGE, VacancyKeyboardKey.STOP_KEYBOARD);
                    } else if (NavigationAction.RETURN.getTitle().equals(arguments)) {
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, BACK_INTO_SETTINGS_MESSAGE, VacancyKeyboardKey.SETTING_KEYBOARD);
                    } else {
                        return new VacancyReply(userId, ERROR_READY_MESSAGE, VacancyKeyboardKey.READY_KEYBOARD);
                    }
                }
            }
            case STOP -> {
                if (checkCurrentState(userId)) {
                    userService.updateSettingState(userId, UserSettingState.WAITING_STOP_COMMAND);
                    return new VacancyReply(userId, STOP_MESSAGE, VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
                } else {
                    if (ReadyAction.YES.getTitle().equals(arguments)) {
                        userService.setStateSession(userId, VacancySessionState.INACTIVE);
                        notificationService.cancelNotifications(userId);
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, SUCCESSFUL_STOP_MESSAGE, VacancyKeyboardKey.START_KEYBOARD);
                    } else if (ReadyAction.NO.getTitle().equals(arguments)) {
                        userService.updateSettingState(userId, UserSettingState.CLEAN);
                        return new VacancyReply(userId, CONTINUE_MESSAGE, VacancyKeyboardKey.STOP_KEYBOARD);
                    } else {
                        return new VacancyReply(userId, ERROR_STOP_MESSAGE, VacancyKeyboardKey.YES_OR_NO_KEYBOARD);
                    }
                }
            }
            case UNKNOWN -> {
                return new VacancyReply(userId, UNKNOWN_MESSAGE, VacancyKeyboardKey.NONE);
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
