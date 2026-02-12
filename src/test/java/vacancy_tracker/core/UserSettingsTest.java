package vacancy_tracker.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserSettingsTest {

    @Test
    @DisplayName("Проверка корректно заполненного конструктора")
    void constructor_shouldCreateNewUserSettings_whenDataIsValid() {
        LocalTime time = LocalTime.parse("07:00");
        UserSettings userSettings = new UserSettings(
                65,
                3,
                80000,
                "Java developer",
                time);

        assertThat(userSettings.getRegionCode())
                .isEqualTo(65);

        assertThat(userSettings.getExperienceFrom())
                .isEqualTo(3);

        assertThat(userSettings.getSalaryFrom())
                .isEqualTo(80000);

        assertThat(userSettings.getWordForSearch())
                .contains("Java developer");

        assertThat(userSettings.getNotificationTime())
                .isEqualTo(LocalTime.parse("07:00"));
    }

    @Test
    @DisplayName("Проверка поведения при обновлении данных полей класса")
    void updateDataUserSettings_shouldChangeFields_whenDataIsChanged() {
        LocalTime time = LocalTime.parse("07:00");
        UserSettings userSettings = new UserSettings(
                65,
                3,
                80000,
                "Java developer",
                time);

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

        userSettings.updateNotificationTime(LocalTime.parse("07:00"));
        assertThat(userSettings.getNotificationTime())
                .isEqualTo(LocalTime.parse("07:00"));
    }

    @Test
    @DisplayName("Проверка создания экземпляра, когда notificationTime не null")
    void constructor_shouldCreateNewUserSettings_whenNotificationTimeNotNull() {
        LocalTime time = LocalTime.parse("07:00");
        UserSettings userSettings = new UserSettings(
                65,
                3,
                80000,
                "Java developer",
                time);

        assertThat(userSettings.isNotificationScheduleReady())
                .isTrue();
    }

    @Test
    @DisplayName("Конструктор допускает null/минимальные значения")
    void constructor_shouldCreateUserSettings() {
        UserSettings userSettings = new UserSettings(
                65,
                null,
                0,
                "",
                null);

        assertThat(userSettings).isNotNull();
        assertThat(userSettings.isNotificationScheduleReady()).
                isFalse();
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, если updateNotificationTime = null")
    void updateNotificationTime_shouldThrowsIllegalArgumentException_whenUpdateNotificationTimeNull() {
        UserSettings userSettings = new UserSettings(
                65,
                null,
                0,
                "",
                null);
        assertThatThrownBy(() ->
                userSettings.updateNotificationTime(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("NotificationTime не может быть null");
    }
}