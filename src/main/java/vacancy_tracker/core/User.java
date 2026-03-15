package vacancy_tracker.core;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalTime;
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
    private UserSettingState settingState = UserSettingState.NOT_INITIALIZED;

    public User(Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        this.userId = userId;
    }

    public void updateSettings(UserSettings userSettings) {
        if (userSettings == null) {
            ensureSettings();
        } else {
            this.settings = userSettings;
        }
    }

    public void updateRegionCode(int regionCode) {
        ensureSettings();
        settings.updateRegionCode(regionCode);
    }

    public void updateExperienceFrom(int experienceFrom) {
        ensureSettings();
        settings.updateExperienceFrom(experienceFrom);
    }

    public void updateSalaryFrom(int salaryFrom) {
        ensureSettings();
        settings.updateSalaryFrom(salaryFrom);
    }

    public void updateWordForSearch(String wordForSearch) {
        ensureSettings();
        settings.updateWordForSearch(wordForSearch);
    }

    public void updateNotificationTime(LocalTime notificationTime) {
        ensureSettings();
        settings.updateNotificationTime(notificationTime);
    }

    public void updateLastRequestTime(Instant lastRequestTime) {
        ensureSettings();
        settings.updateRequestTime(lastRequestTime);
    }

    public void updateUtcOffset(ZoneOffset zoneOffset) {
        this.utcOffset = zoneOffset;
    }

    public void updateSettingState(UserSettingState settingState) {
        this.settingState = settingState;
    }

    private void ensureSettings() {
        if (settings == null) {
            settings = new UserSettings(
                    null, null, null, null, null
            );
        }
    }

    public boolean hasUtcOffset() {
        return utcOffset != null;
    }

    /**
     * Проверяет, готов ли пользователь к запуску поиска вакансий.
     *
     * @return true, если настройки заданы и расписание уведомлений настроено
     */
    public boolean isSettingsReady() {
        return settings != null && settings.isNotificationScheduleReady();
    }

    public boolean hasSettings() {
        return settings != null;
    }
}
