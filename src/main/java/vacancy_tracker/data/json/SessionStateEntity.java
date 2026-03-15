package vacancy_tracker.data.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import vacancy_tracker.core.VacancySessionState;

/**
 * Сущность для хранения состояния сессии в JSON-файле.
 * <p>
 * Содержит минимально необходимую информацию для восстановления сессии:
 * идентификатор пользователя и состояние сессии.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionStateEntity {

    private Long userId;
    private VacancySessionState sessionState;

    /**
     * Создаёт сущность из доменных данных.
     *
     * @param userId идентификатор пользователя
     * @param state  состояние сессии
     * @return заполненная сущность для сохранения
     */
    public static SessionStateEntity fromDomain(Long userId, VacancySessionState state) {
        SessionStateEntity entity = new SessionStateEntity();
        entity.setUserId(userId);
        entity.setSessionState(state);
        return entity;
    }

    /**
     * Преобразует сущность в доменное состояние сессии.
     *
     * @return состояние сессии
     */
    public VacancySessionState toDomain() {
        return sessionState;
    }
}
