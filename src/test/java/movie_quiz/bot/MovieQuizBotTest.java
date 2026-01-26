package movie_quiz.bot;

import movie_quiz.core.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovieQuizBotTest {

    MovieQuizBot bot;

    @BeforeEach
    void setUp() {
        List<Movie> movies = List.of(new Movie("Back to the Future", "Назад в будущее"));
        bot = new MovieQuizBot(movies);
    }

    @Test
    @DisplayName("Проверка метода на возвращение не пустого текста вопроса")
    void startGame_shouldReturnNonEmptyQuestion_whenChatStartsGame() {
        Update update = makeUpdate(1L, "/playMovieQuiz");
        BotReply answerBot = bot.startGame(update);

        assertThat(answerBot).isNotNull();

        assertThat(answerBot.text())
                .isNotNull()
                .isNotEmpty()
                .isNotBlank();
        assertThat(answerBot.movieTitles())
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @DisplayName("Проверка метода обработки ответа пользователя")
    void handleAnswer_shouldReturnAppropriateResult_whenDataIsValid() {
        Update update1 = makeUpdate(3L, "/playMovieQuiz");
        Update update2 = makeUpdate(5L, "/playMovieQuiz");

        Update updateRightAnswer = makeUpdate(3L, "назад в будущее");
        Update updateWrongAnswer = makeUpdate(5L, "вперёд в будущее");

        bot.startGame(update1);
        bot.startGame(update2);

        BotReply goodResult = bot.handleAnswer(updateRightAnswer);
        BotReply badResult1 = bot.handleAnswer(updateWrongAnswer);
        BotReply badResult2 = bot.handleAnswer(updateWrongAnswer);

        assertThat(goodResult.text())
                .isNotNull()
                .isNotBlank()
                .contains("Это правильный ответ!");
        assertThat(goodResult.isFinished()).isTrue();

        assertThat(badResult1.text())
                .contains("К сожалению ответ не верный.")
                .contains("Игра завершена!");
        assertThat(badResult1.isFinished()).isTrue();
        assertThat(badResult2.text())
                .contains("Игры не существует!");
        assertThat(badResult2.isFinished()).isTrue();
    }

    private Update makeUpdate(long chatID, String messageText) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat(chatID, "");
        message.setChat(chat);
        message.setText(messageText);
        update.setMessage(message);

        return update;
    }
}