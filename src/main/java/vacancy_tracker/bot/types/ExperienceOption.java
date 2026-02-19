package vacancy_tracker.bot.types;

public enum ExperienceOption {

    WITHOUT_EXPERIENCE("Без опыта");

    private final String title;

    ExperienceOption(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
