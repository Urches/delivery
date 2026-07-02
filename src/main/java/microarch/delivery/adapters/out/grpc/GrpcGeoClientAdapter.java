package microarch.delivery.adapters.out.grpc;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.ports.GeoClientPort;
import org.springframework.stereotype.Component;
import libs.errs.Error;
import libs.errs.Result;

/**
 * Adapter для вызова Geo сервиса через gRPC. Реализует порт GeoClientPort.
 */
@Slf4j
@Component
public class GrpcGeoClientAdapter implements GeoClientPort {

    private final ApplicationProperties properties;
    private final ManagedChannel channel;
    private final GeoGrpc.GeoBlockingStub geoBlockingStub;

    @Autowired
    public GrpcGeoClientAdapter(ApplicationProperties properties) {
        this.properties = properties;
        var grpc = properties.getGrpc().getGeoService();
        this.channel = ManagedChannelBuilder.forAddress(grpc.getHost(), grpc.getPort()).usePlaintext().build();
        this.geoBlockingStub = GeoGrpc.newBlockingStub(channel);
        log.info("Geo gRPC client initialized: host={}, port={}", grpc.getHost(), grpc.getPort());
    }

    @PreDestroy
    public void destroy() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            log.info("Geo gRPC channel closed");
        }
    }

    @Override
    public Result<Location, Error> getGeolocationByStreet(String street) {
        try {
            var request = GeoProto.GetGeolocationRequest.newBuilder().setStreet(street).build();

            var response = geoBlockingStub.getGeolocation(request);
            var location = response.getLocation();
            var locationResult = Location.create(location.getX(), location.getY());
            if (locationResult.isFailure()) {
                return Result.failure(locationResult.getError());
            }

            log.debug("Received location for street '{}': x={}, y={}", street, location.getX(), location.getY());
            return Result.success(locationResult.getValue());

        } catch (Exception e) {
            log.error("Failed to get geolocation for street '{}': {}", street, e.getMessage());
            return Result.failure(Error.of("geo.service.error", "Failed to get geolocation: " + e.getMessage()));
        }
    }
}
