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

        try (TelegramBotsLongPollingApplication botApplication = new TelegramBotsLongPollingApplication()) {
            botApplication.registerBot(token.botToken(), new BotRouter(telegramClient));
            System.out.println("Бот запущен!");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
