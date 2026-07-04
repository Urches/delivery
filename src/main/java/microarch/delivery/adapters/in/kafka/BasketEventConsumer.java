package microarch.delivery.adapters.in.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.command.order.CreateBasketOrderCommand;
import microarch.delivery.core.application.command.order.CreateBasketOrderCommandHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import queues.basket.events.BasketEventsProto;

import java.util.UUID;

/**
 * Kafka Consumer для обработки событий из топика basket.events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BasketEventConsumer {

    private final CreateBasketOrderCommandHandler createBasketOrderCommandHandler;

    @KafkaListener(topics = "${app.kafka.basket-events-topic}", groupId = "delivery-service")
    public void consume(byte[] message) {
        try {
            var event = BasketEventsProto.BasketConfirmedIntegrationEvent.parseFrom(message);
            log.info("Received BasketConfirmedIntegrationEvent for basket: {}", event.getBasketId());

            var basketId = UUID.fromString(event.getBasketId());
            var address = event.getAddress();
            var items = event.getItemsList().stream().map(item -> new CreateBasketOrderCommand.BasketItem(item.getId(),
                    item.getGoodId(), item.getTitle(), item.getPrice(), item.getQuantity())).toList();

            var command = CreateBasketOrderCommand.create(basketId, address.getCountry(), address.getCity(),
                    address.getStreet(), address.getHouse(), address.getApartment(), event.getVolume(), items);

            var result = createBasketOrderCommandHandler.handle(command);
            if (result.isFailure()) {
                log.error("Failed to create order for basket {}: {}", event.getBasketId(), result.getError());
                return;
            }
            log.info("Order {} successfully created for basket: {}", result.getValue().getId(), event.getBasketId());
        } catch (InvalidProtocolBufferException ex) {
            throw new RuntimeException("Failed to parse protobuf message", ex);
        }
    }
}
