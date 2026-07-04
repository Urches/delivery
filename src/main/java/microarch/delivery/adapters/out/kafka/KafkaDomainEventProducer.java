package microarch.delivery.adapters.out.kafka;

import libs.ddd.DomainEvent;
import lombok.RequiredArgsConstructor;
import microarch.delivery.ApplicationProperties;
import microarch.delivery.core.domain.model.order.events.OrderAssignedEvent;
import microarch.delivery.core.domain.model.order.events.OrderCompletedEvent;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;
import queues.order.events.OrderEventsProto;

import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class KafkaDomainEventProducer implements DomainEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final ApplicationProperties properties;

    @Override
    public void produce(DomainEvent event) {
        try {
            var topic = properties.getKafka().getOrderEventsTopic();
            switch (event) {
                case OrderAssignedEvent assignedEvent -> {
                    var orderId = assignedEvent.getOrderId().toString();
                    var integrationEvent = OrderEventsProto.OrderAssignedIntegrationEvent.newBuilder()
                            .setOrderId(orderId)
                            .build();

                    kafkaTemplate.send(topic, orderId, integrationEvent.toByteArray()).get();
                }
                case OrderCompletedEvent orderCompletedEvent -> {
                    var orderId = orderCompletedEvent.getOrderId().toString();
                    var integrationEvent = OrderEventsProto.OrderCompletedIntegrationEvent.newBuilder()
                            .setOrderId(orderId)
                            .build();
                    kafkaTemplate.send(topic, orderId, integrationEvent.toByteArray()).get();
                }
                default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka publish interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Kafka publish failed", e);
        }
    }
}
