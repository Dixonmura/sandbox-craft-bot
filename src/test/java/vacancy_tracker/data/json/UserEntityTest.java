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

class UserEntityTest {

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
    @DisplayName("Конвертация User → UserEntity → User сохраняет все данные")
    void fromDomainAndToDomain_shouldPreserveAllData() {
        User originalUser = createTestUser(123L);

        UserEntity entity = UserEntity.fromDomain(originalUser);
        User restoredUser = entity.toDomain();

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
    @DisplayName("Конвертация User без настроек")
    void fromDomain_shouldWork_whenUserHasNoSettings() {
        User user = new User(123L);
        user.updateUtcOffset(ZoneOffset.ofHours(3));
        user.updateSettingState(UserSettingState.NOT_INITIALIZED);

        UserEntity entity = UserEntity.fromDomain(user);
        User restoredUser = entity.toDomain();

        assertThat(restoredUser.getUserId()).isEqualTo(123L);
        assertThat(restoredUser.getUtcOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(restoredUser.getSettingState()).isEqualTo(UserSettingState.NOT_INITIALIZED);
        assertThat(restoredUser.getSettings()).isNotNull();
        assertThat(restoredUser.getSettings().getRegionCode()).isNull();
        assertThat(restoredUser.getSettings().getExperienceFrom()).isNull();
        assertThat(restoredUser.getSettings().getSalaryFrom()).isNull();
        assertThat(restoredUser.getSettings().getWordForSearch()).isNull();
        assertThat(restoredUser.getSettings().getNotificationTime()).isNull();
        assertThat(restoredUser.getSettings().getLastRequestTime()).isNull();
    }

    @Test
    @DisplayName("Конвертация User с частичными настройками")
    void fromDomain_shouldWork_whenSettingsHaveNulls() {
        User user = new User(123L);
        UserSettings settings = new UserSettings(
                null,
                null,
                100000,
                null,
                LocalTime.parse("09:00")
        );
        user.updateSettings(settings);

        UserEntity entity = UserEntity.fromDomain(user);
        User restoredUser = entity.toDomain();

        assertThat(restoredUser.getSettings().getRegionCode()).isNull();
        assertThat(restoredUser.getSettings().getExperienceFrom()).isNull();
        assertThat(restoredUser.getSettings().getSalaryFrom()).isEqualTo(100000);
        assertThat(restoredUser.getSettings().getWordForSearch()).isNull();
        assertThat(restoredUser.getSettings().getNotificationTime()).isEqualTo(LocalTime.parse("09:00"));
    }

    @Test
    @DisplayName("toDomain создаёт пользователя даже с null полями")
    void toDomain_shouldCreateUserWithNullFields() {
        UserEntity entity = new UserEntity();
        entity.setUserId(123L);

        User user = entity.toDomain();

        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isEqualTo(123L);
        assertThat(user.getUtcOffset()).isNull();
        assertThat(user.getSettingState()).isNull();
        assertThat(user.getSettings()).isNotNull();
        assertThat(user.getSettings().getRegionCode()).isNull();
    }

    @Test
    @DisplayName("Сохранение lastRequestTime работает корректно")
    void lastRequestTime_shouldBePreserved() {
        User user = new User(123L);
        UserSettings settings = new UserSettings(null, null, null, null, null);
        Instant testTime = Instant.parse("2024-01-01T10:00:00Z");
        settings.updateRequestTime(testTime);
        user.updateSettings(settings);

        UserEntity entity = UserEntity.fromDomain(user);
        User restoredUser = entity.toDomain();

        assertThat(restoredUser.getSettings().getLastRequestTime()).isEqualTo(testTime);
    }
}