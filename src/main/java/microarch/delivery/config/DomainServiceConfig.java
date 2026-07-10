package microarch.delivery.config;

import microarch.delivery.core.domain.services.OrderDispatchService;
import microarch.delivery.core.domain.services.OrderDispatchServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public OrderDispatchService orderDispatchService() {
        return new OrderDispatchServiceImpl();
    }
}
