package microarch.delivery.core.application.query;

import libs.errs.Error;
import libs.errs.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Запрос на получение всех незавершенных заказов (в статусах Created или Assigned).
 */
@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class GetNotCompletedOrdersQuery {

    public static Result<GetNotCompletedOrdersQuery, Error> create() {
        return Result.success(new GetNotCompletedOrdersQuery());
    }
}
