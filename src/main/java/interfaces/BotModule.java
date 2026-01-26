package interfaces;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotModule<R>{
    boolean hasSession(Long chatId);
    R handleAnswer(Update update);
}
