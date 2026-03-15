package vacancy_tracker.presentation;

import org.telegram.telegrambots.meta.api.objects.Update;
import vacancy_tracker.presentation.dto.IncomingUpdateDto;

public class UpdateMapper {
    public IncomingUpdateDto getUpdateDto(Update update) {
        if (update == null) {
            throw new IllegalArgumentException("update не может быть null");
        }
        
        IncomingUpdateDto currentUpdate = null;
        
        if (update.hasMessage()) {
            currentUpdate = new IncomingUpdateDto(
                    update.getMessage().getChatId(),
                    update.getMessage().getText()
            );
        }

        if (update.hasCallbackQuery()) {
            currentUpdate = new IncomingUpdateDto(
                    update.getCallbackQuery().getMessage().getChatId(),
                    update.getCallbackQuery().getData()
            );
        }
        
        return currentUpdate;
    }
}
