package vacancy_tracker.bot.types;

public enum NavigationAction {

    FURTHER("Далее"),
    RETURN("Вернуться");

    private final String title;

    NavigationAction(String title) {
        this.title = title;
    }


    public String getTitle() {
        return title;
    }
}
