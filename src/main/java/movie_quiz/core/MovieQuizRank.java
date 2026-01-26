package movie_quiz.core;

public enum MovieQuizRank {

    NEWBIE(0, 0, "Зритель по настроению 🙂"),
    POPCORN_LOVER(1, 5, "Любитель попкорна 🍿"),
    MOVIE_FAN(6, 10, "Киноман-любитель 🎬"),
    CRITIC(11, 15, "Домашний кинокритик 🎭"),
    MOVIE_GURU(16, 20, "Гуру кинозала 🧠🎥"),
    QUIZ_STAR(21, 30, "Звезда кино-квиза ⭐"),
    DIRECTOR(31, 40, "Режиссёр своего плейлиста 🎬🎧"),
    OSCAR_HUNTER(41, 49, "Охотник за Оскарами 🏆"),
    LEGEND(50, Integer.MAX_VALUE, "Легенда большого экрана 🎞️👑");

    private final int minInclusive;
    private final int maxInclusive;
    private final String title;

    MovieQuizRank(int minInclusive, int maxInclusive, String title) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
        this.title = title;
    }

    public String title() {
        return title;
    }

    public static String fromScore(int score) {
        for (MovieQuizRank rank : values()) {
            if (score >= rank.minInclusive && score <= rank.maxInclusive) {
                return rank.title();
            }
        }
        return NEWBIE.title();
    }
}
