package vacancy_tracker.presentation;

import vacancy_tracker.core.UserSettingState;
import vacancy_tracker.presentation.dto.CommandType;
import vacancy_tracker.presentation.dto.IncomingUpdateDto;
import vacancy_tracker.presentation.dto.UserCommandDto;

import java.util.Map;

public class UserCommandParser {

    private final Map<String, CommandType> commandByButtonText = Map.of(
            "Изменить часовой пояс UTC", CommandType.SET_UTC,
            "Регион", CommandType.SET_REGION,
            "Опыт", CommandType.SET_MIN_EXPERIENCE,
            "Минимальная зарплата", CommandType.SET_MIN_SALARY,
            "Ключевое слово", CommandType.SET_KEYWORD,
            "Уведомление", CommandType.SET_NOTIFY_TIME,
            "Готово", CommandType.READY,
            "Стоп", CommandType.STOP
    );

    public UserCommandDto parse(IncomingUpdateDto dto, UserSettingState state) {
        String text = dto.text();

        if (text == null || text.isBlank()) {
            return new UserCommandDto(dto.userId(), CommandType.UNKNOWN, null);
        }

        text = text.trim();

        if (state == UserSettingState.CLEAN) {
            CommandType type = commandByButtonText.get(text);
            if (type != null) {
                return new UserCommandDto(dto.userId(), type, null);
            }
            return new UserCommandDto(dto.userId(), CommandType.UNKNOWN, text);
        }

        CommandType inferredType = switch (state) {
            case WAITING_SET_UTC -> CommandType.SET_UTC;
            case WAITING_SET_REGION -> CommandType.SET_REGION;
            case WAITING_SET_MIN_EXPERIENCE -> CommandType.SET_MIN_EXPERIENCE;
            case WAITING_SET_MIN_SALARY -> CommandType.SET_MIN_SALARY;
            case WAITING_SET_KEYWORD -> CommandType.SET_KEYWORD;
            case WAITING_SET_NOTIFY_TIME -> CommandType.SET_NOTIFY_TIME;
            case WAITING_READY_COMMAND -> CommandType.READY;
            case WAITING_STOP_COMMAND -> CommandType.STOP;
            default -> CommandType.UNKNOWN;
        };

        return new UserCommandDto(dto.userId(), inferredType, text);
    }
}
