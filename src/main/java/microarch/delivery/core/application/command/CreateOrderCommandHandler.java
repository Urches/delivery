package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClientPort;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Обработчик команды на создание заказа.
 */
@RequiredArgsConstructor
public class CreateOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final GeoClientPort geoClientPort;

    @Transactional
    public Result<Order, Error> handle(CreateOrderCommand command) {
        // Получаем Location из Geo сервиса по адресу улицы
        var locationResult = geoClientPort.getGeolocationByStreet(command.getStreet());
        if (locationResult.isFailure()) {
            return Result.failure(locationResult.getError());
        }

        var location = locationResult.getValue();

        // Создаем заказ с полученным Location
        var orderResult = Order.create(command.getOrderId(), location, command.getVolume());
        if (orderResult.isFailure()) {
            return Result.failure(orderResult.getError());
        }

        // Сохраняем заказ в репозиторий
        var order = orderResult.getValue();
        orderRepository.save(order);
        return Result.success(order);
    }
}
