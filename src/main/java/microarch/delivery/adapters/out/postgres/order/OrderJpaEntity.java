package microarch.delivery.adapters.out.postgres.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.order.Volume;

import java.util.UUID;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@Getter
public class OrderJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @Column(name = "volume", nullable = false)
    private int volume;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public OrderJpaEntity(UUID id, int locationX, int locationY, int volume, OrderStatus status) {
        this.id = id;
        this.locationX = locationX;
        this.locationY = locationY;
        this.volume = volume;
        this.status = status;
    }

    public static OrderJpaEntity fromDomain(Order order) {
        var location = order.getLocation();
        return new OrderJpaEntity(order.getId(), location.getX(), location.getY(), order.getVolume().getValue(),
                order.getStatus());
    }

    public Order toDomain() {
        return Order.of(id, Location.create(locationX, locationY).getValueOrThrow(),
                Volume.create(volume).getValueOrThrow(), status);
    }
}
