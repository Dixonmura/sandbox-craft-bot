package pomodoro.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MotivationPhotoTest {

    @Test
    @DisplayName("Проверка работы конструктора при подаче валидных данных")
    void constructor_shouldCreateMotivationPhoto_whenDataIsValid() {
        MotivationPhoto photo = new MotivationPhoto(
                "res/assets/motivationPhotos",
                "whenWorking");

        assertThat(photo)
                .isNotNull()
                .extracting(MotivationPhoto::motivationTitle, MotivationPhoto::pathToPhoto)
                .containsExactly("whenWorking", "res/assets/motivationPhotos");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, если какой-либо из аргументов null или пустой")
    void constructor_shouldThrowsIllegalArgumentExeption_whenDataInvalid() {

        assertThatThrownBy(() ->
                new MotivationPhoto("res/assets/motivationPhotos", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Название мотивационной фотографии не может быть пустым");

        assertThatThrownBy(() ->
                new MotivationPhoto("", "whenWorking"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Путь к фотографии не может быть пустым");
    }
}