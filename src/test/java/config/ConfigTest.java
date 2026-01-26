package config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    @DisplayName("Config создаётся, когда токен не пустой")
    void shouldCreateConfigWhenTokenNotEmpty() {
        Config config = new Config("some-token");

        assertEquals("some-token", config.botToken());
    }

    @Test
    @DisplayName("Config кидает IllegalStateException, когда токен null")
    void shouldThrowExceptionWhenTokenIsNull() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new Config(null)
        );

        assertEquals("Токен не введен!", ex.getMessage());
    }

    @Test
    @DisplayName("Config кидает IllegalStateException, когда токен пустой")
    void shouldThrowExceptionWhenTokenIsEmpty() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new Config("")
        );

        assertEquals("Токен не введен!", ex.getMessage());
    }
}
