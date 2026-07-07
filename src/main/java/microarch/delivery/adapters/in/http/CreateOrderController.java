package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateOrderApi;
import microarch.delivery.adapters.in.http.model.CreateOrderResponse;
import microarch.delivery.adapters.in.http.model.NewOrder;
import microarch.delivery.core.application.command.CreateOrderCommand;
import microarch.delivery.core.application.command.CreateOrderCommandHandler;
import org.springframework.http.HttpStatus;
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
        var address = newOrder.getAddress();
        var commandResult = CreateOrderCommand.create(newOrder.getId(), address.getCountry(), address.getCity(),
                address.getStreet(), address.getHouse(), address.getApartment(), newOrder.getVolume());

        if (commandResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        // Выполняем команду
        var command = commandResult.getValue();
        var result = createOrderCommandHandler.handle(command);

        if (result.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Возвращаем успешный ответ
        var order = result.getValue();
        var response = new CreateOrderResponse().orderId(order.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
