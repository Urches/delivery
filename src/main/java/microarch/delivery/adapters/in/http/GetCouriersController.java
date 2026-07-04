package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetCouriersApi;
import microarch.delivery.adapters.in.http.mappers.HttpMapper;
import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.core.application.query.GetAllCouriersQuery;
import microarch.delivery.core.application.query.GetAllCouriersQueryHandler;
import microarch.delivery.core.application.query.courier.GetAllCouriersQuery;
import microarch.delivery.core.application.query.courier.GetAllCouriersQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для получения всех курьеров.
 */
@RestController
@RequiredArgsConstructor
public class GetCouriersController implements GetCouriersApi {

    private final GetAllCouriersQueryHandler getAllCouriersQueryHandler;

    @Override
    public ResponseEntity<List<Courier>> getCouriers() {
        var couriers = GetAllCouriersQuery.create().flatMap(getAllCouriersQueryHandler::handle).getValueOrThrow();
        return ResponseEntity.ok(HttpMapper.toHttpCouriers(couriers));
    }
}
