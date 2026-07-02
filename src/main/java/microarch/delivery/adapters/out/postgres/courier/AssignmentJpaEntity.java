package microarch.delivery.adapters.out.postgres.courier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.assignment.AssignmentStatus;
import microarch.delivery.core.domain.model.order.Volume;

import java.util.UUID;

@Entity
@Table(name = "assignments")
@NoArgsConstructor
@Getter
public class AssignmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "volume", nullable = false)
    private int volume;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    public AssignmentJpaEntity(UUID orderId, int volume, int locationX, int locationY, AssignmentStatus status) {
        this.orderId = orderId;
        this.volume = volume;
        this.locationX = locationX;
        this.locationY = locationY;
        this.status = status;
    }

    public static AssignmentJpaEntity fromDomain(Assignment assignment) {
        var location = assignment.getLocation();
        var volume = assignment.getVolume();
        return new AssignmentJpaEntity(assignment.getOrderId(), volume.getValue(), location.getX(), location.getY(),
                assignment.getStatus());
    }

    public Assignment toDomain() {
        var location = Location.mustCreate(locationX, locationY);
        var domainVolume = Volume.mustCreate(volume);
        return Assignment.mustCreate(id, orderId, domainVolume, location);
    }
}
