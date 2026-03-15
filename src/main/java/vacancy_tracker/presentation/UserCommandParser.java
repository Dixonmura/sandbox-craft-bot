package vacancy_tracker.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vacancy_tracker.core.UserSettingState;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.IncomingUpdateDto;
import vacancy_tracker.presentation.dto.UserCommandDto;

import java.util.Map;

import static vacancy_tracker.bot.CallbackPrefixes.*;
import static vacancy_tracker.bot.types.StartSchedulerOptions.START_SCHEDULER;
import static vacancy_tracker.bot.types.StartStopBotOption.*;

/**
 * Парсер входящих сообщений от пользователя в команды для диспетчера.
 * <p>
 * Анализирует текст сообщения и текущее состояние пользователя,
 * определяет тип команды и извлекает аргументы.
 */
public class UserCommandParser {

    private static final Logger log = LogManager.getLogger(UserCommandParser.class);

    private final Map<String, CommandType> commandByButtonText = Map.of(
            START_BOT.getTitle(), CommandType.START,
            STOP_BOT.getTitle(), CommandType.STOP,
            OUT_IN_ROUTER.getTitle(), CommandType.HOME
    );

    /**
     * Парсит входящее сообщение в команду.
     *
     * @param dto   DTO с данными от пользователя
     * @param state текущее состояние пользователя
     * @return DTO команды для передачи в диспетчер
     */
    public UserCommandDto parse(IncomingUpdateDto dto, UserSettingState state) {
        String text = dto.text();
        Long userId = dto.userId();

        log.debug("Parse incoming update: userId={}, state={}, rawText='{}'",
                userId, state, text);

        UserCommandDto result;

        if (text == null || text.isBlank()) {
            result = new UserCommandDto(userId, CommandType.UNKNOWN, null);

        } else if (text.startsWith(SETTING_PREFIX)) {
            result = parseSettingCommand(userId, text);

        } else if (state == UserSettingState.CLEAN ||
                (state == UserSettingState.NOT_INITIALIZED && START_BOT.getTitle().equals(text))) {
            result = parseCleanStateCommand(userId, text);

        } else {
            result = parseStateCommand(userId, text, state);
        }

        log.debug("Parsed command result: userId={}, type={}, args='{}'",
                result.userId(), result.commandType(), result.arguments());

        return result;
    }

    /**
     * Парсит команды из меню настроек (с префиксом SETTING_PREFIX).
     */
    private UserCommandDto parseSettingCommand(Long userId, String text) {
        String menuItem = text.substring(SETTING_PREFIX.length());
        CommandType type = switch (menuItem) {
            case "🕐 Изменить часовой пояс UTC" -> CommandType.SET_UTC;
            case "🌍 Регион" -> CommandType.SET_REGION;
            case "💼 Минимальный опыт" -> CommandType.SET_MIN_EXPERIENCE;
            case "💰 Минимальная зарплата" -> CommandType.SET_MIN_SALARY;
            case "🔍 Слово для поиска" -> CommandType.SET_KEYWORD;
            case "🔔 Настройки нотификации" -> CommandType.SET_NOTIFY_TIME;
            case "🚀 Запустить планировщик" -> CommandType.READY;
            default -> CommandType.UNKNOWN;
        };
        return new UserCommandDto(userId, type, null);
    }

    /**
     * Парсит команды в чистом состоянии (CLEAN или NOT_INITIALIZED).
     */
    private UserCommandDto parseCleanStateCommand(Long userId, String text) {
        CommandType type = commandByButtonText.get(text);
        if (type != null) {
            return new UserCommandDto(userId, type, null);
        } else if (text.startsWith(START_SCHEDULER.getTitle())) {
            String textWithoutPrefix = text.substring(READY_PREFIX.length());
            return new UserCommandDto(userId, CommandType.READY, textWithoutPrefix);
        } else {
            return new UserCommandDto(userId, CommandType.UNKNOWN, text);
        }
    }

