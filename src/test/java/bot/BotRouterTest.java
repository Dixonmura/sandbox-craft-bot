package bot;

import bot.utils.ReplyUtils;
import command.CommandDispatcher;
import markups.VacancyKeyboardKey;
import movie_quiz.bot.BotReply;
import movie_quiz.bot.MovieQuizBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pomodoro.bot.PomodoroBot;
import pomodoro.bot.PomodoroReply;
import vacancy_tracker.bot.VacancyBot;
import vacancy_tracker.bot.VacancyReply;
import vacancy_tracker.bot.VacancyTelegramSender;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotRouterTest {

    @Mock
    private TelegramClient client;
    @Mock
    private CommandDispatcher commandDispatcher;
    @Mock
    private MovieQuizBot movieQuizBot;
    @Mock
    private PomodoroBot pomodoroBot;
    @Mock
    private VacancyBot vacancyBot;
    @Mock
    private VacancyTelegramSender vacancySender;

    private BotRouter botRouter;
    private final Long CHAT_ID = 123L;
    private final Integer MESSAGE_ID = 456;

    @BeforeEach
    void setUp() {
        botRouter = new BotRouter(client,
                commandDispatcher,
                movieQuizBot,
                pomodoroBot,
                vacancyBot,
                vacancySender);
    }

    private Message createMessage(String text) {
        Message message = new Message();
        message.setChat(new Chat(CHAT_ID, "private"));
        message.setText(text);
        message.setMessageId(MESSAGE_ID);
        return message;
    }

    private Update createTextUpdate(String text) {
        Update update = new Update();
        update.setMessage(createMessage(text));
        return update;
    }

    private CallbackQuery createCallbackQuery(String data) {
        CallbackQuery callback = new CallbackQuery();
        callback.setId("callbackId");
        callback.setData(data);

        Message message = new Message();
        message.setChat(new Chat(CHAT_ID, "private"));
        message.setMessageId(MESSAGE_ID);
        callback.setMessage(message);

        return callback;
    }

    private Update createCallbackUpdate(String data) {
        Update update = new Update();
        update.setCallbackQuery(createCallbackQuery(data));
        return update;
    }

    @Test
    @DisplayName("Обновление без сообщения игнорируется")
    void consume_shouldIgnoreUpdateWithoutMessage() {
        Update update = new Update();
        botRouter.consume(update);
        verifyNoInteractions(client, commandDispatcher, movieQuizBot, pomodoroBot, vacancyBot);
    }

    @Test
    @DisplayName("Callback с префиксом 'start' отправляется в CommandDispatcher")
    void consume_callbackWithStart_shouldDispatchToCommandDispatcher() throws TelegramApiException {
        Update update = createCallbackUpdate("start_pomodoro");

        botRouter.consume(update);

        verify(commandDispatcher).dispatch(eq("start_pomodoro"), any(Update.class));
        verify(client).execute(any(AnswerCallbackQuery.class));
    }

    @Test
    @DisplayName("Callback с префиксом 'vacancy_' отправляется в VacancyTelegramSender")
    void consume_callbackWithVacancyPrefix_shouldHandlePagination() {
        Update update = createCallbackUpdate("vacancy_next");

        botRouter.consume(update);

        verify(vacancySender).nextPage(CHAT_ID);
        verifyNoInteractions(vacancyBot);
    }

    @Test
    @DisplayName("Callback с префиксом 'vacancy_prev' отправляется в VacancyTelegramSender")
    void consume_callbackWithVacancyPrev_shouldHandlePagination() {
        Update update = createCallbackUpdate("vacancy_prev");

        botRouter.consume(update);

        verify(vacancySender).prevPage(CHAT_ID);
    }

    @Test
    @DisplayName("Callback для VacancyBot обрабатывается и редактирует сообщение")
    void consume_callbackForVacancyBot_shouldEditMessage() throws TelegramApiException {
        Update update = createCallbackUpdate("SETTING:Регион");

        VacancyReply vacancyReply = new VacancyReply(CHAT_ID, "Текст ответа", VacancyKeyboardKey.REGION_KEYBOARD);
        when(vacancyBot.handleAnswer(update)).thenReturn(vacancyReply);

        SendMessage sendMessage = mock(SendMessage.class);
        when(sendMessage.getText()).thenReturn("Текст ответа");

        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        when(sendMessage.getReplyMarkup()).thenReturn(keyboard);

        try (MockedStatic<ReplyUtils> replyUtils = mockStatic(ReplyUtils.class)) {
            replyUtils.when(() -> ReplyUtils.sendMessageVacancy(vacancyReply)).thenReturn(sendMessage);

            botRouter.consume(update);

            ArgumentCaptor<EditMessageText> editCaptor = ArgumentCaptor.forClass(EditMessageText.class);
            verify(client).execute(editCaptor.capture());
            verify(client).execute(any(AnswerCallbackQuery.class));

            EditMessageText editMsg = editCaptor.getValue();
            assertThat(editMsg.getChatId()).isEqualTo(CHAT_ID.toString());
            assertThat(editMsg.getMessageId()).isEqualTo(MESSAGE_ID);
            assertThat(editMsg.getText()).isEqualTo("Текст ответа");
        }
    }

    @Test
    @DisplayName("Команда /start отправляется в CommandDispatcher")
    void consume_command_shouldDispatchToCommandDispatcher() {
        Update update = createTextUpdate("/start");

        botRouter.consume(update);

        verify(commandDispatcher).dispatch("/start", update);
    }

    @Test
    @DisplayName("Команда игнорируется при активном MovieQuiz")
    void consume_command_shouldIgnore_whenMovieQuizActive() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(true);
        Update update = createTextUpdate("/start");

        botRouter.consume(update);

        verify(commandDispatcher, never()).dispatch(anyString(), any());
        verify(client).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Команда игнорируется при активном Pomodoro")
    void consume_command_shouldIgnore_whenPomodoroActive() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(true);
        Update update = createTextUpdate("/start");

        botRouter.consume(update);

        verify(commandDispatcher, never()).dispatch(anyString(), any());
        verify(client).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Команда игнорируется при настройке VacancyBot")
    void consume_command_shouldIgnore_whenVacancyConfiguring() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(false);
        when(vacancyBot.isConfiguring(CHAT_ID)).thenReturn(true);
        Update update = createTextUpdate("/start");

        botRouter.consume(update);

        verify(commandDispatcher, never()).dispatch(anyString(), any());
        verify(client).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Обычное сообщение при активном MovieQuiz отправляется в MovieQuizBot")
    void consume_plainMessage_shouldHandleMovieQuiz() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(true);
        Update update = createTextUpdate("Ответ на вопрос");

        BotReply botReply = new BotReply("Текст ответа", List.of("1", "2", "3", "4"), false, "image.jpg");
        when(movieQuizBot.handleAnswer(update)).thenReturn(botReply);

        SendPhoto sendPhoto = mock(SendPhoto.class);
        SendMessage sendMessage = mock(SendMessage.class);

        try (MockedStatic<ReplyUtils> replyUtils = mockStatic(ReplyUtils.class)) {
            replyUtils.when(() -> ReplyUtils.sendPhotoQuiz(eq(botReply), eq(CHAT_ID), any())).thenReturn(sendPhoto);
            replyUtils.when(() -> ReplyUtils.sendMessageQuiz(botReply, CHAT_ID)).thenReturn(sendMessage);

            botRouter.consume(update);

            verify(client).execute(any(SendPhoto.class));
            verify(client).execute(any(SendMessage.class));
        }
    }

    @Test
    @DisplayName("Обычное сообщение при активном Pomodoro отправляется в PomodoroBot")
    void consume_plainMessage_shouldHandlePomodoro() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(true);
        Update update = createTextUpdate("Старт");

        PomodoroReply pomodoroReply = new PomodoroReply("Текст ответа", "image.jpg", false);
        when(pomodoroBot.handleAnswer(update)).thenReturn(pomodoroReply);

        SendPhoto sendPhoto = mock(SendPhoto.class);
        SendMessage sendMessage = mock(SendMessage.class);
        when(sendMessage.getText()).thenReturn("Текст ответа");

        try (MockedStatic<ReplyUtils> replyUtils = mockStatic(ReplyUtils.class)) {
            replyUtils.when(() -> ReplyUtils.sendPhotoPomodoro(eq(pomodoroReply), eq(CHAT_ID), any())).thenReturn(sendPhoto);
            replyUtils.when(() -> ReplyUtils.sendMessagePomodoro(pomodoroReply, CHAT_ID)).thenReturn(sendMessage);

            botRouter.consume(update);

            verify(client).execute(any(SendPhoto.class));
            verify(client).execute(any(SendMessage.class));
        }
    }

    @Test
    @DisplayName("Обычное сообщение при настройке VacancyBot отправляется в VacancyBot")
    void consume_plainMessage_shouldHandleVacancyConfiguring() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(false);
        when(vacancyBot.isConfiguring(CHAT_ID)).thenReturn(true);
        Update update = createTextUpdate("Москва");

        VacancyReply vacancyReply = new VacancyReply(CHAT_ID, "Регион обновлён", VacancyKeyboardKey.SETTING_KEYBOARD);
        when(vacancyBot.handleAnswer(update)).thenReturn(vacancyReply);

        SendMessage sendMessage = mock(SendMessage.class);

        try (MockedStatic<ReplyUtils> replyUtils = mockStatic(ReplyUtils.class)) {
            replyUtils.when(() -> ReplyUtils.sendMessageVacancy(vacancyReply)).thenReturn(sendMessage);

            botRouter.consume(update);

            verify(client).execute(any(SendMessage.class));
        }
    }

    @Test
    @DisplayName("Обычное сообщение при активном VacancyBot отправляет сообщение о работе")
    void consume_plainMessage_shouldHandleVacancyActive() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(false);
        when(vacancyBot.isConfiguring(CHAT_ID)).thenReturn(false);
        when(vacancyBot.isActive(CHAT_ID)).thenReturn(true);
        Update update = createTextUpdate("Что-то");

        try (MockedStatic<ReplyUtils> replyUtils = mockStatic(ReplyUtils.class)) {
            replyUtils.when(() -> ReplyUtils.sendMessageVacancy(any(VacancyReply.class)))
                    .thenReturn(mock(SendMessage.class));

            botRouter.consume(update);

            verify(vacancyBot, never()).handleAnswer(any());
            verify(client).execute(any(SendMessage.class));
        }
    }

    @Test
    @DisplayName("Обычное сообщение без активных сессий отправляет подсказку")
    void consume_plainMessage_shouldSendHint() throws TelegramApiException {
        when(movieQuizBot.hasSession(CHAT_ID)).thenReturn(false);
        when(pomodoroBot.hasSession(CHAT_ID)).thenReturn(false);
        when(vacancyBot.isConfiguring(CHAT_ID)).thenReturn(false);
        when(vacancyBot.isActive(CHAT_ID)).thenReturn(false);
        Update update = createTextUpdate("Привет");

        botRouter.consume(update);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getChatId()).isEqualTo(CHAT_ID.toString());
        assertThat(sentMessage.getText()).isEqualTo(RouterMessages.COMMAND_UNDERSTAND_MESSAGE);
    }

    @Test
    @DisplayName("sendPomodoroReply отправляет сообщение и фото")
    void sendPomodoroReply_shouldSendPhotoAndMessage() throws TelegramApiException {
        PomodoroReply reply = new PomodoroReply("Текст", "image.jpg", false);

        SendPhoto sendPhoto = mock(SendPhoto.class);
        SendMessage sendMessage = mock(SendMessage.class);
        when(sendMessage.getText()).thenReturn("Текст");

        try (MockedStatic<ReplyUtils> replyUtils = mockStatic(ReplyUtils.class)) {
            replyUtils.when(() -> ReplyUtils.sendPhotoPomodoro(eq(reply), eq(CHAT_ID), any())).thenReturn(sendPhoto);
            replyUtils.when(() -> ReplyUtils.sendMessagePomodoro(reply, CHAT_ID)).thenReturn(sendMessage);

            botRouter.sendPomodoroReply(CHAT_ID, reply);

            verify(client).execute(any(SendPhoto.class));
            verify(client).execute(any(SendMessage.class));
        }
    }

    @Test
    @DisplayName("sendPomodoroReply отправляет только сообщение если нет фото")
    void sendPomodoroReply_shouldSendOnlyMessage_whenNoImage() throws TelegramApiException {
        PomodoroReply reply = new PomodoroReply("Текст", null, false);

        SendMessage sendMessage = mock(SendMessage.class);
        when(sendMessage.getText()).thenReturn("Текст");

        try (MockedStatic<ReplyUtils> replyUtils = mockStatic(ReplyUtils.class)) {
            replyUtils.when(() -> ReplyUtils.sendMessagePomodoro(reply, CHAT_ID)).thenReturn(sendMessage);

            botRouter.sendPomodoroReply(CHAT_ID, reply);

            verify(client).execute(any(SendMessage.class));
            verify(client, never()).execute(any(SendPhoto.class));
        }
    }

    @Test
    @DisplayName("sendFinalStatsQuestion отправляет вопрос со статистикой")
    void sendFinalStatsQuestion_shouldSendQuestion() throws TelegramApiException {
        String question = "Хотите статистику?";

        botRouter.sendFinalStatsQuestion(CHAT_ID, question);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(client).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getChatId()).isEqualTo(CHAT_ID.toString());
        assertThat(sentMessage.getText()).isEqualTo(question);
        assertThat(sentMessage.getReplyMarkup()).isNotNull();
    }
}