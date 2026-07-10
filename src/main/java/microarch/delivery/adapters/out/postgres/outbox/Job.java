package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import libs.ddd.DomainEvent;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Job {
    private final DomainEventProducer domainEventProducer;
    private final OutboxJpaRepository jpa;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    public void run() {
        var outboxMessages = jpa.findUnprocessedMessages();
        for (var outboxMessage : outboxMessages) {
            try {
                // Динамически находим класс события
                var eventClassName = outboxMessage.getEventType();
                var eventClass = Class.forName(eventClassName);
                var eventObject = objectMapper.readValue(outboxMessage.getPayload(), eventClass);

                // Проверяем, что это DomainEvent
                if (!(eventObject instanceof DomainEvent domainEvent)) {
                    throw new IllegalStateException("Invalid outbox message type: " + eventClass);
                }

                // Публикуем доменное событие
                domainEventProducer.produce(domainEvent);

                // Отмечаем как отправленное
                outboxMessage.markAsProcessed();
                jpa.save(outboxMessage);
            } catch (Exception e) {
                System.err.println("Failed to publish outbox message: " + e.getMessage());
            }
        }
    }
}
