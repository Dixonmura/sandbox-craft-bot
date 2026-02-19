package vacancy_tracker.bot.types;

public enum StartBotOption {

    START_BOT("Старт");


    private final String title;

    StartBotOption(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
