package microarch.delivery.core.application.query.dto;

import microarch.delivery.core.domain.model.Location;

import java.util.UUID;

/**
 * DTO для представления информации о заказе.
 */
public record OrderDto(UUID id, Location location) {
}
