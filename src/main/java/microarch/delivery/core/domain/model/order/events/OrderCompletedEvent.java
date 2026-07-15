package microarch.delivery.core.domain.model.order.events;

import libs.ddd.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;

import java.util.UUID;

/**
 * Domain Event, возникающий при завершении заказа (доставке).
 */
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class OrderCompletedEvent extends DomainEvent {
    private final UUID orderId;

    public OrderCompletedEvent(Order order) {
        super(order);
        this.orderId = order.getId();
    }
}
