package microarch.delivery.core.application.query.dto;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;

import java.util.UUID;

/**
 * DTO для представления информации о курьере.
 */
public record CourierDto(UUID id, String name, Location location) {

    /**
     * Создаёт DTO из доменной модели Courier.
     *
     * @param courier
     *            доменный объект курьера
     *
     * @return CourierDto
     */
    public static CourierDto from(Courier courier) {
        return new CourierDto(courier.getId(), courier.getName(), courier.getLocation());
    }
}
