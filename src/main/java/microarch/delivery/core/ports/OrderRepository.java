package microarch.delivery.core.ports;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.order.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository для агрегата Order.
 * <p>
 * Содержит методы для сохранения и восстановления доменных объектов Order.
 */
public interface OrderRepository {

    /**
     * Сохраняет заказ в репозиторий
     */
    void save(Order order);

    /**
     * Обновляет существующий заказ в репозитории.
     *
     * @param order
     *            заказ для обновления
     */
    void update(Order order);

    /**
     * Получает заказ по идентификатору.
     *
     * @param id
     *            идентификатор заказа
     *
     * @return Optional<Order>
     */
    Optional<Order> getById(UUID id);

    /**
     * Получает один любой новый заказ (в статусе CREATED).
     *
     * @return Optional<Order>
     */
    Optional<Order> getOneNew();

    /**
     * Получает все назначенные заказы (в статусе ASSIGNED).
     *
     * @return список заказов в статусе ASSIGNED
     */
    List<Order> getAllAssigned();

    /**
     * Получает все незавершенные заказы (в статусах CREATED и ASSIGNED).
     *
     * @return список незавершенных заказов
     */
    List<Order> getAllNotCompleted();
}
