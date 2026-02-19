package vacancy_tracker.bot.types;

public enum StartStopOptions {

    START("Запустить планировщик"),
    STOP("Остановить планировщик");

    private final String title;

    StartStopOptions(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
