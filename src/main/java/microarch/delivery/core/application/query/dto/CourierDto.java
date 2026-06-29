package microarch.delivery.core.application.query.dto;

import microarch.delivery.core.domain.model.Location;

import java.util.UUID;

/**
 * DTO для представления информации о курьере.
 */
public record CourierDto(UUID id, String name, Location location) {
}
