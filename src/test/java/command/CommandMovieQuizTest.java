package command;

import movie_quiz.bot.BotReply;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandMovieQuizTest {

    @Mock
    TelegramClient telegramClient;

    @Mock
    MovieQuizBot movieQuizBot;

    CommandMovieQuiz commandMovieQuiz;

    @BeforeEach
    void setUp() {
        commandMovieQuiz = new CommandMovieQuiz(telegramClient, movieQuizBot);
    }

    @Test
    @DisplayName("Проверка отправки сообщения при корректной команде /playMovieQuiz")
    void execute_shouldSendQuestionMessage_whenCommandIsCorrect() throws TelegramApiException {
        // given
        Update update = getUpdate();
        BotReply reply = new BotReply(
                "Угадай фильм по кадру",
                java.util.List.of("Фильм 1", "Фильм 2", "Фильм 3", "Фильм 4"),
                false,
                "path/to/image.jpg"
        );
        when(movieQuizBot.startGame(update)).thenReturn(reply);

        // when
        commandMovieQuiz.execute(update);

        // then: проверяем только текстовое сообщение
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertThat(sendMessage.getChatId()).isEqualTo("15");
        assertThat(sendMessage.getText()).contains("Угадай фильм по кадру");
    }

    @Test
    @DisplayName("Команда /playMovieQuiz не падает, если TelegramClient кидает TelegramApiException")
    void execute_shouldNotThrow_whenTelegramClientFails() throws TelegramApiException {
        // given
        Update update = getUpdate();
        BotReply reply = new BotReply(
                "Угадай фильм по кадру",
                List.of("Фильм 1", "Фильм 2", "Фильм 3", "Фильм 4"),
                false,
                null
        );
        when(movieQuizBot.startGame(update)).thenReturn(reply);

        when(telegramClient.execute(any(SendMessage.class)))
                .thenThrow(new TelegramApiException());

        commandMovieQuiz.execute(update);
        verify(telegramClient).execute(any(SendMessage.class));
    }

    private Update getUpdate() {
        Update update = new Update();
        Chat chat = new Chat(15L, "");
        Message message = new Message();
        message.setChat(chat);
        message.setText("/playmoviequiz");
        update.setMessage(message);
        return update;
    }
}
