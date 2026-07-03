package microarch.delivery.adapters.out.postgres.order;

import jakarta.persistence.*;
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
        var volume = order.getVolume();
        return new OrderJpaEntity(
                order.getId(),
                location.getX(),
                location.getY(),
                volume.getValue(),
                order.getStatus());
    }

    public Order toDomain() {
        var location = Location.mustCreate(locationX, locationY);
        var volume = Volume.mustCreate(this.volume);
        return Order.of(id, location, volume, status);
    }
}
