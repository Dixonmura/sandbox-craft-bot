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

        return new BotReply(MovieQuizMessages.GUESS_MOVIE,
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
            builder.append(MovieQuizMessages.ANSWER_WITHOUT_SESSION);
            return new BotReply(builder.toString(),
                    List.of(),
                    true,
                    null);
        }

        if (message.getText().equalsIgnoreCase(MovieQuizMessages.END_GAME_BUTTON)) {
            int score = manager.getScore();
            String rank = MovieQuizRank.fromScore(score);
            builder.append(String.format(MovieQuizMessages.ANSWER_END_GAME_WITH_RANK, score, rank));
            sessions.remove(chatId);
            log.info("Завершение игровой сессии по желанию игрока, chatId={}", chatId);

            return new BotReply(builder.toString(), List.of(), true, null);
        }

        boolean checkAnswer = manager.checkAnswer(message.getText());

        if (!checkAnswer) {
            String rightAnswer = manager.getRightAnswer();
            builder.append(String.format(MovieQuizMessages.WRONG_ANSWER, rightAnswer));
        } else {
            builder.append(String.format(MovieQuizMessages.RIGHT_ANSWER, manager.getScore()));
        }

        Optional<QuestionView> questions = manager.getNextQuestion();

        if (questions.isEmpty()) {
            int score = manager.getScore();
            builder.append(String.format(
                    MovieQuizMessages.END_GAME_MESSAGE,
                    score,
                    MovieQuizRank.fromScore(score)));

            sessions.remove(chatId);
            log.info("Завершение игровой сессии по логике игры, chatId={}", chatId);

            return new BotReply(builder.toString(), List.of(), true, null);
        } else {
            Movie current = manager.getCurrentMovie();
            List<String> titles = questions.get().movieTitles();
            builder.append(MovieQuizMessages.NEXT_QUESTION);
            return new BotReply(builder.toString(), titles, false, current.imageFileName());
        }
    }

    public boolean hasSession(Long chatID) {
        return sessions.containsKey(chatID);
    }
}
