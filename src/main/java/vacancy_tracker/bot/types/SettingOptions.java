package vacancy_tracker.bot.types;

public enum SettingOptions {
    UTC_OPTION("🕐 Изменить часовой пояс UTC"),
    REGION("🌍 Регион"),
    MIN_EXPERIENCE("💼 Минимальный опыт"),
    MIN_SALARY("💰 Минимальная зарплата"),
    WORD_FOR_SEARCH("🔍 Слово для поиска"),
    SETTINGS_NOTIFICATION("🔔 Настройки нотификации");

    private final String title;

    SettingOptions(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
