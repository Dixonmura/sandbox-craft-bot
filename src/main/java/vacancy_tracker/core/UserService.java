package vacancy_tracker.core;

import java.time.ZoneOffset;

/**
 * Application-сервис для сценариев работы с пользователем:
 * создание/загрузка, изменение часового пояса и настроек поиска вакансий.
 */
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository не может быть null");
        }
        this.repository = repository;
    }

    /**
     * Возвращает существующего пользователя по userId
     * или создаёт нового, если он ещё не сохранён в репозитории.
     */
    public User getOrCreateUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        return repository.findById(userId).orElseGet(
                () -> {
                    User newUser = new User(userId);
                    repository.saveUser(newUser);
                    return newUser;
                }
        );
    }

    /**
     * Обновляет смещение часового пояса пользователя по строке формата,
     * поддерживаемом ZoneOffset (например, "+03:00" или "-05:30").
     * При некорректном формате бросает IllegalArgumentException.
     */
    public User updateUtcOffset(Long userId, String zoneUtcOffset) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        if (zoneUtcOffset == null || zoneUtcOffset.trim().isEmpty()) {
            throw new IllegalArgumentException("Неверный формат ввода зоны времени UTC. Ожидается: UTC+3, UTC+3:30, UTC-5");
        }
        User user = getOrCreateUser(userId);
        user.updateUtcOffset(parseUtcOffset(zoneUtcOffset));
        repository.saveUser(user);
        return user;
    }

    /**
     * Обновляет настройки (фильтры) пользователя для поиска вакансий.
     * Если пользователь с таким userId не найден, будет создан новый.
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
        repository.saveUser(user);

        return user;
    }

    private ZoneOffset parseUtcOffset(String zoneOffset) {
        try {
            return ZoneOffset.of(zoneOffset);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Неверный формат ввода зоны времени UTC");
        }
    }
}
