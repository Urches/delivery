package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetOrdersApi;
import microarch.delivery.adapters.in.http.mappers.HttpMapper;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.query.GetNotCompletedOrdersQuery;
import microarch.delivery.core.application.query.GetNotCompletedOrdersQueryHandler;
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
        var orders = GetNotCompletedOrdersQuery.create().flatMap(getNotCompletedOrdersQueryHandler::handle)
                .getValueOrThrow();
        return ResponseEntity.ok(HttpMapper.toHttpOrders(orders));
    }
}
