package microarch.delivery.core.application.command.assignment;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.services.OrderDispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;

/**
 * Обработчик команды на назначение заказа курьеру.
 */
@RequiredArgsConstructor
public class AssignOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderDispatchService dispatchService;

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
        courierRepository.update(dispatchResult.getValue());
        orderRepository.update(order);

        return Result.success();
    }
}
