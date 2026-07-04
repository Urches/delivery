package microarch.delivery.adapters.out.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.net.ServerSocket;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.ApplicationProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Интеграционный тест для GrpcGeoClientAdapter с реальным gRPC сервером.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class GrpcGeoClientAdapterTest {

    private Server grpcServer;
    private GrpcGeoClientAdapter adapter;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private ApplicationProperties.Grpc grpc;

    @Mock
    private ApplicationProperties.Grpc.GeoService geoService;

    @BeforeEach
    void setUp() throws IOException {
        // Найти свободный порт
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        // Создать и запустить тестовый gRPC сервер
        grpcServer = ServerBuilder.forPort(port).addService(new TestGeoServiceImpl()).build().start();

        log.info("Test gRPC server started on port {}", port);

        // Setup mocks for properties
        when(properties.getGrpc()).thenReturn(grpc);
        when(grpc.getGeoService()).thenReturn(geoService);
        when(geoService.getHost()).thenReturn("localhost");
        when(geoService.getPort()).thenReturn(port);

        // Создать адаптер с моком properties
        adapter = new GrpcGeoClientAdapter(properties);
    }

    @AfterEach
    void tearDown() {
        if (adapter != null) {
            adapter.destroy();
        }
        if (grpcServer != null) {
            grpcServer.shutdownNow();
        }
    }

    @Test
    void shouldGetGeolocationFromRealGrpcServer() {
        // Arrange
        String street = "Tverskaya";

        // Act
        var result = adapter.getGeolocationByStreet(street);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(1);
        assertThat(result.getValue().getY()).isEqualTo(2);
    }

    /**
     * Тестовая реализация gRPC сервиса Geo.
     */
    private static class TestGeoServiceImpl extends GeoGrpc.GeoImplBase {
        @Override
        public void getGeolocation(GeoProto.GetGeolocationRequest request,
                StreamObserver<GeoProto.GetGeolocationReply> responseObserver) {
            // Возвращаем фиксированные координаты для любого запроса
            var location = GeoProto.Location.newBuilder().setX(1).setY(2).build();

            var reply = GeoProto.GetGeolocationReply.newBuilder().setLocation(location).build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
