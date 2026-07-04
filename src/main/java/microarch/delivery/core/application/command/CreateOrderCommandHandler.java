package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Обработчик команды на создание заказа.
 */
@RequiredArgsConstructor
public class CreateOrderCommandHandler {

    private final OrderRepository orderRepository;

    @Transactional
    public Result<Order, Error> handle(CreateOrderCommand command) {
        // Создаем заказ
        var orderResult = Order.create(command.getOrderId(), command.getLocation(), command.getVolume());
        if (orderResult.isFailure()) {
            return Result.failure(orderResult.getError());
        }

        // Сохраняем заказ в репозиторий
        var order = orderResult.getValue();
        orderRepository.save(order);
        return Result.success(order);
    }
}
