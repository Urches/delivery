package microarch.delivery.core.application.query.order;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.query.dto.OrderDto;
import microarch.delivery.core.ports.OrderRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Обработчик запроса на получение всех незавершенных заказов.
 */
@RequiredArgsConstructor
public class GetNotCompletedOrdersQueryHandler {

    private final OrderRepository orderRepository;

    public Result<List<OrderDto>, Error> handle(GetNotCompletedOrdersQuery query) {
        // Получаем все незавершенные заказы из БД (в статусах CREATED и ASSIGNED)
        var orders = orderRepository.getAllNotCompleted();

        // Преобразуем в DTO
        var result = orders.stream().map(OrderDto::from).collect(Collectors.toList());

        return Result.success(result);
    }
}
