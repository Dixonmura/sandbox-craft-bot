package vacancy_tracker.bot;

public final class VacancyMessages {

    private VacancyMessages() {
    }

    // Приветственные сообщения и общий поток

    public static final String START_MESSAGE = """
            Привет! 👨‍💻
            Я бот‑помощник по поиску вакансий в любой сфере.
            Помогу настроить фильтры по региону, опыту, зарплате и времени уведомлений,
            а потом каждый день присылать тебе актуальные предложения 💼
            Нажми «Старт», чтобы выбрать настройки и запустить планировщик 🚀""";

    public static final String AFTER_START_MESSAGE = """
            Vacancy Tracker Bot приветствует вас! 🙌
            Для удобства в боте будет использоваться часовой пояс UTC +3(МСК) 🌍""";

    public static final String ENTER_UTC_OFFSET = """
            ⏰ Выберите смещение часового пояса в формате UTC
            или введите его вручную.
            Например: +07:00 или -11:30""";

    public static final String UPDATED_UTC_MESSAGE = """
            ✅ Смещение часового пояса успешно обновлено.""";

    public static final String START_WHEN_ACTIVE_MESSAGE = """
            ⚙️ В настоящий момент планировщик уже работает в фоновом режиме.
            Если вы хотите остановить работу бота — нажмите кнопку «🛑 Завершить работу бота»
            Или нажмите «Старт» для выбора новой конфигурации 🔁
            """;

    public static final String REGION_MESSAGE = """
            📍 Выберите регион из списка или введите номер региона в виде целого числа.
            Например: 65, 77, 05""";

    public static final String UPDATE_REGION_MESSAGE = """
            ✅ Регион для поиска вакансий обновлён.""";

    public static final String EXPERIENCE_MESSAGE = """
            🧱 Введите минимальный опыт работы в виде целого числа (лет).
            Например: 1, 3, 5""";

    public static final String UPDATE_EXPERIENCE_MESSAGE = """
            ✅ Минимальный опыт работы для поиска вакансий обновлён.""";

    public static final String SALARY_MESSAGE = """
            💰 Введите минимальную ожидаемую заработную плату в виде целого числа.
            Например: 70000""";

    public static final String UPDATE_SALARY_MESSAGE = """
            ✅ Минимальная ожидаемая зарплата для поиска вакансий обновлена.""";

    public static final String KEYWORD_MESSAGE = """
            🔎 Выберите ключевое слово для поиска соответствующих вакансий
            или введите свой вариант вручную.
            Например: Java Developer""";

    public static final String UPDATE_KEYWORD_MESSAGE = """
            ✅ Ключевое слово для поиска соответствующих вакансий обновлено.""";

    public static final String NOTIFY_TIME_MESSAGE = """
            ⏰ Выберите время нотификации (это обязательное поле)
            или введите его вручную.
            Например: 13:35 или 18 55""";

    public static final String UPDATE_NOTIFY_TIME_MESSAGE = """
            ✅ Время нотификаций для ежедневного оповещения обновлено.""";

    public static final String READY_MESSAGE = """
            🎯 Настройки завершены!
            Нажмите кнопку «Запустить планировщик» или вернитесь в меню настроек 🔧""";

    public static final String READY_START_MESSAGE = """
            🚀 Планировщик запущен!
            Удачного поиска и до встречи! 🙂""";

    public static final String BACK_INTO_SETTINGS_MESSAGE = """
            ↩️ Возврат в главное меню настроек.""";

    public static final String STOP_MESSAGE = """
            ⛔ Вы уверены, что хотите остановить работу бота и удалить данные поиска?
            Нажмите: "✅ Подтвердить" или "❌ Отмена"
            """;

    public static final String HOME_MESSAGE_WHEN_CONFIGURING = """
            ⛔ Вы уверены, что хотите выйти в главное меню?
            (Вы не завершили настройку и запуск VacancyBot, ранее введённые данные будут удалены)
            Нажмите: "✅ Подтвердить" или "❌ Отмена"
            """;

    public static final String HOME_MESSAGE_WHEN_ACTIVE = """
            ✅ Планировщик работает в фоновом режиме, ожидайте уведомлений 😊""";

    public static final String SUCCESSFUL_STOP_MESSAGE = """
            ✅ Планировщик остановлен, данные поиска очищены.
            Спасибо, что пользовались VacancyTrackerBot! 🙏""";

    public static final String SUCCESSFUL_HOME_MESSAGE = """
            ✅ Настройки сброшены.
            Вернулись в главное меню! 🏠""";

    public static final String CONTINUE_MESSAGE = """
            🙌 Отлично! Продолжаем пользоваться VacancyTrackerBot 😊""";


    // Сообщения об ошибках

    public static final String ERROR_UTC_MESSAGE = """
            ⚠️ Неверный формат ввода зоны времени UTC.
            Ожидается что-то вроде: +03:00, +03:30, -05:00""";

    public static final String ERROR_REGION_MESSAGE = """
            ⚠️ Неверный формат ввода региона. (доступны 85 регионов от 1 до 85)
            Введите целое число, например: 65, 77, 5""";

    public static final String ERROR_EXPERIENCE_MESSAGE = """
            ⚠️ Неверный формат ввода минимального опыта работы.
            Ожидается целое число лет, например: 1, 3, 5""";

    public static final String ERROR_SALARY_MESSAGE = """
            ⚠️ Неверный формат ввода минимальной заработной платы.
            Ожидается целое число, например: 70000 или 90000""";

    public static final String ERROR_NOTIFY_TIME_MESSAGE = """
            ⚠️ Неверный формат времени нотификации.
            Ожидается, например: 17:00 или 02 33""";

    public static final String ERROR_READY_MESSAGE = """
            ⚠️ Нельзя запустить планировщик: время уведомлений не задано.
            Пожалуйста, сначала установите время нотификации в настройках.""";

    public static final String ERROR_STOP_MESSAGE = """
            ⚠️ Похоже, был введён некорректный ответ.
            Нажмите: "✅ Подтвердить" или "❌ Отмена"
            """;

    public static final String UNKNOWN_MESSAGE = """
            🤔 Неизвестная команда.
            Попробуйте воспользоваться клавиатурой выше или введите команду корректно.""";
}
