package microarch.delivery.core.application.command.assignment;

import microarch.delivery.core.application.Command;

/**
 * Команда на назначение заказа курьеру. Не содержит полей - система сама выбирает первый незавершенный заказ и
 * подходящего курьера.
 */
public record AssignOrderCommand() implements Command {
}
