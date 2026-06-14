package microarch.delivery.adapters.out.postgres.order;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.out.postgres.JpaException;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    @Override
    public void save(Order order) {
        var entity = OrderJpaEntity.fromDomain(order);
        var id = entity.getId();
        if (jpaRepository.findById(id).isPresent()) {
            throw new JpaException(String.format("Order already exists by id: (%s)", id));
        }
        jpaRepository.save(entity);
    }

    @Override
    public void update(Order order) {
        var entity = OrderJpaEntity.fromDomain(order);
        var id = entity.getId();
        if (jpaRepository.findById(id).isEmpty()) {
            throw new JpaException(String.format("Order not found by id: (%s)", id));
        }
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Order> getById(UUID id) {
        return jpaRepository.findById(id)
                .map(OrderJpaEntity::toDomain);
    }

    @Override
    public Optional<Order> getOneNew() {
        return jpaRepository.findAnyNewOrder()
                .map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> getAllAssigned() {
        return jpaRepository.findAssignedOrders()
                .stream()
                .map(OrderJpaEntity::toDomain)
                .toList();
    }
}
