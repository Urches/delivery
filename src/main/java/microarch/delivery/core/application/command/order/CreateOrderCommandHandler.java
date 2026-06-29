package microarch.delivery.core.application.command.order;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.CommandHandler;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.ports.OrderRepository;

import java.util.Random;

/**
 * Обработчик команды на создание заказа.
 */
@RequiredArgsConstructor
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, Void> {

    private final OrderRepository orderRepository;
    private final Random random;

    @Override
    public Result<Void, Error> handle(CreateOrderCommand command) {
        // Создаем рандомную Location для заказа (в будущем будем использовать Geo Service)
        var randomX = random.nextInt(10) + 1; // от 1 до 10
        var randomY = random.nextInt(10) + 1; // от 1 до 10

        var location = Location.create(randomX, randomY);
        if (location.isFailure()) {
            return Result.failure(location.getError());
        }

        // Создаем объем заказа
        var volume = Volume.create(command.volume());
        if (volume.isFailure()) {
            return Result.failure(volume.getError());
        }

        // Создаем заказ
        var order = Order.create(command.orderId(), location.getValue(), volume.getValue());
        if (order.isFailure()) {
            return Result.failure(order.getError());
        }

        // Сохраняем заказ в репозиторий
        orderRepository.save(order.getValue());

        return Result.success();
    }
}
