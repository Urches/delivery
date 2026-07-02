package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetOrdersApi;
import microarch.delivery.adapters.in.http.mappers.HttpMapper;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.query.order.GetNotCompletedOrdersQuery;
import microarch.delivery.core.application.QueryHandler;
import microarch.delivery.core.application.query.dto.OrderDto;
import microarch.delivery.core.application.query.order.GetNotCompletedOrdersQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для получения всех незавершенных заказов.
 */
@RestController
@RequiredArgsConstructor
public class GetOrdersController implements GetOrdersApi {

    private final GetNotCompletedOrdersQueryHandler getNotCompletedOrdersQueryHandler;

    @Override
    public ResponseEntity<List<Order>> getOrders() {
        var result = GetNotCompletedOrdersQuery.create()
                .flatMap(getNotCompletedOrdersQueryHandler::handle);

        if (result.isFailure()) {
            return ResponseEntity.status(500).body(null);
        }

        // Преобразуем в HTTP модель
        var orders = HttpMapper.toHttpOrders(result.getValue());
        return ResponseEntity.ok(orders);
    }
}
