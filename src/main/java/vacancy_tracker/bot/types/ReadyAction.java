package vacancy_tracker.bot.types;

public enum ReadyAction {

    YES("Да"),
    NO("Нет"),
    COMPLETE("Начать");

    private final String title;

    ReadyAction(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
