package microarch.delivery.core.application;

import libs.errs.Error;
import libs.errs.Result;

/**
 * Интерфейс обработчика запросов.
 *
 * @param <Q>
 *            тип запроса
 * @param <T>
 *            тип результата
 */
public interface QueryHandler<Q extends Query, T> {
    Result<T, Error> handle(Q query);
}
