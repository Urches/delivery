package microarch.delivery.core.application.command.assignment;

import microarch.delivery.core.application.Command;

import java.util.UUID;

/**
 * Команда на завершение заказа курьером.
 */
public record CompleteOrderCommand(UUID courierId, UUID orderId) implements Command {
}
