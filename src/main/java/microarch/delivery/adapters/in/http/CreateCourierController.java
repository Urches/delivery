package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateCourierApi;
import microarch.delivery.adapters.in.http.model.CreateCourierResponse;
import microarch.delivery.adapters.in.http.model.NewCourier;
import microarch.delivery.core.application.command.courier.CreateCourierCommand;
import microarch.delivery.core.application.command.courier.CreateCourierCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для создания курьера.
 */
@RestController
@RequiredArgsConstructor
public class CreateCourierController implements CreateCourierApi {

    private final CreateCourierCommandHandler createCourierCommandHandler;

    @Override
    public ResponseEntity<CreateCourierResponse> createCourier(NewCourier newCourier) {
        var result = CreateCourierCommand.create(newCourier.getName()).flatMap(createCourierCommandHandler::handle);

        if (result.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Возвращаем успешный ответ
        var courier = result.getValue();
        var response = new CreateCourierResponse().courierId(courier.getId());
        return ResponseEntity.status(201).body(response);
    }
}
