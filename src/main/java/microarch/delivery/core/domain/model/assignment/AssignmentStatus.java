package microarch.delivery.core.domain.model.assignment;

/**
 * Status сущности Assignment.
 * <p>
 * Assigned - назначение было создано и назначено курьеру. Completed - назначение было завершено (заказ доставлен).
 */
public enum AssignmentStatus {
    /**
     * Заказ назначен на курьера.
     */
    ASSIGNED,

    /**
     * Заказ завершен (доставлен).
     */
    COMPLETED
}
