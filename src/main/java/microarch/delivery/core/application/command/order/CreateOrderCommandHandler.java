package microarch.delivery.core.application.command.order;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.ports.OrderRepository;

import java.util.Random;

/**
 * Обработчик команды на создание заказа.
 */
@RequiredArgsConstructor
public class CreateOrderCommandHandler {

    private final OrderRepository orderRepository;

    public Result<Void, Error> handle(CreateOrderCommand command) {
        // Создаем заказ
        var order = Order.create(command.getOrderId(), command.getLocation(), command.getVolume());
        if (order.isFailure()) {
            return Result.failure(order.getError());
        }

        // Сохраняем заказ в репозиторий
        orderRepository.save(order.getValue());
        return Result.success();
    }
}
