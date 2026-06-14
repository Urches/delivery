package microarch.delivery.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "microarch.delivery.adapters.out.postgres")
@EntityScan(basePackages = { "microarch.delivery.adapters.out.postgres" })
public class JpaConfig {
}
