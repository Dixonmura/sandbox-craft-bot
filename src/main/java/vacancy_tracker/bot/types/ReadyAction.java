package vacancy_tracker.bot.types;

public enum ReadyAction {

    YES("✅ Подтвердить"),
    NO("❌ Отмена"),
    GO_BACK("⬅️ Назад"),
    COMPLETE("▶️ Запустить поиск");

    private final String title;

    ReadyAction(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
