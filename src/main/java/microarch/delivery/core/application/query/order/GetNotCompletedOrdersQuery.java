package microarch.delivery.core.application.query.order;

import microarch.delivery.core.application.Query;

/**
 * Запрос на получение всех незавершенных заказов (в статусах Created или Assigned).
 */
public record GetNotCompletedOrdersQuery() implements Query {
}
