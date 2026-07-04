package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Реализация Domain Service для распределения заказов на курьеров.
 * <p>
 * Алгоритм распределения: - За 1 раз диспетчеризуется только 1 заказ. - Курьер может брать сколько угодно заказов, но
 * при условии, что они суммарно не превышают MaxVolume. - Курьеры, которые переполнены, в диспетчеризации не участвуют.
 * - Считаем расстояние до заказа для каждого курьера, учитывая его текущее местоположение. - Побеждает курьер, который
 * ближе всего находится к заказу. - Если курьер переполнен - на него нельзя назначить заказ, возвращаем ошибку. - Если
 * все курьеры переполнены или отсутствуют, возвращаем бизнес-ошибку.
 */
public class OrderDispatchServiceImpl implements OrderDispatchService {

    @Override
    public Result<Courier, Error> dispatch(Order order, List<Courier> couriers) {
        var findCourierResult = findNearestAvailableCourier(order, couriers);
        if (findCourierResult.isFailure()) {
            return Result.failure(findCourierResult.getError());
        }

        var selectedCourier = findCourierResult.getValue();

        var assignResult = assignOrderToCourier(selectedCourier, order);
        if (assignResult.isFailure()) {
            return Result.failure(assignResult.getError());
        }

        return Result.success(selectedCourier);
    }

    private Result<Courier, Error> findNearestAvailableCourier(Order order, List<Courier> couriers) {
        Objects.requireNonNull(order, "Order must not be null");
        Objects.requireNonNull(couriers, "Couriers list must not be null");

        if (order.getStatus() != OrderStatus.CREATED) {
            return Result.failure(GeneralErrors.invalidOperation(
                    String.format("Can only dispatch orders with status CREATED, but got %s", order.getStatus())));
        }

        if (couriers.isEmpty()) {
            return Result.failure(GeneralErrors.invalidOperation("No couriers available for dispatch"));
        }

        var orderLocation = order.getLocation();

        return couriers.stream()
                .map(courier -> Objects.requireNonNull(courier, "Courier must not be null"))
                .filter(courier -> courier.canTakeOrder(order).getValue())
                .min(Comparator.comparingInt(c -> c.getLocation().distanceTo(orderLocation)))
                .map(Result::success)
                .orElse(Result.failure(GeneralErrors.invalidOperation("All couriers are unavailable or too busy")));
    }

    private Result<Void, Error> assignOrderToCourier(Courier courier, Order order) {
        Objects.requireNonNull(courier, "Courier must not be null");
        Objects.requireNonNull(order, "Order must not be null");

        var takeOrderResult = courier.takeOrder(order);
        if (takeOrderResult.isFailure()) {
            return Result.failure(takeOrderResult.getError());
        }

        var assignResult = order.assign();
        if (assignResult.isFailure()) {
            return Result.failure(assignResult.getError());
        }

        return Result.success();
    }
}
