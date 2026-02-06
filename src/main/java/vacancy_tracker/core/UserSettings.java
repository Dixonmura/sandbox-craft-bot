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
     * Все параметры могут быть null на этапе построения мастера настроек.
     *
     * @param regionCode       код региона поиска вакансий
     * @param experienceFrom   минимальный опыт работы (в годах)
     * @param salaryFrom       минимальная ожидаемая заработная плата (в рублях)
     * @param wordForSearch    ключевое слово для поиска вакансий
     * @param notificationTime время будущих уведомлений в UTC
     */
    public UserSettings(Integer regionCode, Integer experienceFrom, Integer salaryFrom, String wordForSearch, Instant notificationTime) {
        this.regionCode = regionCode;
        this.experienceFrom = experienceFrom;
        this.salaryFrom = salaryFrom;
        this.wordForSearch = wordForSearch;
        this.notificationTime = notificationTime;
    }

    //TODO дописать методы для обновления отдельных полей, а так же обязательную инициализацию notificationTime
}
