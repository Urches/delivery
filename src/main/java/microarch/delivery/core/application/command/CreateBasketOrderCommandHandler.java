package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.ports.GeoClientPort;
import microarch.delivery.core.ports.OrderRepository;

/**
 * Обработчик команды на создание заказа из события корзины.
 */
@Slf4j
@RequiredArgsConstructor
public class CreateBasketOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final GeoClientPort geoClientPort;

    public Result<Order, Error> handle(CreateBasketOrderCommand command) {
        log.info("Processing order creation for basket: {}", command.getBasketId());

        // Получаем Location из Geo сервиса по адресу улицы
        var locationResult = geoClientPort.getGeolocationByStreet(command.getStreet());
        if (locationResult.isFailure()) {
            return Result.failure(locationResult.getError());
        }

        var location = locationResult.getValue();

        // Создаем заказ с объемом из события
        var volumeResult = Volume.create(command.getVolume());
        if (volumeResult.isFailure()) {
            return Result.failure(volumeResult.getError());
        }

        var orderResult = Order.create(command.getBasketId(), location, volumeResult.getValue());
        if (orderResult.isFailure()) {
            return Result.failure(orderResult.getError());
        }

        // Сохраняем заказ в репозиторий
        orderRepository.save(orderResult.getValue());

        log.info("Order {} created successfully for basket {}", orderResult.getValue().getId(), command.getBasketId());

        return Result.success(orderResult.getValue());
    }
}
