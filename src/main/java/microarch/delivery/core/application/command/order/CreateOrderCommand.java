package microarch.delivery.core.application.command.order;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Volume;

import java.util.UUID;

/**
 * Команда на создание заказа.
 */
@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CreateOrderCommand {
    private final UUID orderId;
    private final String country;
    private final String city;
    private final String street;
    private final String house;
    private final String apartment;
    private final Volume volume;
    private final Location location;

    private static final Location DEFAULT_LOCATION = Location.mustCreate(1, 1);

    public static Result<CreateOrderCommand, Error> create(UUID orderId, String country, String city, String street, String house, String apartment, int volume) {
        var volumeResult = Volume.create(volume);
        var error = Guard.combine(Guard.againstNullOrEmpty(orderId, "orderId"),
                Guard.againstNullOrEmpty(country, "country"), Guard.againstNullOrEmpty(city, "city"),
                Guard.againstNullOrEmpty(street, "street"), Guard.againstNullOrEmpty(house, "house"),
                Guard.againstNullOrEmpty(apartment, "apartment"),
                volumeResult.isFailure() ? volumeResult.getError() : null);

        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new CreateOrderCommand(orderId, country, city, street, house, apartment,
                volumeResult.getValue(), DEFAULT_LOCATION));
    }
}
