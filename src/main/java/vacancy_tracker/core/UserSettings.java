package vacancy_tracker.core;

import lombok.Getter;

import java.time.Instant;

/**
 * Настройки пользователя, для поиска вакансий.
 * Хранят выбранный регион, фильтры и время нотификации.
 */
@Getter
public class UserSettings {

    private Integer regionCode;
    private Integer experienceFrom;
    private Integer salaryFrom;
    private String wordForSearch;
    private Instant notificationTime;

    /**
     * Создает настройки пользователя с заданными параметрами.
     * Все параметры могут быть null кроме notificationTime.
     *
     * @param regionCode       код региона поиска вакансий
     * @param experienceFrom   минимальный опыт работы (в годах)
     * @param salaryFrom       минимальная ожидаемая заработная плата (в рублях)
     * @param wordForSearch    ключевое слово для поиска вакансий
     * @param notificationTime время будущих уведомлений в UTC
     * @throws IllegalArgumentException если notificationTime null
     */
    public UserSettings(Integer regionCode, Integer experienceFrom, Integer salaryFrom, String wordForSearch, Instant notificationTime) {
        this.regionCode = regionCode;
        this.experienceFrom = experienceFrom;
        this.salaryFrom = salaryFrom;
        this.wordForSearch = wordForSearch;
        this.notificationTime = notificationTime;

        if (notificationTime == null) {
            throw new IllegalArgumentException("NotificationTime не может быть null");
        }
    }

    public void updateRegionCode(int regionCode) {
        this.regionCode = regionCode;
    }

    public void updateExperienceFrom(int experienceFrom) {
        this.experienceFrom = experienceFrom;
    }

    public void updateSalaryFrom(int salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public void updateWordForSearch(String wordForSearch) {
        this.wordForSearch = wordForSearch;
    }

    public void updateNotificationTime(Instant notificationTime) {
        this.notificationTime = notificationTime;
    }

    /**
     * Метод проверки установки времени нотификации.
     *
     * @return результат проверки установки нотификации
     */
    public boolean isNotificationScheduleReady() {
        return notificationTime != null;
    }
}
