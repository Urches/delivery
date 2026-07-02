package microarch.delivery.core.application.query.dto;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;

import java.util.UUID;

/**
 * DTO для представления информации о заказе.
 */
public record OrderDto(UUID id, Location location) {

    /**
     * Создаёт DTO из доменной модели Order.
     *
     * @param order
     *            доменный объект заказа
     *
     * @return OrderDto
     */
    public static OrderDto from(Order order) {
        return new OrderDto(order.getId(), order.getLocation());
    }
}
