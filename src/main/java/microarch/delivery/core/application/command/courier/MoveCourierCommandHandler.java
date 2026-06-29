package microarch.delivery.core.application.command.courier;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.CommandHandler;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

/**
 * Обработчик команды на перемещение курьера.
 */
public class MoveCourierCommandHandler implements CommandHandler<MoveCourierCommand, Void> {

    private final CourierRepository courierRepository;

    public MoveCourierCommandHandler(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    public Result<Void, Error> handle(MoveCourierCommand command) {
        // Получаем курьера из БД
        var courier = courierRepository.getById(command.courierId())
                .orElseThrow(() -> new IllegalArgumentException("Courier not found: " + command.courierId()));

        // Перемещаем курьера
        courier.move(command.location());

        // Сохраняем изменения в БД
        courierRepository.update(courier);

        return Result.success();
    }
}
