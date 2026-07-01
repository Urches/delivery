package microarch.delivery.config;

import microarch.delivery.core.domain.service.OrderDispatchService;
import microarch.delivery.core.domain.service.OrderDispatchServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public OrderDispatchService orderDispatchService() {
        return new OrderDispatchServiceImpl();
    }
}
