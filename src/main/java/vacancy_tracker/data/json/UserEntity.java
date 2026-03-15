package vacancy_tracker.data.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import vacancy_tracker.core.User;
import vacancy_tracker.core.UserSettingState;
import vacancy_tracker.core.UserSettings;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * Сущность для хранения пользователя в JSON-файле.
 * Отделена от доменной модели {@link User} для гибкости сериализации.
 * Содержит все поля, необходимые для восстановления состояния пользователя.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntity {

    private Long userId;
    private ZoneOffset utcOffset;
    private UserSettingState userSettingState;

    private Integer regionCode;
    private Integer experienceFrom;
    private Integer salaryFrom;
    private String wordForSearch;
    private LocalTime notificationTime;
    private Instant lastRequestTime;

    public UserEntity() {
        // Конструктор по умолчанию для Jackson
    }

    /**
     * Создаёт Entity из доменной модели User.
     *
     * @param user доменная модель пользователя
     * @return заполненная сущность для сохранения
     */
    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.userId = user.getUserId();
        entity.utcOffset = user.getUtcOffset();
        entity.userSettingState = user.getSettingState();

        if (user.getSettings() != null) {
            entity.regionCode = user.getSettings().getRegionCode();
            entity.experienceFrom = user.getSettings().getExperienceFrom();
            entity.salaryFrom = user.getSettings().getSalaryFrom();
            entity.wordForSearch = user.getSettings().getWordForSearch();
            entity.notificationTime = user.getSettings().getNotificationTime();
            entity.lastRequestTime = user.getSettings().getLastRequestTime();
        }

        return entity;
    }

    /**
     * Восстанавливает доменную модель User из Entity.
     * Всегда создаёт {@link UserSettings}, даже если поля null.
     *
     * @return восстановленный пользователь
     */
    public User toDomain() {
        User user = new User(userId);
        user.updateUtcOffset(utcOffset);
        user.updateSettingState(userSettingState);

        UserSettings settings = new UserSettings(
                regionCode,
                experienceFrom,
                salaryFrom,
                wordForSearch,
                notificationTime
        );
        if (lastRequestTime != null) {
            settings.updateRequestTime(lastRequestTime);
        }
        user.updateSettings(settings);

        return user;
    }
}