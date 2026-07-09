package microarch.delivery;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private final Grpc grpc = new Grpc();
    private final Kafka kafka = new Kafka();

    public Grpc getGrpc() {
        return grpc;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public static class Grpc {
        private final GeoService geoService = new GeoService();

        public GeoService getGeoService() {
            return geoService;
        }

        public static class GeoService {
            private String host;
            private int port;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }
        }
    }

    public static class Kafka {
        private String stockEventsTopic;
        private String basketEventsTopic;

        public String getStockEventsTopic() {
            return stockEventsTopic;
        }

        public void setStockEventsTopic(String stockEventsTopic) {
            this.stockEventsTopic = stockEventsTopic;
        }

        public String getBasketEventsTopic() {
            return basketEventsTopic;
        }

        public void setBasketEventsTopic(String basketEventsTopic) {
            this.basketEventsTopic = basketEventsTopic;
        }
    }
}