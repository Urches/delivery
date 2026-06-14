package microarch.delivery.adapters.out.postgres.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    @Query(value = "SELECT o FROM OrderJpaEntity o WHERE o.status = 'CREATED' order by o.id limit 1")
    Optional<OrderJpaEntity> findAnyNewOrder();

    @Query(value = "SELECT o FROM OrderJpaEntity o WHERE o.status = 'ASSIGNED'")
    List<OrderJpaEntity> findAssignedOrders();
}
