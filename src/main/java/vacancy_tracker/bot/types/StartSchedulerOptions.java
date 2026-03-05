package vacancy_tracker.bot.types;

public enum StartSchedulerOptions {

    START_SCHEDULER("🚀 Запустить планировщик");

    private final String title;

    StartSchedulerOptions(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
