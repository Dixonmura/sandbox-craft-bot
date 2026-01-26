package pomodoro.core;

/**
 * Конструктор класса мотивационного фото
 *
 * @param pathToPhoto     путь к фотографии
 * @param motivationTitle название мотивационной фотографии
 */
public record MotivationPhoto(String pathToPhoto, String motivationTitle) {

    /**
     * Валидирует данные при создании фото
     *
     * @throws IllegalArgumentException если путь или название null
     */
    public MotivationPhoto {
        if (pathToPhoto == null || pathToPhoto.isBlank()) {
            throw new IllegalArgumentException("Путь к фотографии не может быть пустым");
        }
        if (motivationTitle == null || motivationTitle.isBlank()) {
            throw new IllegalArgumentException("Название мотивационной фотографии не может быть пустым");
        }
    }
}
