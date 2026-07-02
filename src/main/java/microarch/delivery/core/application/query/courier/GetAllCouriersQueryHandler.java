package microarch.delivery.core.application.query.courier;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.query.dto.CourierDto;
import microarch.delivery.core.ports.CourierRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Обработчик запроса на получение всех курьеров.
 */
@RequiredArgsConstructor
public class GetAllCouriersQueryHandler {

    private final CourierRepository courierRepository;

    public Result<List<CourierDto>, Error> handle(GetAllCouriersQuery query) {
        // Получаем всех курьеров из БД
        var couriers = courierRepository.getAll();

        // Преобразуем в DTO
        var result = couriers.stream()
                .map(CourierDto::from)
                .collect(Collectors.toList());

        return Result.success(result);
    }
}
