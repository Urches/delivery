package microarch.delivery.core.ports;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository для агрегата Courier.
 * <p>
 * Содержит методы для сохранения и восстановления доменных объектов Courier.
 */
public interface CourierRepository {

    /**
     * Добавляет нового курьера в репозиторий.
     *
     * @param courier
     *            курьер для добавления
     */
    void save(Courier courier);

    /**
     * Обновляет существующего курьера в репозитории.
     *
     * @param courier
     *            курьер для обновления
     */
    void update(Courier courier);

    /**
     * Получает курьера по идентификатору.
     *
     * @param id
     *            идентификатор курьера
     *
     * @return Optional<Courier>
     */
    Optional<Courier> getById(UUID id);

    /**
     * Получает всех курьеров.
     *
     * @return список курьеров
     */
    List<Courier> getAll();
}
