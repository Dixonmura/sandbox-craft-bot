package bot;

public final class RouterMessages {

    private RouterMessages() {

    }

    //Общий поток сообщений
    public static final String QUIZ_IS_ACTIVE = """
            🎬 Квиз уже запущен.
            ❌ Нельзя одновременно запускать два бота.
            🎥 Сначала завершите работу с ботом Movie Quiz, а потом попробуйте запустить другого.""";

    public static final String POMODORO_IS_ACTIVE = """
            🤖 У вас уже запущен бот Pomodoro.
            🍅 Пожалуйста, сначала завершите текущую сессию, а затем запускайте другого бота.""";

    public static final String VACANCY_IS_CONFIGURE = """
            ⚙️ Сейчас настраивается бот вакансий.
            ✅ Завершите настройку и запуск, а потом можно будет запускать другие боты.""";

    //Сообщения с подсказками
    public static final String VACANCY_IS_ACTIVE = """
            📢 У вас уже запущен планировщик вакансий.
            🛑 Нажмите «Стоп», чтобы остановить уведомления. 👇""";

    public static final String COMMAND_UNDERSTAND_MESSAGE = """
            📎 Сейчас я понимаю только команды.
            📋 Выберите команду в меню или введите её вручную.""";
}
