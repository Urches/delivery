package microarch.delivery.core.application.command.assignment;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.CommandHandler;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.service.OrderDispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;

/**
 * Обработчик команды на назначение заказа курьеру.
 */
public class AssignOrderCommandHandler implements CommandHandler<AssignOrderCommand, Void> {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderDispatchService dispatchService;

    public AssignOrderCommandHandler(OrderRepository orderRepository, CourierRepository courierRepository,
            OrderDispatchService dispatchService) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.dispatchService = dispatchService;
    }

    @Override
    public Result<Void, Error> handle(AssignOrderCommand command) {
        // Получаем 1 любой не назначенный заказ из БД (со статусом CREATED)
        var order = orderRepository.getOneNew()
                .orElseThrow(() -> new IllegalArgumentException("No new orders available for assignment"));

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
