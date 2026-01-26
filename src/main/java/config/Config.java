package config;

/**
 * Класс отвечающий за сборку конфигурацию Telegram-бота.
 * Хранит токен бота и проверяет его корректность при создании.
 */
public record Config(String botToken) {

    /**
     * Проверяет токен бота при создании конфигурации.
     * @throws IllegalStateException если токен равен null или пустой.
     */
    public Config {
        if (botToken == null || botToken.isEmpty()) {
            throw new IllegalStateException("Токен не введен!");
        }
    }
}
