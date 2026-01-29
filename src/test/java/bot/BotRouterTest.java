package bot;

import command.CommandDispatcher;
import movie_quiz.bot.BotReply;
import movie_quiz.bot.MovieQuizBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pomodoro.bot.PomodoroBot;
import pomodoro.bot.PomodoroReply;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotRouterTest {

    private static final Long CHAT_ID = 33L;

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
                List.of("A", "B", "C", "D"),
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

    @Test
    @DisplayName("Проверка отработки условия невозможности запуска двух ботов одновременно")
    void createAnyBot_shouldCreateOnlyOneBot_whenUserCallOtherBot() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(true);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(false);
        botRouter.consume(createUpdateWithText(CHAT_ID, "/startpomodoro"));

        ArgumentMatcher<SendMessage> twoBotsWarning = msg ->
                msg != null
                        && CHAT_ID.toString().equals(msg.getChatId())
                        && msg.getText() != null
                        && msg.getText().contains("❌ Нельзя одновременно запускать два бота.");

        verify(telegramClient, atLeastOnce())
                .execute(argThat(twoBotsWarning));
    }

    @Test
    @DisplayName("Проверка отработки условия невозможности запуска двух ботов одновременно")
    void createAnyBot_shouldCreateOnlyOneBot_whenPomodoroBotSessionIsExist() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(true);
        botRouter.consume(createUpdateWithText(CHAT_ID, "/playmoviequiz"));

        ArgumentMatcher<SendMessage> twoBotsWarning = msg ->
                msg != null
                        && CHAT_ID.toString().equals(msg.getChatId())
                        && msg.getText() != null
                        && msg.getText().contains("У вас уже запущен бот Pomodoro.");

        verify(telegramClient, atLeastOnce())
                .execute(argThat(twoBotsWarning));
    }

    @Test
    @DisplayName("проверка раннего выхода из consume, когда на вход подается null")
    void consume_nullUpdate_shouldReturnWithoutInteractions() {
        botRouter.consume((Update) null);

        verifyNoInteractions(telegramClient, commandDispatcher, movieQuizBot, pomodoroBot);
    }

    @Test
    @DisplayName("Проверка раннего выхода, когда в consume приходит обновление без сообщения")
    void consume_updateWithoutMessage_shouldReturn() {
        Update update = new Update();
        botRouter.consume(update);

        verifyNoInteractions(telegramClient, commandDispatcher, movieQuizBot, pomodoroBot);
    }

    @Test
    @DisplayName("Pomodoro: при TelegramApiException отправляется fallback")
    void consume_pomodoroReply_shouldSendFallbackOnTelegramException() throws Exception {
        Update update = createUpdateWithText(CHAT_ID, "ответ");
        PomodoroReply reply = new PomodoroReply("text", "img.png", false);

        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(true);
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.handleAnswer(update)).thenReturn(reply);

        doThrow(new TelegramApiException("fall"))
                .when(telegramClient)
                .execute(any(SendMessage.class));

        botRouter.consume(update);

        verify(telegramClient, times(2)).execute(any(SendMessage.class));

        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getChatId().equals(CHAT_ID.toString())
                        && msg.getText() != null
                        && msg.getText().contains("(⚠️ Мотивашку с картинкой отправить не удалось"))
        );
    }

    @Test
    @DisplayName("MovieQuiz: при TelegramApiException отправляется fallback")
    void consume_BotReply_shouldSendFallbackOnTelegramException() throws Exception {
        Update update = createUpdateWithText(CHAT_ID, "ответ");
        BotReply reply = new BotReply("text", List.of("1", "2", "3", "4"), false, ".img");

        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(false);
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(true);
        when(movieQuizBot.handleAnswer(update)).thenReturn(reply);

        doThrow(new TelegramApiException("fall"))
                .when(telegramClient)
                .execute(any(SendMessage.class));

        botRouter.consume(update);

        verify(telegramClient, times(2)).execute(any(SendMessage.class));

        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getChatId().equals(CHAT_ID.toString())
                        && msg.getText() != null
                        && msg.getText().contains("⚠️ Картинку отправить не удалось из-за ошибки соединения."))
        );
    }

    @Test
    @DisplayName("pomodoroBot: при TelegramApiException отправляется fallback")
    void consume_PomodoroReply_shouldSendFallbackOnTelegramException() throws Exception {
        Update update = createUpdateWithText(CHAT_ID, "ответ");
        PomodoroReply reply = new PomodoroReply("text", "path", false);

        doThrow(new TelegramApiException("fall"))
                .when(telegramClient)
                .execute(any(SendMessage.class));

        botRouter.sendPomodoroReply(CHAT_ID, reply);

        verify(telegramClient, times(2)).execute(any(SendMessage.class));

        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getChatId().equals(CHAT_ID.toString())
                        && msg.getText() != null
                        && msg.getText().contains("⚠️ Мотивашку с картинкой отправить не удалось из-за ошибки"))
        );
    }

    @Test
    @DisplayName("проверка отправки сообщения через метод sendFinalStatsQuestion")
    void sendFinalStatsQuestion_shouldSendMessage() throws TelegramApiException {
        botRouter.sendFinalStatsQuestion(CHAT_ID, "final Text");

        verify(telegramClient, times(1)).execute(any(SendMessage.class));
        verify(telegramClient).execute(argThat((SendMessage msg) ->
                msg.getChatId().equals(CHAT_ID.toString())
                        && msg.getText().contains("final")
                        && msg.getReplyMarkup() != null
        ));
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
