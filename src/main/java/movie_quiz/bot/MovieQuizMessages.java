package movie_quiz.bot;

public final class MovieQuizMessages {

    private MovieQuizMessages() {

    }

    public static final String GUESS_MOVIE = "Угадай фильм по кадру \uD83C\uDFA5✨";

    public static final String ANSWER_WITHOUT_SESSION = """
            Игры не существует! \uD83D\uDEAB
            Сначала начните новую игру с помощью команды «/playMovieQuiz» \uD83C\uDFAC""";

    public static final String END_GAME_BUTTON = "Завершить игру \uD83C\uDFAC\uD83C\uDFC1";

    public static final String ANSWER_END_GAME_WITH_RANK = """
            Игра завершена по вашему желанию! \uD83C\uDFAC 
            Вы набрали %d очков. \uD83C\uDFC6
            Вам присваивается звание: %s""";

    public static final String WRONG_ANSWER = """
            К сожалению ответ не верный. \uD83D\uDE14
            Правильный ответ: %s ✅""";

    public static final String RIGHT_ANSWER = """
            Это правильный ответ! \uD83C\uDF89
            Поздравляем! \uD83C\uDFC6
            Ваши очки: %d ⭐""";

    public static final String END_GAME_MESSAGE = """
            Игра завершена! \uD83C\uDFAC 
            Вы набрали %d очков. \uD83C\uDFC6
            Вам присваивается звание: %s""";


    public static final String NEXT_QUESTION = "Следующий вопрос: ➡\uFE0F\n";
}
