package microarch.delivery.core.application.command.courier;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.CommandHandler;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

import java.util.Random;

/**
 * Обработчик команды на создание курьера.
 */
@RequiredArgsConstructor
public class CreateCourierCommandHandler implements CommandHandler<CreateCourierCommand, Void> {

    private final CourierRepository courierRepository;
    private final Random random;

    @Override
    public Result<Void, Error> handle(CreateCourierCommand command) {
        // Создаем рандомную Location для курьера
        var randomX = random.nextInt(10) + 1; // от 1 до 10
        var randomY = random.nextInt(10) + 1; // от 1 до 10
        var location = Location.mustCreate(randomX, randomY);

        // Создаем курьера
        var courier = Courier.mustCreate(command.courierId(), command.name(), location);

        // Сохраняем курьера в репозиторий
        courierRepository.save(courier);

        return Result.success();
    }
}
