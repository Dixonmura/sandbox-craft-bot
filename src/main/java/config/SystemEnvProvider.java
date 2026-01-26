package config;

/**
 * Реализация {@link EnvProvider}, использующая системные переменные окружения.
 */
public class SystemEnvProvider implements EnvProvider {

    /**
     * Возвращает значение системной переменной окружения по её имени.
     *
     * @param name имя переменной окружения
     * @return значение переменной или null, если не найдено
     */
    @Override
    public String getEnv(String name) {
        return System.getenv(name);
    }
}
