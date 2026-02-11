package vacancy_tracker.presentation;

import org.telegram.telegrambots.meta.api.objects.Update;
import vacancy_tracker.presentation.dto.IncomingUpdateDto;

public class UpdateMapper {
    public IncomingUpdateDto getUpdateDto(Update update) {
        return new IncomingUpdateDto(
                update.getMessage().getChatId(),
                update.getMessage().getText());
    }
}
