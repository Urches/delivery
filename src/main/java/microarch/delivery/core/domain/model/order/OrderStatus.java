package microarch.delivery.core.domain.model.order;

/**
 * Status заказа.
 * <p>
 * Created - заказ создан. Assigned - заказ назначен курьеру. Completed - заказ завершен (доставлен).
 */
public enum OrderStatus {
    /**
     * Заказ создан.
     */
    CREATED,

    /**
     * Заказ назначен курьеру.
     */
    ASSIGNED,

    /**
     * Заказ завершен (доставлен).
     */
    COMPLETED
}
