package vacancy_tracker.data.repository;

import vacancy_tracker.core.VacancySessionState;

import java.util.Map;
import java.util.Optional;

/**
 * Репозиторий для хранения состояний сессий пользователей.
 * <p>
 * Отвечает за сохранение, загрузку и удаление состояний сессий
 * в отдельном от пользовательских данных хранилище.
 * Состояния сессий хранятся отдельно от {@link vacancy_tracker.core.User},
 * так как являются временными и относятся к уровню сервиса, а не к доменной модели.
 */
public interface SessionStateRepository {

    /**
     * Находит состояние сессии пользователя по его ID.
     *
     * @param userId идентификатор пользователя
     * @return Optional с состоянием сессии или пустой Optional, если состояние не найдено
     */
    Optional<VacancySessionState> findByUserId(Long userId);

    /**
     * Сохраняет состояние сессии пользователя.
     *
     * @param userId идентификатор пользователя
     * @param state  состояние сессии для сохранения
     */
    void save(Long userId, VacancySessionState state);

    /**
     * Удаляет состояние сессии пользователя.
     *
     * @param userId идентификатор пользователя
     */
    void delete(Long userId);

    /**
     * Возвращает все сохранённые состояния сессий.
     *
     * @return Map, где ключ - ID пользователя, значение - состояние сессии
     */
    Map<Long, VacancySessionState> findAll();
}
