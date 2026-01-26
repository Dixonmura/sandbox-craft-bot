package pomodoro.core;

public enum PomodoroRank {

    NONE(0, 0, "Новичок в помидорах!"),
    NOVICE(3, 5, "Новичок!"),
    HARD_WORKER(6, 8, "Настоящий трудяга!"),
    IRON_WOODCUTTER(9, Integer.MAX_VALUE, "Железный дровосек!");

    private final int minInclusive;
    private final int maxInclusive;
    private final String title;

    PomodoroRank(int minInclusive, int maxInclusive, String title) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
        this.title = title;
    }

    public String title() {
        return title;
    }

    public static PomodoroRank fromCycles(int cycles) {
        for (PomodoroRank rank : values()) {
            if (cycles >= rank.minInclusive && cycles <= rank.maxInclusive) {
                return rank;
            }
        }
        return NONE;
    }
}