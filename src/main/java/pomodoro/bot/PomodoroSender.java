package pomodoro.bot;

public interface PomodoroSender {
    void sendPomodoroReply(Long chatId, PomodoroReply reply);
    void sendFinalStatsQuestion(Long chatId, String text);
}
