package microarch.delivery.core.application.query.courier;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.QueryHandler;
import microarch.delivery.core.application.query.dto.CourierDto;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Обработчик запроса на получение всех курьеров.
 */
@RequiredArgsConstructor
public class GetAllCouriersQueryHandler implements QueryHandler<GetAllCouriersQuery, List<CourierDto>> {

    private final CourierRepository courierRepository;

    @Override
    public Result<List<CourierDto>, Error> handle(GetAllCouriersQuery query) {
        // Получаем всех курьеров из БД
        var couriers = courierRepository.getAll();

        // Преобразуем в DTO
        var result = couriers.stream()
                .map(courier -> new CourierDto(courier.getId(), courier.getName(), courier.getLocation()))
                .collect(Collectors.toList());

        return Result.success(result);
    }
}
