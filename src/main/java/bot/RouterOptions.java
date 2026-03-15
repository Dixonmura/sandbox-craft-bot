package bot;

public enum RouterOptions {
    START_POMODORO("🍅 Pomodoro", "startpomodoro"),
    START_MOVIE_QUIZ("🎬 Movie Quiz", "startmoviequiz"),
    START_VACANCY_TRACKER("💼 Vacancy Tracker", "startvacancybot");

    private final String title;
    private final String commandText;

    RouterOptions(String title, String commandText) {
        this.title = title;
        this.commandText = commandText;
    }

    public String getTitle() {
        return title;
    }

    public String getCommand() {
        return commandText;
    }
}
