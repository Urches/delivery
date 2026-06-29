package microarch.delivery.core.application;

import libs.errs.Error;
import libs.errs.Result;

/**
 * Интерфейс обработчика команд.
 *
 * @param <C>
 *            тип команды
 * @param <T>
 *            тип результата
 */
public interface CommandHandler<C extends Command, T> {
    Result<T, Error> handle(C command);
}
