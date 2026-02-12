package vacancy_tracker.core;

import lombok.Getter;

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
        this.settings = userSettings;
    }

    public void updateRegionCode(int regionCode) {
        settings.updateRegionCode(regionCode);
    }

    public void updateExperienceFrom(int experienceFrom) {
        settings.updateExperienceFrom(experienceFrom);
    }

    public void updateSalaryFrom(int salaryFrom) {
        settings.updateSalaryFrom(salaryFrom);
    }

    public void updateWordForSearch(String wordForSearch) {
        settings.updateWordForSearch(wordForSearch);
    }

    public void updateNotificationTime(LocalTime notificationTime) {
        settings.updateNotificationTime(notificationTime);
    }

    public void updateUtcOffset(ZoneOffset zoneOffset) {
        this.utcOffset = zoneOffset;
    }

    public void updateSettingState(UserSettingState settingState) {
        this.settingState = settingState;
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
