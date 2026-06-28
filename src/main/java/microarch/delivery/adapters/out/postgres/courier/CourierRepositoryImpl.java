package microarch.delivery.adapters.out.postgres.courier;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.out.postgres.JpaException;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CourierRepositoryImpl implements CourierRepository {

    private final CourierJpaRepository jpaRepository;

    @Override
    public void save(Courier courier) {
        var entity = CourierJpaEntity.fromDomain(courier);
        var id = entity.getId();
        if (jpaRepository.findById(id).isPresent()) {
            throw new JpaException(String.format("Courier already exists by id: (%s)", id));
        }
        jpaRepository.save(entity);
    }

    @Override
    public void update(Courier courier) {
        var entity = CourierJpaEntity.fromDomain(courier);
        var id = entity.getId();
        if (jpaRepository.findById(id).isEmpty()) {
            throw new JpaException(String.format("Courier not found by id: (%s)", id));
        }
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Courier> getById(UUID id) {
        return jpaRepository.findById(id)
                .map(CourierJpaEntity::toDomain);
    }

    @Override
    public List<Courier> getAll() {
        return jpaRepository.findAll()
                .stream()
                .map(CourierJpaEntity::toDomain)
                .toList();
    }
}
