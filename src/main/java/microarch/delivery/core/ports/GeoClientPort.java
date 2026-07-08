package microarch.delivery.core.ports;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;

/**
 * Port для взаимодействия с сервисом Geo. Получает Location (координаты) по адресу улицы.
 */
public interface GeoClientPort {

    /**
     * Получает координаты (Location) для указанного адреса улицы.
     *
     * @param street
     *            название улицы
     *
     * @return Result с Location при успехе или Error при неудаче
     */
    Result<Location, Error> getGeolocationByStreet(String street);
}
