package vacancy_tracker.core;

public interface NotificationService {

    void scheduleNotifications(User user);

    void cancelNotifications(Long userId);
}
