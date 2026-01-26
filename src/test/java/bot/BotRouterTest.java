package bot;

import command.CommandDispatcher;
import movie_quiz.bot.BotReply;
import movie_quiz.bot.MovieQuizBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pomodoro.bot.PomodoroBot;
import pomodoro.bot.PomodoroReply;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotRouterTest {

    private static final Long CHAT_ID = 123L;

    @Mock
    TelegramClient telegramClient;
    @Mock
    CommandDispatcher commandDispatcher;
    @Mock
    MovieQuizBot movieQuizBot;
    @Mock
    PomodoroBot pomodoroBot;

    private BotRouter botRouter;

    @BeforeEach
    void setUp() {
        botRouter = new BotRouter(telegramClient, commandDispatcher, movieQuizBot, pomodoroBot);
    }

    @Test
    @DisplayName("команда с '/' делегируется в CommandDispatcher")
    void consume_command_shouldDelegateToDispatcher() {
        Update update = createUpdateWithText(CHAT_ID, "/playmoviequiz");

        botRouter.consume(update);

        verify(commandDispatcher).dispatch("/playmoviequiz", update);
        verifyNoMoreInteractions(commandDispatcher);
        verifyNoInteractions(telegramClient);
    }

    @Test
    @DisplayName("обычное сообщение без сессий отправляет системный ответ")
    void consume_plainMessageWithoutSessions_shouldSendSystemMessage() throws Exception {
        Update update = createUpdateWithText(CHAT_ID, "привет");

        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(false);

        botRouter.consume(update);

        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getChatId().equals(String.valueOf(CHAT_ID)) &&
                        msg.getText().contains("Сейчас я понимаю только команды")));
    }

    @Test
    @DisplayName("сообщение при активной сессии квиза уходит в MovieQuizBot")
    void consume_messageWithQuizSession_shouldForwardToMovieQuizBot() throws Exception {
        Update update = createUpdateWithText(CHAT_ID, "ответ");
        BotReply reply = new BotReply(
                "text",
                List.of("A","B","C","D"),
                false,
                "img.png"
        );

        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(true);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(false);
        when(movieQuizBot.handleAnswer(update)).thenReturn(reply);

        botRouter.consume(update);

        verify(movieQuizBot).handleAnswer(update);
        verify(telegramClient).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("сообщение при активной Pomodoro-сессии уходит в PomodoroBot")
    void consume_messageWithPomodoroSession_shouldForwardToPomodoroBot() throws Exception {
        Update update = createUpdateWithText(CHAT_ID, "Старт");
        PomodoroReply reply = new PomodoroReply("text", "img.png", false);

        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(true);
        when(pomodoroBot.handleAnswer(update)).thenReturn(reply);

        botRouter.consume(update);

        verify(pomodoroBot).handleAnswer(update);
        verify(telegramClient).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("sendPomodoroReply отправляет сообщение")
    void sendPomodoroReply_shouldSendMessage() throws Exception {
        PomodoroReply reply = new PomodoroReply("text", "img.png", false);

        botRouter.sendPomodoroReply(CHAT_ID, reply);

        verify(telegramClient).execute(any(SendMessage.class));
    }

    private static Update createUpdateWithText(Long chatId, String text) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat(chatId, "");
        message.setChat(chat);
        message.setText(text);
        update.setMessage(message);
        return update;
    }
}
