package command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandStartTest {

    @Mock
    TelegramClient telegramClient;
    CommandStart commandStart;

    @BeforeEach
    void setUp() {
        commandStart = new CommandStart(telegramClient);
    }

    @Test
    @DisplayName("Проверка отправки сообщения при корректной команде /Start")
    void execute_shouldSendMessage_whenCommandIsCorrectly() throws TelegramApiException {

        commandStart.execute(getUpdate());

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertThat(sendMessage.getChatId()).isEqualTo("11");
        assertThat(sendMessage.getText())
                .contains("Помогаю выбрать, чем заняться")
                .contains("отдохнуть — сыграй в кино‑квиз")
                .contains("включить помидор‑таймер");
    }

    @Test
    @DisplayName("Команда /Start не падает, если TelegramClient кидает TelegramApiException")
    void execute_shouldNotThrow_whenTelegramClientFails() throws TelegramApiException {

        when(telegramClient.execute(any(SendMessage.class)))
                .thenThrow(new TelegramApiException());
        commandStart.execute(getUpdate());

        verify(telegramClient).execute(any(SendMessage.class));
    }

    Update getUpdate() {
        Update update = new Update();
        Chat chat = new Chat(11L, "");
        Message message = new Message();
        message.setChat(chat);
        message.setText("/Start");
        update.setMessage(message);

        return update;
    }
}