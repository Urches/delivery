package microarch.delivery.core.application.command.courier;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

/**
 * Обработчик команды на перемещение курьера.
 */
public class MoveCourierCommandHandler {

    private final CourierRepository courierRepository;

    public MoveCourierCommandHandler(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    public Result<Void, Error> handle(MoveCourierCommand command) {
        // Получаем курьера из БД
        var courier = courierRepository.getById(command.getCourierId())
                .orElseThrow(() -> new IllegalArgumentException("Courier not found: " + command.getCourierId()));

        // Перемещаем курьера
        courier.move(command.getLocation());

        // Сохраняем изменения в БД
        courierRepository.update(courier);

        return Result.success();
    }
}
