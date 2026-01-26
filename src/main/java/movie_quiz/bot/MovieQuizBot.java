package movie_quiz.bot;

import bot.utils.CsvResourceReader;
import movie_quiz.service.GameManager;
import movie_quiz.core.Movie;
import movie_quiz.core.MovieQuizRank;
import movie_quiz.core.QuestionView;
import org.apache.logging.log4j.LogManager;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Основной сервис кино-квиза.
 * Управляет сессиями игр, стартом и обработкой ответов.
 */
public class MovieQuizBot {

    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(MovieQuizBot.class);
    private final List<Movie> movies;
    private final Map<Long, GameManager> sessions = new HashMap<>();

    public MovieQuizBot() {
        CsvResourceReader reader = new CsvResourceReader();

        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("assets/movies/movies.csv")) {

            this.movies = reader.read(is, ',', row -> new Movie(row[0], row[1]));
        } catch (IOException e) {
            log.error("Не удалось прочитать файл movies.csv", e);
            throw new UncheckedIOException(e);
        }
    }

    public MovieQuizBot(List<Movie> listMovies) {
        this.movies = listMovies;
    }

    /**
     * Запускает новую игру для пользователя и возвращает первый вопрос.
     */
    public BotReply startGame(Update update) {
        Long chatId = update.getMessage().getChatId();
        GameManager manager = new GameManager(movies);
        sessions.put(chatId, manager);

        QuestionView movieTitles = manager.getNextQuestion().orElseThrow();
        Movie current = manager.getCurrentMovie();

        log.info("Старт новой сессии MovieQuiz для chatId={}", chatId);

        return new BotReply("Угадай фильм по кадру \uD83C\uDFA5✨",
                movieTitles.movieTitles(),
                false,
                current.imageFileName());
    }

    /**
     * Обрабатывает ответ пользователя и возвращает следующий шаг игры.
     */
    public BotReply handleAnswer(Update update) {
        Long chatId = update.getMessage().getChatId();
        GameManager manager = sessions.get(chatId);
        StringBuilder builder = new StringBuilder();
        Message message = update.getMessage();

        if (manager == null) {
            log.warn("Ответ без активной игровой сессии: manager is null, chatId={}", chatId);
            builder.append("Игры не существует! \uD83D\uDEAB\n")
                    .append("Сначала начните новую игру с помощью команды «/playMovieQuiz» \uD83C\uDFAC\n");
            return new BotReply(builder.toString(),
                    List.of(),
                    true,
                    null);
        }

        if (message.getText().equalsIgnoreCase("Завершить игру \uD83C\uDFAC\uD83C\uDFC1")) {
            builder.append("Игра завершена по вашему желанию! \uD83C\uDFAC Вы набрали ")
                    .append(manager.getScore())
                    .append(" очков. \uD83C\uDFC6\n")
                    .append("Вам присваивается звание: ")
                    .append(MovieQuizRank.fromScore(manager.getScore()));

            sessions.remove(chatId);
            log.info("Завершение игровой сессии по желанию игрока, chatId={}", chatId);

            return new BotReply(builder.toString(), List.of(), true, null);
        }

        boolean checkAnswer = manager.checkAnswer(message.getText());

        if (!checkAnswer) {
            String rightAnswer = manager.getRightAnswer();
            builder.append("К сожалению ответ не верный. \uD83D\uDE14\n")
                    .append("Правильный ответ: ")
                    .append(rightAnswer)
                    .append(" ✅\n\n");
        } else {
            builder.append("Это правильный ответ! \uD83C\uDF89 ")
                    .append("Поздравляем! \uD83C\uDFC6")
                    .append("\nВаши очки: ")
                    .append(manager.getScore())
                    .append(" ⭐\n\n");
        }

        Optional<QuestionView> questions = manager.getNextQuestion();

        if (questions.isEmpty()) {
            builder.append("Игра завершена! \uD83C\uDFAC Вы набрали ")
                    .append(manager.getScore())
                    .append(" очков. \uD83C\uDFC6\n")
                    .append("Вам присваивается звание: ")
                    .append(MovieQuizRank.fromScore(manager.getScore()));

            sessions.remove(chatId);
            log.info("Завершение игровой сессии по логике игры, chatId={}", chatId);

            return new BotReply(builder.toString(), List.of(), true, null);
        } else {
            Movie current = manager.getCurrentMovie();
            List<String> titles = questions.get().movieTitles();
            builder.append("Следующий вопрос: ➡\uFE0F\n");
            return new BotReply(builder.toString(), titles, false, current.imageFileName());
        }
    }

    public boolean hasSession(Long chatID) {
        return sessions.containsKey(chatID);
    }
}
