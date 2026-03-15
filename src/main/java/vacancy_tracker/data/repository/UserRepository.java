package vacancy_tracker.data.repository;

import vacancy_tracker.core.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long userId);

    void saveUser(User user);

    void deleteUser(Long userId);

    default List<User> findAll() {
        return List.of();
    }
}
