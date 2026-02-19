package vacancy_tracker.bot.types;

public enum KeyWordOption {

    LEAVE_IT_EMPTY("Оставить пустым");

    private final String title;

    KeyWordOption(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
