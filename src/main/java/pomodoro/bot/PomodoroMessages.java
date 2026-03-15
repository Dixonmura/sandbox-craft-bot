package pomodoro.bot;

public final class PomodoroMessages {

    private PomodoroMessages() {
    }

    public static final String MESSAGE_WITHOUT_STATS = """
            ⏰ Сессия завершена!\s
            
            📊 Статистика ещё не накопилась — она появится после первого рабочего цикла 🍅
            Вернись и заверши его, тогда покажу крутой отчёт! 😊""";

    public static final String WELCOME_MESSAGE = """
            Метод «Помодоро» — это работа короткими рывками с паузами 🍅
            Один «помидор» = сначала работа, потом короткий отдых — так легче не выгореть и не залипать в телефоне 💪
            
            Как будем работать:
            1️⃣ Ты задаёшь длительность рабочего интервала в минутах
            2️⃣ Я запускаю таймер и напомню, когда пора отдыхать ⏱️
            3️⃣ За каждый завершённый помидор ты копишь прогресс и получаешь звания 🏅
            
            Напиши, на сколько минут поставить первый рабочий интервал (только цифру, число больше 0️⃣).
            """;

    public static final String DONT_UNDERSTAND_MESSAGE = "Не понял сообщение \uD83E\uDD14\n" +
            "Для нужной команды нажмите на соответствующую кнопку на клавиатуре ниже ⬇\uFE0F";

    public static final String START_MESSAGE = "Старт \uD83D\uDE80";

    public static final String MOTIVATION_REPLY = "Отличный настрой! Отсчет пошел! ⏱\uFE0F";

    public static final String DOUBLE_CALL = "Тссс… сессия уже идёт \uD83E\uDD2B\n" +
            "Подождите завершения текущего цикла ⏳";

    public static final String PAUSE_MESSAGE = "Пауза ⏸\uFE0F";

    public static final String CANSEL_CURRENT_CYCLE = "Текущий цикл отменён ⏹\uFE0F";

    public static final String END_SEANCE_MESSAGE = "Завершить сеанс ✅";

    public static final String QUESTION_STATS_MESSAGE = "📊 Хотите вывести статистику за последние 30 дней?";

    public static final String NO_ANSWER_MESSAGE = "Нет ❌";

    public static final String YES_ANSWER_MESSAGE = "Да 📊";

    public static final String LIMIT_IS_UP_MESSAGE = "Уважаемый пользователь, сессия превысила лимит времени существования и будет закрыта ⏳\uD83D\uDEAA";

    public static final String WARNED_LIMIT_MESSAGE = """
            Уважаемый пользователь, с момента первого запуска сессии прошло уже более 14 часов ⏰
            В скором времени сессия будет закрыта по достижению лимита ⏳
            """;

    public static final String SHORT_REST_MESSAGE = "Пора сделать короткий перерыв! \uD83E\uDDD8\u200D♂\uFE0F☕";

    public static final String LONG_REST_MESSAGE = "Пора сделать длинный перерыв! \uD83C\uDF34☕";

    public static final String END_REST_MESSAGE = "Перерыв окончен, поехали дальше! \uD83D\uDCAA";

    public static final String WRONG_VALUE_MESSAGE = "Кажется, что-то пошло не так \uD83E\uDD14\n" +
            "Пожалуйста, введите запрошенное значение.";

    public static final String WRONG_VALUE_NOT_INTEGER_MESSAGE = "Нужно ввести целое число, больше 0\uFE0F⃣ \uD83D\uDE42";

    public static final String WRONG_VALUE_NOT_POSITIVE_MESSAGE = "Число должно быть больше 0, попробуйте ещё раз \uD83D\uDD01";

    public static final String CREATE_WORK_MESSAGE = "Период рабочего цикла определён ✅\n" +
            "Теперь отправьте период короткого отдыха в минутах ⏱\uFE0F";

    public static final String CREATE_SHORT_REST_MESSAGE = "Период короткого отдыха определён ✅\n" +
            "Далее отправьте период длинного отдыха в минутах ⏱\uFE0F";

    public static final String CREATE_LONG_REST_MESSAGE = "Период длинного отдыха определён ✅\n" +
            "Теперь отправьте количество рабочих циклов до длинного отдыха \uD83D\uDD01";

    public static final String ALL_PERIODS_CREATE_MESSAGE = "Количество циклов работы до длинного отдыха определено, теперь можно начинать! \nОжидаю команду \"Старт \uD83D\uDE80\"!";

    public static final String CLOSING_MESSAGE_TEMPLATE = """
            Сессия завершена. ✅
            Совершено рабочих циклов: %d 💼
            Вам присваивается звание: %s \uD83C\uDFC5""";


    public static final String END_MESSAGE_WITHOUT_STATS = "✅ Сессия завершена! Отличная работа, возвращайся, когда будешь готов к новой \uD83D\uDE80";
}