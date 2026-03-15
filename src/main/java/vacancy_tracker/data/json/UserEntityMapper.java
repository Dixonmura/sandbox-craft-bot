package vacancy_tracker.data.json;

import vacancy_tracker.core.User;

/**
 * Конвертер между доменной моделью {@link User} и сущностью для хранения {@link UserEntity}.
 *
 * <p>Выступает тонкой прослойкой, делегирующей преобразование статическим методам
 * {@link UserEntity#fromDomain(User)} и {@link UserEntity#toDomain()}.
 * Введён для соблюдения принципа единственной ответственности и упрощения тестирования.
 */
public class UserEntityMapper {

    /**
     * Преобразует доменного пользователя в сущность для сохранения.
     *
     * @param user доменная модель пользователя
     * @return сущность для JSON-сериализации
     * @throws IllegalArgumentException если user равен null
     */
    public UserEntity toEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null");
        }
        return UserEntity.fromDomain(user);
    }

    /**
     * Восстанавливает доменного пользователя из сохранённой сущности.
     *
     * @param entity сущность из JSON-файла
     * @return восстановленная доменная модель пользователя
     * @throws IllegalArgumentException если entity равен null
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("UserEntity не может быть null");
        }
        return entity.toDomain();
    }
}
