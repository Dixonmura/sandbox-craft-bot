package vacancy_tracker.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.core.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repo;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(65L);
        repo = new InMemoryUserRepository();
    }

    @Test
    @DisplayName("Проверка корректного добавления User в репозиторий и его поиска")
    void saveAndFindUserById_shouldSaveAndFindUser_whenDataIsValid() {
        repo.saveUser(user);

        assertThat(repo.findById(65L))
                .isPresent()
                .contains(user);
    }

    @Test
    @DisplayName("Проверка корректного удаления User из репозитория")
    void deleteExistingUserRemovesIt_shouldDeleteUser_whenDataIsValid() {
        repo.saveUser(user);

        repo.deleteUser(65L);

        assertThat(repo.findById(65L)).isEmpty();
    }

    @Test
    @DisplayName("Проверка поиска User которого нет в списке репозитория")
    void findUserById_shouldReturnOptionalEmpty_whenUserNotExist() {
        Optional<User> notExistUser = repo.findById(79L);

        assertThat(notExistUser).isEmpty();
    }

    @Test
    @DisplayName("Проверка выброса исключения при попытке удалить несуществующего пользователя")
    void deleteUser_shouldThrowsIllegalStateException_whenUserDoesNotExist() {
        assertThatThrownBy(() -> repo.deleteUser(42L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Невозможно удалить пользователя, которого не существует");
    }

    @Test
    @DisplayName("Проверка выброса исключения при попытке сохранить nullUser")
    void saveNullUser_shouldThrowsIllegalArgumentException_whenUserIsNull() {
        assertThatThrownBy(() -> repo.saveUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Для сохранения, User не должен быть null");
    }
}
