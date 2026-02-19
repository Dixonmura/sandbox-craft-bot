package command;

import movie_quiz.bot.BotReply;
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
import vacancy_tracker.bot.VacancyBot;
import vacancy_tracker.bot.VacancyReply;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandVacancyTest {

    @Mock
    TelegramClient telegramClient;
    @Mock
    VacancyBot vacancyBot;
    CommandVacancy commandVacancy;

    @BeforeEach
    void setUp() {
        commandVacancy = new CommandVacancy(telegramClient, vacancyBot);
    }

    @Test
    @DisplayName("Проверка отправки сообщения при корректной команде /startvacancybot")
    void execute_shouldSendVacancyMessage_whenCommandIsCorrect() throws TelegramApiException {


    }

    @Test
    @DisplayName("Команда /startvacancybot, бот не падает, если TelegramClient кидает TelegramApiException")
    void execute_shouldNotThrow_whenTelegramClientFails() throws TelegramApiException {

    }

    private Update getUpdate() {
        Update update = new Update();
        Chat chat = new Chat(15L, "");
        Message message = new Message();
        message.setChat(chat);
        message.setText("/startvacancybot");
        update.setMessage(message);
        return update;
    }
}