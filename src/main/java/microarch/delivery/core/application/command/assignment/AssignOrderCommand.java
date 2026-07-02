package microarch.delivery.core.application.command.assignment;

import libs.errs.Error;
import libs.errs.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Команда на назначение заказа курьеру. Не содержит полей - система сама выбирает первый незавершенный заказ и
 * подходящего курьера.
 */
@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class AssignOrderCommand {

    public static Result<AssignOrderCommand, Error> create() {
        return Result.success(new AssignOrderCommand());
    }
}
