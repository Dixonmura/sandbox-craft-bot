package vacancy_tracker.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    Long USER_ID = 85L;
    User user;

    @BeforeEach
    void setUp() {
        user = new User(USER_ID);
    }

    @Test
    @DisplayName("Проверка корректного создания экземпляра")
    void constructor_shouldCreateUser_whenDataIsValid() {
        assertThat(user)
                .isNotNull();
    }

    @Test
    @DisplayName("Проверка начальных значений UserSettings и обновления полей класса")
    void changeData_shouldCorrectlyChangedData() {
        assertThat(user.getUserId()).isEqualTo(USER_ID);
        assertThat(user.hasSettings()).isFalse();
        assertThat(user.hasUtcOffset()).isFalse();
        assertThat(user.isSettingsReady()).isFalse();
        assertThat(user.getSettingState()).isEqualTo(UserSettingState.NOT_INITIALIZED);

        user.updateSettings(new UserSettings(
                80,
                8,
                80000,
                "Java",
                LocalTime.parse("02:20")));
        user.updateUtcOffset(ZoneOffset.ofHours(3));
        user.updateRegionCode(65);
        user.updateExperienceFrom(8);
        user.updateSalaryFrom(80000);
        user.updateWordForSearch("C++");
        user.updateUtcOffset(ZoneOffset.of("+02:20"));
        user.updateNotificationTime(LocalTime.of(3, 15));
        user.updateSettingState(UserSettingState.READY);
        user.updateLastRequestTime(Instant.parse("2026-01-16T10:00:00Z"));

        assertThat(user.hasSettings()).isTrue();
        assertThat(user.isSettingsReady()).isTrue();
        assertThat(user.hasUtcOffset()).isTrue();
        assertThat(user.getUtcOffset()).isEqualTo(ZoneOffset.of("+02:20"));
        assertThat(user.getSettingState()).isEqualTo(UserSettingState.READY);
        assertThat(user.getSettings().getWordForSearch()).contains("C++");
        assertThat(user.getSettings().getNotificationTime()).isEqualTo(LocalTime.of(3, 15));
        assertThat(user.getSettings().getLastRequestTime()).isEqualTo(Instant.parse("2026-01-16T10:00:00Z"));
    }

    @Test
    @DisplayName("Проверка инициализации UserSetting и установленных в этом классе значений")
    void updateUserSettings_shouldCorrectlyInitializedAndApplyValues_whenValuesIsValid() {

        assertThat(user.hasSettings()).isFalse();

        user.updateSettings(new UserSettings(
                null, null, null, null, null));
        user.updateSettingState(UserSettingState.READY);

        assertThat(user.isSettingsReady()).isFalse();

        user.updateSalaryFrom(95000);
        assertThat(user.getSettings().getSalaryFrom()).isEqualTo(95000);
        assertThat(user.hasSettings()).isTrue();

        user.updateWordForSearch("Java");
        user.updateNotificationTime(LocalTime.of(3, 15));

        assertThat(user.isSettingsReady()).isTrue();
        assertThat(user.getSettingState()).isEqualTo(UserSettingState.READY);
        assertThat(user.getSettings().getWordForSearch()).contains("Java");
        assertThat(user.getSettings().getNotificationTime()).isEqualTo(LocalTime.of(3, 15));
    }

    @Test
    @DisplayName("При вызове updateSalaryFrom без явного updateSettings настройки инициализируются лениво")
    void updateSalaryFrom_shouldInitSettingsLazily() {
        assertThat(user.hasSettings()).isFalse();

        user.updateSalaryFrom(50000);

        assertThat(user.hasSettings()).isTrue();
        assertThat(user.getSettings().getSalaryFrom()).isEqualTo(50000);
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