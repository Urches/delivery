package microarch.delivery.adapters.in.http.mappers;

import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.query.dto.CourierDto;
import microarch.delivery.core.application.query.dto.OrderDto;

import java.util.List;

/**
 * Маппер для преобразования между HTTP моделями и Application DTO.
 */
public class HttpMapper {

    private HttpMapper() {
        // Utility class
    }

    // ========== Order Mapping ==========

    /**
     * Преобразует OrderDto в HTTP Order.
     */
    public static Order toHttpOrder(OrderDto orderDto) {
        return new Order(orderDto.id(), toHttpLocation(orderDto.location()));
    }

    /**
     * Преобразует список OrderDto в список HTTP Order.
     */
    public static List<Order> toHttpOrders(List<OrderDto> orderDtos) {
        return orderDtos.stream().map(HttpMapper::toHttpOrder).toList();
    }

    // ========== Courier Mapping ==========

    /**
     * Преобразует CourierDto в HTTP Courier.
     */
    public static Courier toHttpCourier(CourierDto courierDto) {
        return new Courier(courierDto.id(), courierDto.name(), toHttpLocation(courierDto.location()));
    }

    /**
     * Преобразует список CourierDto в список HTTP Courier.
     */
    public static List<Courier> toHttpCouriers(List<CourierDto> courierDtos) {
        return courierDtos.stream().map(HttpMapper::toHttpCourier).toList();
    }

    // ========== Location Mapping ==========

    /**
     * Преобразует HTTP Location в доменную Location.
     */
    public static microarch.delivery.core.domain.model.Location toDomainLocation(Location httpLocation) {
        return microarch.delivery.core.domain.model.Location.create(httpLocation.getX(), httpLocation.getY())
                .getValue();
    }

    /**
     * Преобразует доменную Location в HTTP Location.
     */
    public static Location toHttpLocation(microarch.delivery.core.domain.model.Location domainLocation) {
        return new Location(domainLocation.getX(), domainLocation.getY());
    }
}
