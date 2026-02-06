package vacancy_tracker.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserSettingsTest {


    @Test
    @DisplayName("Проверка корректно заполненного конструктора")
    void constructor_shouldCreateNewUserSettings_whenDataIsValid() {
        Instant instant = Instant.parse("2025-03-18T03:00:00Z");
        UserSettings userSettings = new UserSettings(
                65,
                3,
                80000,
                "Java developer",
                instant);

        assertThat(userSettings.getRegionCode())
                .isEqualTo(65);

        assertThat(userSettings.getExperienceFrom())
                .isEqualTo(3);

        assertThat(userSettings.getSalaryFrom())
                .isEqualTo(80000);

        assertThat(userSettings.getWordForSearch())
                .contains("Java developer");

        assertThat(userSettings.getNotificationTime())
                .isEqualTo(Instant.parse("2025-03-18T03:00:00Z"));
    }

    @Test
    @DisplayName("Проверка поведения при обновлении данных полей класса")
    void updateDataUserSettings_shouldChangeFields_whenDataIsChanged() {
        Instant instant = Instant.parse("2025-03-18T03:00:00Z");
        UserSettings userSettings = new UserSettings(
                65,
                3,
                80000,
                "Java developer",
                instant);

        userSettings.updateExperienceFrom(5);
        assertThat(userSettings.getExperienceFrom())
                .isEqualTo(5);

        userSettings.updateRegionCode(77);
        assertThat(userSettings.getRegionCode())
                .isEqualTo(77);

        userSettings.updateSalaryFrom(75000);
        assertThat(userSettings.getSalaryFrom())
                .isEqualTo(75000);

        userSettings.updateWordForSearch("Junior Java developer");
        assertThat(userSettings.getWordForSearch())
                .contains("Junior Java");

        userSettings.updateNotificationTime(Instant.parse("2025-03-18T07:00:00Z"));
        assertThat(userSettings.getNotificationTime())
                .isEqualTo(Instant.parse("2025-03-18T07:00:00Z"));
    }

    @Test
    @DisplayName("Проверка создания экземпляра, когда notificationTime не null")
    void constructor_shouldCreateNewUserSettings_whenNotificationTimeNotNull() {
        Instant instant = Instant.parse("2025-03-18T03:00:00Z");
        UserSettings userSettings = new UserSettings(
                65,
                3,
                80000,
                "Java developer",
                instant);

        assertThat(userSettings.isNotificationScheduleReady())
                .isTrue();
    }

    @Test
    @DisplayName("Конструктор допускает null/минимальные значения, кроме notificationTime")
    void constructor_shouldCreateUserSettings() {
        Instant instant = Instant.parse("2025-03-18T03:00:00Z");
        UserSettings userSettings = new UserSettings(
                65,
                null,
                0,
                "",
                instant);

        assertThat(userSettings).isNotNull();
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, если notificationTime null")
    void constructor_shouldThrowsIllegalArgumentException_whenNotificationTimeNull() {
        assertThatThrownBy(() ->
                new UserSettings(
                        65,
                        null,
                        0,
                        "",
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("NotificationTime не может быть null");
    }
}