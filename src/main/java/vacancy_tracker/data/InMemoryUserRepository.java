package vacancy_tracker.data;

import vacancy_tracker.core.User;
import vacancy_tracker.core.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(storage.get(userId));
    }

    @Override
    public void saveUser(User user) {
        if (user != null) {
            storage.put(user.getUserId(), user);
        } else {
            throw new IllegalArgumentException("Для сохранения, User не должен быть null");
        }
    }

    @Override
    public void deleteUser(Long userId) {
        User removed = storage.remove(userId);
        if (removed == null) {
            throw new IllegalStateException("Невозможно удалить пользователя, которого не существует");
        }
    }
}
