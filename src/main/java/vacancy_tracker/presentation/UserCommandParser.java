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

public class UserCommandParser {

    private static final Logger log = LogManager.getLogger(UserCommandParser.class);

    private final Map<String, CommandType> commandByButtonText = Map.of(
            START_BOT.getTitle(), CommandType.START,
            STOP_BOT.getTitle(), CommandType.STOP,
            OUT_IN_ROUTER.getTitle(), CommandType.HOME
    );

    public UserCommandDto parse(IncomingUpdateDto dto, UserSettingState state) {
        String text = dto.text();
        Long userId = dto.userId();

        log.debug("Parse incoming update: userId={}, state={}, rawText='{}'",
                userId, state, text);

        UserCommandDto result;

        if (text == null || text.isBlank()) {
            result = new UserCommandDto(userId, CommandType.UNKNOWN, null);
        } else if (text.startsWith(SETTING_PREFIX)) {
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
            result = new UserCommandDto(userId, type, null);
        } else if (state == UserSettingState.CLEAN ||
                state == UserSettingState.NOT_INITIALIZED && START_BOT.getTitle().equals(text)) {

            CommandType type = commandByButtonText.get(text);
            if (type != null) {
                result = new UserCommandDto(userId, type, null);
            } else if (text.startsWith(START_SCHEDULER.getTitle())) {
                String textWithoutPrefix = text.substring(READY_PREFIX.length());
                result = new UserCommandDto(userId, CommandType.READY, textWithoutPrefix);
            } else {
                result = new UserCommandDto(userId, CommandType.UNKNOWN, text);
            }

        } else {
            switch (state) {
                case WAITING_SET_UTC -> {
                    if (text.startsWith(UTC_OFFSET_PAGE_PREFIX)) {
                        String pageNum = text.substring(UTC_OFFSET_PAGE_PREFIX.length());
                        result = new UserCommandDto(userId, CommandType.CHANGE_UTC_PAGE, pageNum);
                    } else if (text.startsWith(UTC_OFFSET_PREFIX)) {
                        String value = text.substring(UTC_OFFSET_PREFIX.length());
                        result = new UserCommandDto(userId, CommandType.SET_UTC, value);
                    } else {
                        result = new UserCommandDto(userId, CommandType.SET_UTC, text);
                    }
                }
                case WAITING_SET_REGION -> {
                    if (text.startsWith(REGION_PAGE_PREFIX)) {
                        String pageNum = text.substring(REGION_PAGE_PREFIX.length());
                        result = new UserCommandDto(userId, CommandType.CHANGE_REGION_PAGE, pageNum);
                    } else {
                        if (text.startsWith(REGION_CODE_PREFIX)) {
                            String value = text.substring(REGION_CODE_PREFIX.length());
                            result = new UserCommandDto(userId, CommandType.SET_REGION, value);
                        } else {
                            result = new UserCommandDto(userId, CommandType.SET_REGION, text);
                        }
                    }
                }
                case WAITING_SET_MIN_EXPERIENCE -> {
                    if (text.startsWith(EXPERIENCE_PREFIX)) {
                        String value = text.substring(EXPERIENCE_PREFIX.length());
                        result = new UserCommandDto(userId, CommandType.SET_MIN_EXPERIENCE, value);
                    } else {
                        result = new UserCommandDto(userId, CommandType.SET_MIN_EXPERIENCE, text);
                    }
                }
                case WAITING_SET_MIN_SALARY -> {
                    if (text.startsWith(SALARY_PREFIX)) {
                        String value = text.substring(SALARY_PREFIX.length());
                        result = new UserCommandDto(userId, CommandType.SET_MIN_SALARY, value);
                    } else {
                        result = new UserCommandDto(userId, CommandType.SET_MIN_SALARY, text);
                    }
                }
                case WAITING_SET_KEYWORD -> {
                    result = new UserCommandDto(userId, CommandType.SET_KEYWORD, text);
                }
                case WAITING_SET_NOTIFY_TIME -> {
                    if (text.startsWith(NOTIFY_PAGE_PREFIX)) {
                        String pageNum = text.substring(NOTIFY_PAGE_PREFIX.length());
                        result = new UserCommandDto(userId, CommandType.CHANGE_NOTIFY_PAGE, pageNum);
                    } else if (text.startsWith(NOTIFY_TIME_PREFIX)) {
                        String value = text.substring(NOTIFY_TIME_PREFIX.length());
                        result = new UserCommandDto(userId, CommandType.SET_NOTIFY_TIME, value);
                    } else {
                        result = new UserCommandDto(userId, CommandType.SET_NOTIFY_TIME, text);
                    }
                }
                case WAITING_READY_COMMAND -> {
                    String pageNum = text.substring(READY_PREFIX.length());
                    result = new UserCommandDto(userId, CommandType.READY, pageNum);
                }
                case WAITING_STOP_COMMAND -> {
                    String value = text.substring(YES_OR_NO_PREFIX.length());
                    result = new UserCommandDto(userId, CommandType.STOP, value);
                }
                case WAITING_HOME_COMMAND -> {
                    String value = text.substring(YES_OR_NO_PREFIX.length());
                    result = new UserCommandDto(userId, CommandType.HOME, value);
                }
                default -> result = new UserCommandDto(userId, CommandType.UNKNOWN, null);
            }
        }

        log.debug("Parsed command result: userId={}, type={}, args='{}'",
                result.userId(), result.commandType(), result.arguments());

        return result;
    }
}