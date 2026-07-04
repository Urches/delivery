package microarch.delivery.core.application.command.assignment;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;

import java.util.List;

/**
 * Обработчик команды на завершение заказа курьером.
 */
@RequiredArgsConstructor
public class CompleteOrderCommandHandler {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;

    public Result<Void, Error> handle(CompleteOrderCommand command) {
        // Получаем курьера из БД
        var courier = courierRepository.getById(command.getCourierId())
                .orElseThrow(() -> new IllegalArgumentException("Courier not found: " + command.getCourierId()));

        // Получаем заказ из БД
        var order = orderRepository.getById(command.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + command.getOrderId()));

        // Завершаем выполнение Assignment для курьера
        var completed = courier.completeAssignment();

        if (!completed) {
            return Result.failure(GeneralErrors
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

        domainEventPublisher.publish(List.of(order));
        return Result.success();
    }
}
