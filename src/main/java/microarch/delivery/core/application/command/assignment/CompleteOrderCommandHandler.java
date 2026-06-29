package microarch.delivery.core.application.command.assignment;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.CommandHandler;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;

/**
 * Обработчик команды на завершение заказа курьером.
 */
public class CompleteOrderCommandHandler implements CommandHandler<CompleteOrderCommand, Void> {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;

    public CompleteOrderCommandHandler(CourierRepository courierRepository, OrderRepository orderRepository) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Result<Void, Error> handle(CompleteOrderCommand command) {
        // Получаем курьера из БД
        var courier = courierRepository.getById(command.courierId())
                .orElseThrow(() -> new IllegalArgumentException("Courier not found: " + command.courierId()));

        // Получаем заказ из БД
        var order = orderRepository.getById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.orderId()));

        // Завершаем выполнение Assignment для курьера
        var completed = courier.completeAssignment();

        if (!completed) {
            return Result.failure(libs.errs.GeneralErrors
                    .invalidOperation("Courier cannot complete assignment: not close enough to delivery point"));
        }

        // Помечаем заказ как завершенный
        var completeResult = order.complete();
        if (completeResult.isFailure()) {
            return Result.failure(completeResult.getError());
        }

        // Сохраняем все изменения в БД
        courierRepository.update(courier);
        orderRepository.update(order);

        return Result.success();
    }
}
