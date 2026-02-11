package vacancy_tracker.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    @DisplayName("Проверка корректного создания экземпляра")
    void constructor_shouldCreateUser_whenDataIsValid() {
        User user = new User(11L);
        assertThat(user)
                .isNotNull();
    }

    @Test
    @DisplayName("Проверка начальных значений и обновления полей класса")
    void changeData_shouldCorrectlyChangedData() {
        User user = new User(11L);

        assertThat(user.getUserId()).isEqualTo(11L);
        assertThat(user.hasSettings()).isFalse();
        assertThat(user.hasUtcOffset()).isFalse();
        assertThat(user.isSettingsReady()).isFalse();
        assertThat(user.getSettingState()).isEqualTo(UserSettingState.NOT_INITIALIZED);

        user.updateUtcOffset(ZoneOffset.ofHours(3));
        user.updateSettings(new UserSettings(
                65,
                8,
                80000,
                "Java",
                Instant.parse("2025-03-18T03:00:00Z")));
        user.updateSettingState(UserSettingState.READY);

        assertThat(user.hasSettings()).isTrue();
        assertThat(user.isSettingsReady()).isTrue();
        assertThat(user.hasUtcOffset()).isTrue();
        assertThat(user.getUtcOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(user.getSettingState()).isEqualTo(UserSettingState.READY);
        assertThat(user.isSettingsReady()).isTrue();
    }

    @Test
    @DisplayName("Проверка выбрасывания, когда userId null")
    void constructor_shouldThrowsException_whenUserIdNull() {
        assertThatThrownBy(() ->
                new User(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }
}