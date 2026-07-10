package microarch.delivery.adapters.in.kafka;

import libs.errs.Result;
import microarch.delivery.ApplicationProperties;
import microarch.delivery.adapters.out.postgres.PostgresIntegrationTestBase;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.ports.GeoClientPort;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static queues.basket.events.BasketEventsProto.*;

/**
 * Интеграционный тест для Kafka Consumer.
 */
@Testcontainers
@SpringBootTest
class BasketEventConsumerIntegrationTest extends PostgresIntegrationTestBase {
    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    private ApplicationProperties properties;

    @MockitoBean
    private GeoClientPort geoClientPort;

    @Test
    void shouldCreateOrderFromKafkaMessage() throws Exception {
        // Arrange
        UUID basketId = UUID.randomUUID();
        var location = Location.mustCreate(1, 2);
        var volume = Volume.mustCreate(15);
        when(geoClientPort.getGeolocationByStreet(anyString())).thenReturn(Result.success(location));

        var event = BasketConfirmedIntegrationEvent
                .newBuilder().setBasketId(basketId.toString())
                .setAddress(Address.newBuilder()
                        .setCountry("Russia")
                        .setCity("Moscow")
                        .setStreet("Tverskaya")
                        .setHouse("10")
                        .setApartment("5"))
                .setDeliveryPeriod(
                        DeliveryPeriod.newBuilder()
                                .setFrom(1)
                                .setTo(5))
                .setVolume(15).build();

        // Act - отправляем сообщение в Kafka
        kafkaTemplate.send(properties.getKafka().getBasketEventsTopic(), basketId.toString(), event.toByteArray()).get();

        // Assert - ждем появления заказа в БД
        await().atMost(Duration.ofSeconds(10)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            var orderOpt = orderRepository.getById(basketId);
            assertThat(orderOpt).isPresent();

            var order = orderOpt.get();
            assertThat(order.getVolume()).isEqualTo(volume);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getLocation()).isEqualTo(location);
        });
    }
}
