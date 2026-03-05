package vacancy_tracker.bot.types;

public enum StartStopBotOption {

    START_BOT("Старт"),
    STOP_BOT("🛑 Завершить работу бота"),
    OUT_IN_ROUTER("🏠 Главное меню");

    private final String title;

    StartStopBotOption(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
