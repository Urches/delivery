package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import microarch.delivery.adapters.out.postgres.PostgresIntegrationTestBase;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.domain.model.order.events.OrderCompletedEvent;
import microarch.delivery.core.ports.DomainEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Интеграционные тесты для Job - обработчика outbox-сообщений.
 */
@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JobIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private OutboxJpaRepository outboxJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DomainEventProducer domainEventProducer;

    @Autowired
    private Job job;

    @Test
    @DisplayName("Должен успешно обрабатывать необработанные сообщения")
    void shouldProcessUnprocessedMessagesSuccessfully() {
        // Arrange
        var order = createTestOrder();
        var event = new OrderCompletedEvent(order);

        var outboxMessage = createOutboxMessage(event);
        outboxJpaRepository.save(outboxMessage);

        // Act
        job.run();

        // Assert
        var captor = ArgumentCaptor.forClass(OrderCompletedEvent.class);

        verify(domainEventProducer, times(1)).produce(captor.capture());
        assertThat(event.getEventId()).isEqualTo(captor.getValue().getEventId());

        var saved = outboxJpaRepository.findById(outboxMessage.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getProcessedOnUtc()).isNotNull();
    }

    @Test
    @DisplayName("Должен обрабатывать сообщения только с processedOnUtc == null")
    void shouldProcessOnlyUnprocessedMessages() {
        // Arrange
        var order1 = createTestOrder();
        var order2 = createTestOrder();

        var event1 = new OrderCompletedEvent(order1);
        var event2 = new OrderCompletedEvent(order2);

        var message1 = createOutboxMessage(event1);
        var message2 = createOutboxMessage(event2);

        message1.markAsProcessed();
        outboxJpaRepository.save(message1);
        outboxJpaRepository.save(message2);

        // Act
        job.run();

        // Assert
        var captor = ArgumentCaptor.forClass(OrderCompletedEvent.class);

        verify(domainEventProducer, times(1)).produce(captor.capture());
        assertThat(event2.getEventId()).isEqualTo(captor.getValue().getEventId());

        var savedMessage2 = outboxJpaRepository.findById(message2.getId());
        assertThat(savedMessage2).isPresent();
        assertThat(savedMessage2.get().getProcessedOnUtc()).isNotNull();
    }

    private Order createTestOrder() {
        return Order.mustCreate(UUID.randomUUID(), Location.mustCreate(5, 3), Volume.mustCreate(10));
    }

    @SneakyThrows
    private OutboxMessage createOutboxMessage(OrderCompletedEvent event) {
        return new OutboxMessage(UUID.randomUUID(), event.getClass().getName(), event.getOrderId().toString(), "Order",
                objectMapper.writeValueAsString(event), Instant.now());
    }
}
