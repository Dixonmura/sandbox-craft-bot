package vacancy_tracker.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vacancy_tracker.data.repository.SessionStateRepository;
import vacancy_tracker.data.repository.UserRepository;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application-сервис для сценариев работы с пользователем:
 * создание/загрузка, изменение часового пояса и настроек поиска вакансий.
 */
public class UserService {

    private static final Logger log = LogManager.getLogger(UserService.class);

    private final UserRepository repository;
    private final SessionStateRepository sessionRepository;
    private final Map<Long, VacancySessionState> sessionStates = new ConcurrentHashMap<>();

    public UserService(UserRepository repository, SessionStateRepository sessionRepository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository не может быть null");
        }
        this.repository = repository;
        this.sessionRepository = sessionRepository;
        restoreSessions();
    }

    /**
     * Возвращает существующего пользователя по userId
     * или создаёт нового, если он ещё не сохранён в репозитории.
     * Бросает IllegalArgumentException, если userId null.
     */
    public User getOrCreateUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        return repository.findById(userId).orElseGet(
                () -> {
                    User newUser = new User(userId);
                    newUser.updateUtcOffset(ZoneOffset.ofHours(3));
                    return newUser;
                }
        );
    }

    /**
     * Обновляет смещение часового пояса пользователя по строке формата,
     * поддерживаемом ZoneOffset (например, "+03:00" или "-05:30").
     * Бросает IllegalArgumentException, если userId null.
     */
    public User updateUtcOffset(Long userId, ZoneOffset zoneUtcOffset) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        User user = getOrCreateUser(userId);
        user.updateUtcOffset(zoneUtcOffset);
        repository.saveUser(user);
        return user;
    }

    /**
     * Обновляет настройки (фильтры) пользователя для поиска вакансий.
     * Если пользователь с таким userId не найден, будет создан новый.
     * Бросает IllegalArgumentException, если userId или userSettings null.
     */
    public User updateUserSettings(Long userId, UserSettings userSettings) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        if (userSettings == null) {
            throw new IllegalArgumentException("UserSettings не может быть null");
        }
        User user = getOrCreateUser(userId);
        user.updateSettings(userSettings);
        user.updateSettingState(UserSettingState.READY);
        repository.saveUser(user);

        return user;
    }

    /**
     * Обновляет регион пользователя для поиска вакансий.
     * Если пользователь с таким userId не найден, будет создан новый.
     * Бросает IllegalArgumentException, если userId null.
     */
    public User updateRegionCode(Long userId, int regionCode) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        User user = getOrCreateUser(userId);
        user.updateRegionCode(regionCode);
        repository.saveUser(user);
        return user;
    }

    /**
     * Обновляет минимальный опыт пользователя для поиска вакансий.
     * Если пользователь с таким userId не найден, будет создан новый.
     * Бросает IllegalArgumentException, если userId null.
     */
    public User updateExperienceFrom(Long userId, int experienceFrom) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        User user = getOrCreateUser(userId);
        user.updateExperienceFrom(experienceFrom);
        repository.saveUser(user);
        return user;
    }

    /**
     * Обновляет минимальную ожидаемую зарплату пользователя для поиска вакансий.
     * Если пользователь с таким userId не найден, будет создан новый.
     * Бросает IllegalArgumentException, если userId null.
     */
    public User updateSalaryFrom(Long userId, int salaryFrom) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        User user = getOrCreateUser(userId);
        user.updateSalaryFrom(salaryFrom);
        repository.saveUser(user);
        return user;
    }

    /**
     * Обновляет ключевое слово пользователя для поиска вакансий.
     * Если пользователь с таким userId не найден, будет создан новый.
     * Бросает IllegalArgumentException, если userId null.
     */
    public User updateWordForSearch(Long userId, String wordForSearch) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        User user = getOrCreateUser(userId);
        user.updateWordForSearch(wordForSearch);
        repository.saveUser(user);
        return user;
    }

    /**
     * Обновляет время нотификации пользователя для поиска вакансий.
     * Если пользователь с таким userId не найден, будет создан новый.
     * Бросает IllegalArgumentException, если userId null.
     */
    public User updateNotificationTime(Long userId, LocalTime notificationTime) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        User user = getOrCreateUser(userId);
        user.updateNotificationTime(notificationTime);
        repository.saveUser(user);
        return user;
    }

    /**
     * Обновляет дату и время последнего запроса к серверу.
     * Если пользователь с таким id не найден, будет создан новый.
     * Бросает IllegalArgumentException, если userid или lastRequestTime null.
     */
    public User updateLastRequestTime(Long userId, Instant lastRequestTime) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        if (lastRequestTime == null) {
            throw new IllegalArgumentException("lastRequestTime не может быть null");
        }
        User user = getOrCreateUser(userId);
        user.updateLastRequestTime(lastRequestTime);
        repository.saveUser(user);
        return user;
    }

    /**
     * Обновляет состояние пользователя для поиска вакансий.
     * Если пользователь с таким userId не найден, будет создан новый.
     * Бросает IllegalArgumentException, если userId null.
     */
    public User updateSettingState(Long userId, UserSettingState newState) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        if (newState == null) {
            throw new IllegalArgumentException("newState не может быть null");
        }
        User user = getOrCreateUser(userId);
        user.updateSettingState(newState);
        repository.saveUser(user);
        return user;
    }

    /**
     * Устанавливает состояние сессии для пользователя и сохраняет его в репозитории.
     * Бросает IllegalArgumentException, если userId или sessionState null.
     */
    public void setStateSession(Long userId, VacancySessionState sessionState) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        if (sessionState == null) {
            throw new IllegalArgumentException("sessionState не может быть null");
        }
        sessionStates.put(userId, sessionState);
        sessionRepository.save(userId, sessionState);
    }

    /**
     * Удаляет пользователя и состояние сессии из репозитория.
     *
     * @param userId ID пользователя для удаления
     * @throws IllegalArgumentException если userId == null
     */
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        repository.deleteUser(userId);
        sessionRepository.delete(userId);
    }

    /**
     * Читает текущее состояние пользователя для поиска вакансий.
     * Если пользователь с таким userId не найден, будет создан новый.
     * Бросает IllegalArgumentException, если userId null.
     */
    public UserSettingState getSettingState(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        User user = getOrCreateUser(userId);
        return user.getSettingState();
    }

    /**
     * Читает и возвращает текущее состояние сессии для пользователя.
     * Бросает IllegalArgumentException, если userId null.
     */
    public VacancySessionState getStateSessionOrDefault(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        return sessionStates.getOrDefault(userId, VacancySessionState.INACTIVE);
    }

    /**
     * Восстанавливает состояние всех существующих в репозитории сессий
     */
    private void restoreSessions() {
        sessionStates.putAll(sessionRepository.findAll());
        log.info("Восстановлено {} сессий", sessionStates.size());
    }
}
