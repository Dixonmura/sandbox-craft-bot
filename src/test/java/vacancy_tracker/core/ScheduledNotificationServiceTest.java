package vacancy_tracker.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduledNotificationServiceTest {

    Long USER_ID = 11L;
    User user;
    UserSettings settings;
    ScheduledNotificationService notificationService;

    @BeforeEach
    void setUp() {
        user = new User(USER_ID);
        user.updateUtcOffset(ZoneOffset.ofHours(5));
        settings = new UserSettings(
                65,
                3,
                90000,
                "Developer",
                LocalTime.of(11, 35)
        );
        user.updateSettings(settings);
        notificationService = new ScheduledNotificationService();
    }

    @Test
    @DisplayName("Проверка планировщика на создание задачи, добавления её в список задач и отмену задачи")
    void scheduleNotifications_shouldCreateTaskAndAddInTaskList_whenDataIsValid() {
        notificationService.scheduleNotifications(user);
        assertThat(notificationService.tasksByUser).isNotNull().hasSize(1);

        notificationService.cancelNotifications(USER_ID);
        assertThat(notificationService.tasksByUser).isEmpty();
    }

    @Test
    @DisplayName("Проверка правильного расчёта задержки до следующего уведомления")
    void calculateDelay_shouldCorrectlyCalculateDelay_whenDataIsValid() {
        long delay = notificationService.computeInitialDelayMillis(
                Instant.parse("2025-01-01T12:00:00Z"),
                ZoneOffset.of("+05:00"),
                LocalTime.of(18, 0));
        assertThat(delay).isEqualTo(Duration.ofHours(1).toMillis());

        long delayToTomorrow = notificationService.computeInitialDelayMillis(
                Instant.parse("2025-01-01T12:00:00Z"),
                ZoneOffset.of("+05:00"),
                LocalTime.of(12, 0));
        assertThat(delayToTomorrow).isEqualTo(Duration.ofHours(19).toMillis());
    }

    @Test
    @DisplayName("Проверка выброса исключений, когда userUtcOffset или notifyTine null")
    void scheduleNotifications_shouldThrowsIllegalStateException_whenOffsetOrNotifyTimeIsNull() {
        User userWithInvalidData = new User(17L);
        assertThatThrownBy(() ->
                notificationService.scheduleNotifications(userWithInvalidData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя запланировать уведомления без utcOffset или notificationTime");

        userWithInvalidData.updateUtcOffset(ZoneOffset.ofHoursMinutes(8, 30));
        assertThatThrownBy(() ->
                notificationService.scheduleNotifications(userWithInvalidData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя запланировать уведомления без utcOffset или notificationTime");

        userWithInvalidData.updateUtcOffset(null);
        userWithInvalidData.updateNotificationTime(LocalTime.of(5, 50));
        assertThatThrownBy(() ->
                notificationService.scheduleNotifications(userWithInvalidData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя запланировать уведомления без utcOffset или notificationTime");
    }

    @Test
    @DisplayName("Проверка выброса исключения, когда user null")
    void scheduleNotifications_shouldThrowsIllegalArgumentException_whenUserIsNull() {
        assertThatThrownBy(() ->
                notificationService.scheduleNotifications(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("user не может быть null");
    }
}