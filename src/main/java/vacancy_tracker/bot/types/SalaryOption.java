package vacancy_tracker.bot.types;

public enum SalaryOption {

    NOT_TAKE_SALARY("Не учитывать");

    private final String title;

    SalaryOption(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
