package vacancy_tracker.data.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.core.User;
import vacancy_tracker.core.UserSettings;
import vacancy_tracker.core.UserSettingState;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEntityMapperTest {

    private final UserEntityMapper mapper = new UserEntityMapper();

    private User createTestUser(Long id) {
        User user = new User(id);
        user.updateUtcOffset(ZoneOffset.ofHours(3));
        user.updateSettingState(UserSettingState.CLEAN);

        UserSettings settings = new UserSettings(
                77,
                3,
                100000,
                "Java Developer",
                LocalTime.parse("09:00")
        );
        settings.updateRequestTime(Instant.parse("2024-01-01T10:00:00Z"));
        user.updateSettings(settings);

        return user;
    }

    @Test
    @DisplayName("Маппинг User → UserEntity → User сохраняет все данные")
    void roundTrip_shouldPreserveAllData() {
        User originalUser = createTestUser(123L);

        UserEntity entity = mapper.toEntity(originalUser);
        User restoredUser = mapper.toDomain(entity);

        assertThat(restoredUser.getUserId()).isEqualTo(123L);
        assertThat(restoredUser.getUtcOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(restoredUser.getSettingState()).isEqualTo(UserSettingState.CLEAN);
        assertThat(restoredUser.getSettings()).isNotNull();
        assertThat(restoredUser.getSettings().getRegionCode()).isEqualTo(77);
        assertThat(restoredUser.getSettings().getExperienceFrom()).isEqualTo(3);
        assertThat(restoredUser.getSettings().getSalaryFrom()).isEqualTo(100000);
        assertThat(restoredUser.getSettings().getWordForSearch()).isEqualTo("Java Developer");
        assertThat(restoredUser.getSettings().getNotificationTime()).isEqualTo(LocalTime.parse("09:00"));
        assertThat(restoredUser.getSettings().getLastRequestTime())
                .isEqualTo(Instant.parse("2024-01-01T10:00:00Z"));
    }

    @Test
    @DisplayName("Маппинг User без настроек")
    void shouldMapUserWithoutSettings() {
        User user = new User(123L);
        user.updateUtcOffset(ZoneOffset.ofHours(3));
        user.updateSettingState(UserSettingState.NOT_INITIALIZED);

        UserEntity entity = mapper.toEntity(user);
        User restoredUser = mapper.toDomain(entity);

        assertThat(restoredUser.getUserId()).isEqualTo(123L);
        assertThat(restoredUser.getUtcOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(restoredUser.getSettingState()).isEqualTo(UserSettingState.NOT_INITIALIZED);
        assertThat(restoredUser.getSettings()).isNotNull();
        assertThat(restoredUser.getSettings().getRegionCode()).isNull();
        assertThat(restoredUser.getSettings().getExperienceFrom()).isNull();
    }

    @Test
    @DisplayName("Маппинг с null значениями в настройках")
    void shouldMapSettingsWithNulls() {
        User user = new User(123L);
        UserSettings settings = new UserSettings(
                null,
                null,
                100000,
                null,
                LocalTime.parse("09:00")
        );
        user.updateSettings(settings);

        UserEntity entity = mapper.toEntity(user);
        User restoredUser = mapper.toDomain(entity);

        assertThat(restoredUser.getSettings().getRegionCode()).isNull();
        assertThat(restoredUser.getSettings().getExperienceFrom()).isNull();
        assertThat(restoredUser.getSettings().getSalaryFrom()).isEqualTo(100000);
        assertThat(restoredUser.getSettings().getWordForSearch()).isNull();
        assertThat(restoredUser.getSettings().getNotificationTime()).isEqualTo(LocalTime.parse("09:00"));
    }

    @Test
    @DisplayName("Бросает исключение при null User в toEntity")
    void toEntity_shouldThrow_whenUserIsNull() {
        assertThatThrownBy(() -> mapper.toEntity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User не может быть null");
    }

    @Test
    @DisplayName("Бросает исключение при null UserEntity в toDomain")
    void toDomain_shouldThrow_whenEntityIsNull() {
        assertThatThrownBy(() -> mapper.toDomain(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserEntity не может быть null");
    }
}