package microarch.delivery.core.application.command.order;

import microarch.delivery.core.application.Command;

import java.util.UUID;

/**
 * Команда на создание заказа.
 */
public record CreateOrderCommand(UUID orderId, String country, String city, String street, String house,
        String apartment, int volume) implements Command {
}
