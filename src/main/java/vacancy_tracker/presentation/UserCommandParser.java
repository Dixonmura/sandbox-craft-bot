package vacancy_tracker.presentation;

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

    public UserCommandDto parse(IncomingUpdateDto dto) {
        String text = dto.text();

        if (text == null) {
            return new UserCommandDto(dto.userId(), CommandType.UNKNOWN, null);
        }

        CommandType type;
        String arguments = null; //пока без реализации

        if (text.startsWith("/")) {
            type = CommandType.START; // пока заглушка
        } else {
            type = commandByButtonText.getOrDefault(text, CommandType.UNKNOWN);
        }

        return new UserCommandDto(dto.userId(), type, arguments);
    }
}
