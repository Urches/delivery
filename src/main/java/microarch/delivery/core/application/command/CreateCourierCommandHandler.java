package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * Обработчик команды на создание курьера.
 */
@RequiredArgsConstructor
public class CreateCourierCommandHandler {

    private final CourierRepository courierRepository;
    private final Random random;

    @Transactional
    public Result<Courier, Error> handle(CreateCourierCommand command) {
        // Создаем рандомную Location для курьера
        var randomX = random.nextInt(10) + 1; // от 1 до 10
        var randomY = random.nextInt(10) + 1; // от 1 до 10
        var location = Location.create(randomX, randomY);
        if (location.isFailure()) {
            return Result.failure(location.getError());
        }

        // Создаем курьера
        var courierResult = Courier.create(command.getCourierId(), command.getName(), location.getValue());
        if (courierResult.isFailure()) {
            return Result.failure(courierResult.getError());
        }

        // Сохраняем курьера в репозиторий
        var courier = courierResult.getValue();
        courierRepository.save(courier);

        return Result.success(courier);
    }
}
