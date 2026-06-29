package microarch.delivery.core.application.command.courier;

import microarch.delivery.core.application.Command;

import java.util.UUID;

/**
 * Команда на создание курьера.
 */
public record CreateCourierCommand(UUID courierId, String name) implements Command {
}
