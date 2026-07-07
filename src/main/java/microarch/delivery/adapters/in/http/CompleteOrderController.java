package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CompleteOrderApi;
import microarch.delivery.core.application.command.CompleteOrderCommand;
import microarch.delivery.core.application.command.CompleteOrderCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Контроллер для завершения заказа.
 */
@RestController
@RequiredArgsConstructor
public class CompleteOrderController implements CompleteOrderApi {

    private final CompleteOrderCommandHandler completeOrderCommandHandler;

    @Override
    public ResponseEntity<Void> completeOrder(UUID courierId, UUID orderId) {
        var result = CompleteOrderCommand.create(courierId, orderId).flatMap(completeOrderCommandHandler::handle);

        if (result.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }
}
