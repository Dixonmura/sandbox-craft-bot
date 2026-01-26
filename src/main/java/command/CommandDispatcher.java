package command;

import movie_quiz.bot.MovieQuizBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pomodoro.bot.PomodoroBot;

import java.util.HashMap;
import java.util.Map;

/**
 * Диспетчер команд Telegram-бота.
 * По тексту команды находит соответствующую реализацию {@link Command} и выполняет её.
 */
public class CommandDispatcher {

    private static final Logger log = LogManager.getLogger(CommandDispatcher.class);

    private final Map<String, Command> commandMap = new HashMap<>();

    /**
     * Регистрирует базовый набор команд бота.
     *
     * @param telegramClient клиент Telegram для отправки ответов
     * @param quizBot        экземпляр квиз-бота для игровых команд
     * @param pomodoroBot    экземпляр помодоро бота
     */
    public CommandDispatcher(TelegramClient telegramClient, MovieQuizBot quizBot, PomodoroBot pomodoroBot) {
        commandMap.put("/start", new CommandStart(telegramClient));
        commandMap.put("/playmoviequiz", new CommandMovieQuiz(telegramClient, quizBot));
        commandMap.put("/startpomodoro", new CommandPomodoro(telegramClient, pomodoroBot));
    }

    /**
     * Находит и выполняет команду по тексту сообщения.
     * Если команда не найдена, пишет предупреждение в лог.
     *
     * @param commandText полный текст введённой команды
     * @param update      исходное обновление Telegram
     */
    public void dispatch(String commandText, Update update) {
        String commandKey = commandText.split("\\s")[0].toLowerCase();
        Command command = commandMap.get(commandKey);

        log.info("Обработка команды '{}'", commandKey);

        if (command != null) {
            command.execute(update);
        } else {
            log.warn("Команда '{}' не найдена, исходный текст: '{}'", commandKey, commandText);
        }
    }
}

