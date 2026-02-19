import bot.BotRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.Config;
import config.ConfigReaderEnvironment;
import config.SystemEnvProvider;
import okhttp3.OkHttpClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import vacancy_tracker.bot.VacancyBot;
import vacancy_tracker.core.ScheduledNotificationService;
import vacancy_tracker.core.UserService;
import vacancy_tracker.data.InMemoryUserRepository;
import vacancy_tracker.presentation.UpdateMapper;
import vacancy_tracker.presentation.UserCommandParser;
import vacancy_tracker.presentation.VacancyCommandDispatcher;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Application {
    public static void main(String[] args) {


        ConfigReaderEnvironment configReader = new ConfigReaderEnvironment(new SystemEnvProvider());
        Config token = configReader.reader();
        ObjectMapper mapper = new ObjectMapper();
        TelegramUrl url = TelegramUrl.DEFAULT_URL;
        OkHttpClient myClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        TelegramClient telegramClient = new OkHttpTelegramClient(mapper, myClient, token.botToken(), url);

        UserService userService = new UserService(new InMemoryUserRepository());
        VacancyBot vacancyBot = new VacancyBot(
                userService,
                new UpdateMapper(),
                new UserCommandParser(),
                new VacancyCommandDispatcher(userService, new ScheduledNotificationService())
        );

        try (TelegramBotsLongPollingApplication botApplication = new TelegramBotsLongPollingApplication()) {
            botApplication.registerBot(token.botToken(), new BotRouter(telegramClient, vacancyBot));
            System.out.println("Бот запущен!");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
