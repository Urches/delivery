package microarch.delivery.core.application.command.courier;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.Location;

import java.util.UUID;

/**
 * Команда на перемещение курьера.
 */
@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MoveCourierCommand {
    private final UUID courierId;
    private final Location location;

    public static Result<MoveCourierCommand, Error> create(UUID courierId, Location location) {
        var error = Guard.againstNullOrEmpty(courierId, "courierId");
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new MoveCourierCommand(courierId, location));
    }
}
