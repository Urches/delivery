package microarch.delivery.config;

import microarch.delivery.core.application.command.AssignOrderCommandHandler;
import microarch.delivery.core.application.command.CompleteOrderCommandHandler;
import microarch.delivery.core.application.command.CreateCourierCommandHandler;
import microarch.delivery.core.application.command.MoveCourierCommandHandler;
import microarch.delivery.core.application.command.CreateOrderCommandHandler;
import microarch.delivery.core.application.query.GetAllCouriersQueryHandler;
import microarch.delivery.core.application.query.GetNotCompletedOrdersQueryHandler;
import microarch.delivery.core.domain.services.OrderDispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.GeoClientPort;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

/**
 * Конфигурация для всех Use Case обработчиков (Command Handlers и Query Handlers).
 */
@Configuration
public class ApplicationServiceConfig {

    // Command Handlers

    @Bean
    public Random random() {
        return new Random();
    }

    @Bean
    public CreateOrderCommandHandler createOrderCommandHandler(OrderRepository orderRepository,
            GeoClientPort geoClientPort) {
        return new CreateOrderCommandHandler(orderRepository, geoClientPort);
    }

    @Bean
    public CreateCourierCommandHandler createCourierCommandHandler(CourierRepository courierRepository, Random random) {
        return new CreateCourierCommandHandler(courierRepository, random);
    }

    @Bean
    public MoveCourierCommandHandler moveCourierCommandHandler(CourierRepository courierRepository) {
        return new MoveCourierCommandHandler(courierRepository);
    }

    @Bean
    public AssignOrderCommandHandler assignOrderCommandHandler(OrderRepository orderRepository,
            CourierRepository courierRepository, OrderDispatchService dispatchService) {
        return new AssignOrderCommandHandler(orderRepository, courierRepository, dispatchService);
    }

    @Bean
    public CompleteOrderCommandHandler completeOrderCommandHandler(CourierRepository courierRepository,
            OrderRepository orderRepository) {
        return new CompleteOrderCommandHandler(courierRepository, orderRepository);
    }

    // Query Handlers

    @Bean
    public GetAllCouriersQueryHandler getAllCouriersQueryHandler(CourierRepository courierRepository) {
        return new GetAllCouriersQueryHandler(courierRepository);
    }

    @Bean
    public GetNotCompletedOrdersQueryHandler getNotCompletedOrdersQueryHandler(OrderRepository orderRepository) {
        return new GetNotCompletedOrdersQueryHandler(orderRepository);
    }
}
