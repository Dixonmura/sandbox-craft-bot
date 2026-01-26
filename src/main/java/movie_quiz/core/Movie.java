package movie_quiz.core;

/**
 * Модель фильма для киноквиза.
 * Хранит имя файла изображения и название фильма.
 */
public record Movie(
        String imageFileName,
        String title) {

    /**
     * Валидирует данные фильма при создании.
     *
     * @throws IllegalArgumentException если имя файла или название пустые
     */
    public Movie {
        if (imageFileName == null || imageFileName.isBlank()) {
            throw new IllegalArgumentException("Имя файла картинки не может быть пустым");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название фильма не может быть пустым");
        }
    }
}
