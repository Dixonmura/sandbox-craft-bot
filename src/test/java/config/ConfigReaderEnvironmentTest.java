package config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigReaderEnvironmentTest {

    @Test
    @DisplayName("Возвращает Config с токеном из EnvProvider")
    void shouldReturnConfigWithTokenFromEnv() {
        EnvProvider envProvider = mock(EnvProvider.class);
        when(envProvider.getEnv("BOT_TOKEN")).thenReturn("env-token");

        ConfigReaderEnvironment reader = new ConfigReaderEnvironment(envProvider);

        Config config = reader.reader();

        assertEquals("env-token", config.botToken());
    }

    @Test
    @DisplayName("Кидает IllegalStateException, если BOT_TOKEN null")
    void shouldThrowWhenEnvTokenIsNull() {
        EnvProvider envProvider = mock(EnvProvider.class);
        when(envProvider.getEnv("BOT_TOKEN")).thenReturn(null);

        ConfigReaderEnvironment reader = new ConfigReaderEnvironment(envProvider);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                reader::reader
        );

        assertEquals("Токен не введен!", ex.getMessage());
    }
}
