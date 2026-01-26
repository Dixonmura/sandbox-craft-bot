package command;

import movie_quiz.bot.MovieQuizBot;
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
import pomodoro.bot.PomodoroBot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {

    @Mock
    TelegramClient telegramClient;
    MovieQuizBot movieQuizBot;
    PomodoroBot pomodoroBot;
    CommandDispatcher commandDispatcher;

    @BeforeEach
    void setUp() {
        movieQuizBot = new MovieQuizBot();
        commandDispatcher = new CommandDispatcher(telegramClient, movieQuizBot, pomodoroBot);
    }

    @Test
    @DisplayName("Проверка вызова команды /start")
    void dispatch_shouldCallStartCommand_whenStart() throws TelegramApiException {
        Update update = getUpdate("/Start", 12L);

        commandDispatcher.dispatch(update.getMessage().getText(), update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertThat(sendMessage.getChatId()).isEqualTo("12");
        assertThat(sendMessage.getText())
                .contains("Я — бот‑роутер этого чата \uD83E\uDD16\n");
    }

    @Test
    @DisplayName("Проверка вызова команды /playmoviequiz")
    void dispatch_shouldCallMovieQuizCommand_whenMovieQuiz() throws TelegramApiException {
        Update update = getUpdate("/Playmoviequiz", 17L);

        commandDispatcher.dispatch(update.getMessage().getText(), update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertThat(sendMessage.getChatId()).isEqualTo("17");
        assertThat(sendMessage.getText()).contains("Угадай фильм по кадру");
    }

    @Test
    @DisplayName("Проверка диспетчера при неизвестной команде")
    void dispatch_shouldNotCallAnyCommand_whenCommandUnknown() throws TelegramApiException {
        Update update = getUpdate("/", 12L);

        commandDispatcher.dispatch(update.getMessage().getText(), update);

        verify(telegramClient, never()).execute(any(SendMessage.class));
    }

    private Update getUpdate(String textMessage, Long chatId) {
        Update update = new Update();
        Chat chat = new Chat(chatId, "");
        Message message = new Message();
        message.setChat(chat);
        message.setText(textMessage);
        update.setMessage(message);
        return update;
    }
}