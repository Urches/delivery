package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.MoveCourierApi;
import microarch.delivery.adapters.in.http.mappers.HttpMapper;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.command.MoveCourierCommand;
import microarch.delivery.core.application.command.MoveCourierCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Контроллер для перемещения курьера.
 */
@RestController
@RequiredArgsConstructor
public class MoveCourierController implements MoveCourierApi {

    private final MoveCourierCommandHandler moveCourierCommandHandler;

    @Override
    public ResponseEntity<Void> moveCourier(UUID courierId, Location location) {
        var locationResult = HttpMapper.toDomainLocation(location);
        if (locationResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        var result = MoveCourierCommand.create(courierId, locationResult.getValue())
                .flatMap(moveCourierCommandHandler::handle);

        if (result.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }
}
