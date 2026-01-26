package config;

/**
 * Реализация {@link ConfigReader}, которая читает настройки бота
 * из переменных окружения.
 */
public class ConfigReaderEnvironment implements ConfigReader {

    private final EnvProvider envProvider;

    /**
     * Создаёт ридер конфигурации на основе переданного провайдера окружения.
     *
     * @param provider источник переменных окружения
     */
    public ConfigReaderEnvironment(EnvProvider provider) {
        this.envProvider = provider;
    }

    /**
     * Читает токен бота из переменной окружения BOT_TOKEN
     * и возвращает сконструированный {@link Config}.
     *
     * @return конфигурация бота
     */
    @Override
    public Config reader() {
        String token = envProvider.getEnv("BOT_TOKEN");
        return new Config(token);
    }
}
