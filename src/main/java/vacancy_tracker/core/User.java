package vacancy_tracker.core;

import lombok.Getter;

import java.time.ZoneOffset;

/**
 * Доменная модель пользователя Telegram.
 * Хранит идентификатор, часовой пояс и настройки поиска вакансий.
 */
@Getter
public class User {

    private final Long userId;
    private UserSettings settings;
    private ZoneOffset utcOffset;

    public User(Long userId) {
        this.userId = userId;

        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
    }

    public void updateSettings(UserSettings userSettings) {
        this.settings = userSettings;
    }

    public void updateUtcOffset(ZoneOffset zoneOffset) {
        this.utcOffset = zoneOffset;
    }

    /**
     * Проверяет, готов ли пользователь к запуску поиска вакансий.
     *
     * @return true, если настройки заданы и расписание уведомлений настроено
     */
    public boolean isSettingsReady() {
        return settings != null && settings.isNotificationScheduleReady();
    }
}
