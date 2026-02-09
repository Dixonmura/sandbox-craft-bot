package vacancy_tracker.core;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long userId);

    void saveUser(User user);

    void deleteUser(Long userId);
}
