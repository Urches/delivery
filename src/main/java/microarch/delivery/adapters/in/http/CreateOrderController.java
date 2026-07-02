package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateOrderApi;
import microarch.delivery.adapters.in.http.mappers.HttpMapper;
import microarch.delivery.adapters.in.http.model.CreateOrderResponse;
import microarch.delivery.adapters.in.http.model.NewOrder;
import microarch.delivery.core.application.command.order.CreateOrderCommand;
import microarch.delivery.core.application.CommandHandler;
import microarch.delivery.core.application.command.order.CreateOrderCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для создания заказа.
 */
@RestController
@RequiredArgsConstructor
public class CreateOrderController implements CreateOrderApi {

    private final CreateOrderCommandHandler createOrderCommandHandler;

    @Override
    public ResponseEntity<CreateOrderResponse> createOrder(NewOrder newOrder) {
        // Преобразуем HTTP модель в команду
        var command = CreateOrderCommand.create(newOrder);

        // Выполняем команду
        var result = createOrderCommandHandler.handle(command);

        if (result.isFailure()) {
            // Для ошибок возвращаем null с соответствующим статусом
            // Error будет возвращен через GlobalExceptionHandler
            throw new RuntimeException(result.getError().getMessage());
        }

        // Возвращаем успешный ответ
        var response = new CreateOrderResponse().orderId(command.orderId());
        return ResponseEntity.status(201).body(response);
    }
}