    /**
     * Парсит команды в зависимости от текущего состояния пользователя.
     */
    private UserCommandDto parseStateCommand(Long userId, String text, UserSettingState state) {
        return switch (state) {
            case WAITING_SET_UTC -> parseWaitingUtc(userId, text);
            case WAITING_SET_REGION -> parseWaitingRegion(userId, text);
            case WAITING_SET_MIN_EXPERIENCE -> parseWaitingExperience(userId, text);
            case WAITING_SET_MIN_SALARY -> parseWaitingSalary(userId, text);
            case WAITING_SET_KEYWORD -> parseWaitingKeyword(userId, text);
            case WAITING_SET_NOTIFY_TIME -> parseWaitingNotifyTime(userId, text);
            case WAITING_READY_COMMAND -> parseReadyCommand(userId, text);
            case WAITING_STOP_COMMAND -> parseStopCommand(userId, text);
            case WAITING_HOME_COMMAND -> parseHomeCommand(userId, text);
            default -> new UserCommandDto(userId, CommandType.UNKNOWN, null);
        };
    }

    private UserCommandDto parseWaitingUtc(Long userId, String text) {
        if (text.startsWith(UTC_OFFSET_PAGE_PREFIX)) {
            String pageNum = text.substring(UTC_OFFSET_PAGE_PREFIX.length());
            return new UserCommandDto(userId, CommandType.CHANGE_UTC_PAGE, pageNum);
        } else if (text.startsWith(UTC_OFFSET_PREFIX)) {
            String value = text.substring(UTC_OFFSET_PREFIX.length());
            return new UserCommandDto(userId, CommandType.SET_UTC, value);
        } else {
            return new UserCommandDto(userId, CommandType.SET_UTC, text);
        }
    }

    private UserCommandDto parseWaitingRegion(Long userId, String text) {
        if (text.startsWith(REGION_PAGE_PREFIX)) {
            String pageNum = text.substring(REGION_PAGE_PREFIX.length());
            return new UserCommandDto(userId, CommandType.CHANGE_REGION_PAGE, pageNum);
        } else if (text.startsWith(REGION_CODE_PREFIX)) {
            String value = text.substring(REGION_CODE_PREFIX.length());
            return new UserCommandDto(userId, CommandType.SET_REGION, value);
        } else {
            return new UserCommandDto(userId, CommandType.SET_REGION, text);
        }
    }

    private UserCommandDto parseWaitingExperience(Long userId, String text) {
        if (text.startsWith(EXPERIENCE_PREFIX)) {
            String value = text.substring(EXPERIENCE_PREFIX.length());
            return new UserCommandDto(userId, CommandType.SET_MIN_EXPERIENCE, value);
        } else {
            return new UserCommandDto(userId, CommandType.SET_MIN_EXPERIENCE, text);
        }
    }

    private UserCommandDto parseWaitingSalary(Long userId, String text) {
        if (text.startsWith(SALARY_PREFIX)) {
            String value = text.substring(SALARY_PREFIX.length());
            return new UserCommandDto(userId, CommandType.SET_MIN_SALARY, value);
        } else {
            return new UserCommandDto(userId, CommandType.SET_MIN_SALARY, text);
        }
    }

    private UserCommandDto parseWaitingKeyword(Long userId, String text) {
        if (text.startsWith(KEY_WORD_PREFIX)) {
            String keyWord = text.substring(KEY_WORD_PREFIX.length());
            return new UserCommandDto(userId, CommandType.SET_KEYWORD, keyWord);
        } else {
            return new UserCommandDto(userId, CommandType.SET_KEYWORD, text);
        }
    }

    private UserCommandDto parseWaitingNotifyTime(Long userId, String text) {
        if (text.startsWith(NOTIFY_PAGE_PREFIX)) {
            String pageNum = text.substring(NOTIFY_PAGE_PREFIX.length());
            return new UserCommandDto(userId, CommandType.CHANGE_NOTIFY_PAGE, pageNum);
        } else if (text.startsWith(NOTIFY_TIME_PREFIX)) {
            String value = text.substring(NOTIFY_TIME_PREFIX.length());
            return new UserCommandDto(userId, CommandType.SET_NOTIFY_TIME, value);
        } else {
            return new UserCommandDto(userId, CommandType.SET_NOTIFY_TIME, text);
        }
    }

    private UserCommandDto parseReadyCommand(Long userId, String text) {
        String pageNum = text.substring(READY_PREFIX.length());
        return new UserCommandDto(userId, CommandType.READY, pageNum);
    }

    private UserCommandDto parseStopCommand(Long userId, String text) {
        String value = text.substring(YES_OR_NO_PREFIX.length());
        return new UserCommandDto(userId, CommandType.STOP, value);
    }

    private UserCommandDto parseHomeCommand(Long userId, String text) {
        String value = text.substring(YES_OR_NO_PREFIX.length());
        return new UserCommandDto(userId, CommandType.HOME, value);
    }
}