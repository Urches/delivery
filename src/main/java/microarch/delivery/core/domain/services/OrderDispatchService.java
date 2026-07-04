package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;

import java.util.List;

/**
 * Интерфейс Domain Service для распределения заказов на курьеров.
 */
public interface OrderDispatchService {

    /**
     * Выполняет полный цикл диспетчеризации заказа.
     *
     * @param order
     *            заказ со статусом CREATED
     * @param couriers
     *            список курьеров для выбора
     *
     * @return Result с курьером-победителем при успехе или Error при неудаче
     */
    Result<Courier, Error> dispatch(Order order, List<Courier> couriers);
}
