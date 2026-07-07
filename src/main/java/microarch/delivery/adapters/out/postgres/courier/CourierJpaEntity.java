package microarch.delivery.adapters.out.postgres.courier;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Volume;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "couriers")
@NoArgsConstructor
@Getter
public class CourierJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location_x", nullable = false)
    private int locationX;

    @Column(name = "location_y", nullable = false)
    private int locationY;

    @Column(name = "max_volume", nullable = false)
    private int maxVolume;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "courier_id", nullable = true)
    private final List<AssignmentJpaEntity> assignments = new ArrayList<>();

    public CourierJpaEntity(UUID id, String name, int locationX, int locationY, int maxVolume) {
        this.id = id;
        this.name = name;
        this.locationX = locationX;
        this.locationY = locationY;
        this.maxVolume = maxVolume;
    }

    public static CourierJpaEntity fromDomain(Courier courier) {
        var location = courier.getLocation();
        var volume = courier.getMaxVolume();
        var entity = new CourierJpaEntity(courier.getId(), courier.getName(), location.getX(), location.getY(),
                volume.getValue());

        var assignmentEntities = courier.getAssignments().stream().map(AssignmentJpaEntity::fromDomain).toList();

        entity.assignments.clear();
        entity.assignments.addAll(assignmentEntities);
        return entity;
    }

    public Courier toDomain() {
        var location = Location.mustCreate(locationX, locationY);
        var domainVolume = Volume.mustCreate(maxVolume);
        var domainAssignments = assignments.stream().map(AssignmentJpaEntity::toDomain).toList();
        return Courier.of(id, name, location, domainVolume, domainAssignments);
    }
}
