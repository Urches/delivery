package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Команда на создание курьера.
 */
@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CreateCourierCommand {
    private final UUID courierId;
    private final String name;

    public static Result<CreateCourierCommand, Error> create(UUID courierId, String name) {
        var error = Guard.combine(
                Guard.againstNullOrEmpty(courierId, "courierId"),
                Guard.againstNullOrEmpty(name, "name")
        );
        if (error != null) {
            return Result.failure(error);
        }
        return Result.success(new CreateCourierCommand(courierId, name));
    }
}
