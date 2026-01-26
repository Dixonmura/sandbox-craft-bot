package command;

import bot.utils.ReplyUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pomodoro.bot.PomodoroBot;
import pomodoro.bot.PomodoroReply;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandPomodoroTest {

    @Test
    void execute_shouldReturn_whenUpdateIsNull() {
        CommandPomodoro command = new CommandPomodoro(mock(TelegramClient.class), mock(PomodoroBot.class));

        command.execute(null);
    }

    @Test
    void execute_shouldReturn_whenUpdateHasNoMessage() {
        TelegramClient telegramClient = mock(TelegramClient.class);
        PomodoroBot pomodoroBot = mock(PomodoroBot.class);
        CommandPomodoro command = new CommandPomodoro(telegramClient, pomodoroBot);

        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(false);

        command.execute(update);

        verifyNoInteractions(telegramClient);
        verifyNoInteractions(pomodoroBot);
    }

    @Test
    void execute_shouldStartPomodoroAndSendMessageWithoutPhoto_whenImagePathIsNull() throws TelegramApiException {
        TelegramClient telegramClient = mock(TelegramClient.class);
        PomodoroBot pomodoroBot = mock(PomodoroBot.class);
        CommandPomodoro command = new CommandPomodoro(telegramClient, pomodoroBot);

        long chatId = 13L;
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);

        PomodoroReply reply = mock(PomodoroReply.class);
        when(reply.imagePath()).thenReturn(null);
        when(pomodoroBot.startPomodoro(update)).thenReturn(reply);

        try (MockedStatic<ReplyUtils> replyUtilsMock = mockStatic(ReplyUtils.class)) {
            replyUtilsMock.when(() -> ReplyUtils.sendMessagePomodoro(reply, chatId))
                    .thenReturn(mock(SendMessage.class));

            command.execute(update);

            replyUtilsMock.verify(() -> ReplyUtils.sendPhotoPomodoro(any(), anyLong(), any()), never());
            verify(telegramClient).execute(any(SendMessage.class));
        }
    }

    @Test
    void execute_shouldSendPhotoAndMessage_whenImagePathIsNotNull() throws TelegramApiException {
        TelegramClient telegramClient = mock(TelegramClient.class);
        PomodoroBot pomodoroBot = mock(PomodoroBot.class);
        CommandPomodoro command = new CommandPomodoro(telegramClient, pomodoroBot);

        long chatId = 46L;
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);

        PomodoroReply reply = mock(PomodoroReply.class);
        when(reply.imagePath()).thenReturn("some/path.png");
        when(pomodoroBot.startPomodoro(update)).thenReturn(reply);

        try (MockedStatic<ReplyUtils> replyUtilsMock = mockStatic(ReplyUtils.class)) {
            replyUtilsMock.when(() -> ReplyUtils.sendPhotoPomodoro(reply, chatId, CommandPomodoro.class.getClassLoader()))
                    .thenReturn(mock(SendPhoto.class));
            replyUtilsMock.when(() -> ReplyUtils.sendMessagePomodoro(reply, chatId))
                    .thenReturn(mock(SendMessage.class));

            command.execute(update);

            verify(telegramClient).execute(any(SendPhoto.class));
            verify(telegramClient).execute(any(SendMessage.class));
        }
    }

    @Test
    void execute_shouldHandleTelegramApiException() throws TelegramApiException {
        TelegramClient telegramClient = mock(TelegramClient.class);
        PomodoroBot pomodoroBot = mock(PomodoroBot.class);
        CommandPomodoro command = new CommandPomodoro(telegramClient, pomodoroBot);

        long chatId = 7L;
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);

        PomodoroReply reply = mock(PomodoroReply.class);
        when(reply.imagePath()).thenReturn(null);
        when(pomodoroBot.startPomodoro(update)).thenReturn(reply);

        try (MockedStatic<ReplyUtils> replyUtilsMock = mockStatic(ReplyUtils.class)) {
            replyUtilsMock.when(() -> ReplyUtils.sendMessagePomodoro(reply, chatId))
                    .thenReturn(mock(SendMessage.class));

            doThrow(new TelegramApiException("test")).when(telegramClient).execute(any(SendMessage.class));

            command.execute(update);

            verify(telegramClient).execute(any(SendMessage.class));
        }
    }
}
