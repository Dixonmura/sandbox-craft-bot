package command;

import bot.RouterOptions;
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
import pomodoro.bot.PomodoroMessages;
import pomodoro.bot.PomodoroSender;
import vacancy_tracker.bot.VacancyBot;
import vacancy_tracker.bot.VacancyMessages;
import vacancy_tracker.core.ScheduledNotificationService;
import vacancy_tracker.core.UserService;
import vacancy_tracker.data.json.JsonSessionStateRepository;
import vacancy_tracker.data.json.JsonUserRepository;
import vacancy_tracker.data.repository.SessionStateRepository;
import vacancy_tracker.data.repository.UserRepository;
import vacancy_tracker.presentation.UpdateMapper;
import vacancy_tracker.presentation.UserCommandParser;
import vacancy_tracker.presentation.VacancyCommandDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {

    @Mock
    TelegramClient telegramClient;
    @Mock
    PomodoroSender pomodoroSender;
    UserService userService;
    @Mock
    ScheduledNotificationService notificationService;
    UserRepository userRepository;
    SessionStateRepository stateRepository;
    MovieQuizBot movieQuizBot;
    PomodoroBot pomodoroBot;
    VacancyBot vacancyBot;
    CommandDispatcher commandDispatcher;

    @BeforeEach
    void setUp() {
        userRepository = new JsonUserRepository();
        stateRepository = new JsonSessionStateRepository();
        userService = new UserService(userRepository, stateRepository);
        movieQuizBot = new MovieQuizBot();
        pomodoroBot = new PomodoroBot(pomodoroSender);
        vacancyBot = new VacancyBot(
                userService,
                new UpdateMapper(),
                new UserCommandParser(),
                new VacancyCommandDispatcher(userService, notificationService));
        commandDispatcher = new CommandDispatcher(telegramClient, movieQuizBot, pomodoroBot, vacancyBot);
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
                .contains(CommandMessages.START_COMMAND_MESSAGE);
    }

    @Test
    @DisplayName("Проверка вызова команды startmoviequiz")
    void dispatch_shouldCallMovieQuizCommand_whenMovieQuiz() throws TelegramApiException {
        Update update = getUpdate(RouterOptions.START_MOVIE_QUIZ.getCommand(), 17L);

        commandDispatcher.dispatch(update.getMessage().getText(), update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertThat(sendMessage.getChatId()).isEqualTo("17");
        assertThat(sendMessage.getText()).contains("Угадай фильм по кадру");
    }

    @Test
    @DisplayName("Проверка вызова команды startpomodoro")
    void dispatch_shouldCallPomodoroCommand_whenUpdateTextIsStartPomodoro() throws TelegramApiException {
        Update update = getUpdate(RouterOptions.START_POMODORO.getCommand(), 17L);

        commandDispatcher.dispatch(update.getMessage().getText(), update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertThat(sendMessage.getChatId()).isEqualTo("17");
        assertThat(sendMessage.getText()).contains(PomodoroMessages.WELCOME_MESSAGE);
    }

    @Test
    @DisplayName("Проверка вызова команды startvacancybot")
    void dispatch_shouldCallVacancyBotCommand_whenUpdateTextIsStartVacancyTracker() throws TelegramApiException {
        Update update = getUpdate(RouterOptions.START_VACANCY_TRACKER.getCommand(), 17L);

        commandDispatcher.dispatch(update.getMessage().getText(), update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertThat(sendMessage.getChatId()).isEqualTo("17");
        assertThat(sendMessage.getText()).contains(VacancyMessages.START_MESSAGE);
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