package microarch.delivery.core.application.command.assignment;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.service.OrderDispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;

import java.util.List;

/**
 * Обработчик команды на назначение заказа курьеру.
 */
@RequiredArgsConstructor
public class AssignOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderDispatchService dispatchService;
    private final DomainEventPublisher domainEventPublisher;

    public Result<Void, Error> handle(AssignOrderCommand command) {
        // Получаем 1 любой не назначенный заказ из БД (со статусом CREATED)
        var orderOpt = orderRepository.getOneNew();
        if (orderOpt.isEmpty()) {
            return Result.failure(GeneralErrors.invalidOperation("No new orders available for assignment"));
        }
        var order = orderOpt.get();

        // Получаем всех курьеров
        var couriers = courierRepository.getAll();

        // Производим диспетчеризацию заказа на курьеров
        var dispatchResult = dispatchService.dispatch(order, couriers);
        if (dispatchResult.isFailure()) {
            return Result.failure(dispatchResult.getError());
        }

        // Сохраняем изменения в БД (курьер и заказ уже обновлены в памяти)
        var courier = dispatchResult.getValue();
        courierRepository.update(courier);
        orderRepository.update(order);

        domainEventPublisher.publish(List.of(order));
        return Result.success();
    }
}
