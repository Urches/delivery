package microarch.delivery.core.application.command.courier;

import microarch.delivery.core.application.Command;
import microarch.delivery.core.domain.model.Location;

import java.util.UUID;

/**
 * Команда на перемещение курьера.
 */
public record MoveCourierCommand(UUID courierId, Location location) implements Command {
}
