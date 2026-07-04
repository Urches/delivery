package microarch.delivery.core.domain.model.order.events;

import libs.ddd.DomainEvent;
import lombok.Getter;
import microarch.delivery.core.domain.model.order.Order;

import java.util.UUID;

/**
 * Domain Event, возникающий при назначении заказа курьеру.
 */
@Getter
public class OrderAssignedEvent extends DomainEvent {
    private final UUID orderId;

    public OrderAssignedEvent(Order order) {
        super(order);
        this.orderId = order.getId();
    }
}
