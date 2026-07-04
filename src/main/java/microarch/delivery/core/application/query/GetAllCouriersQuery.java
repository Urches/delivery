package microarch.delivery.core.application.query;

import libs.errs.Error;
import libs.errs.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Запрос на получение всех курьеров.
 */
@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class GetAllCouriersQuery {

    public static Result<GetAllCouriersQuery, Error> create() {
        return Result.success(new GetAllCouriersQuery());
    }
}
