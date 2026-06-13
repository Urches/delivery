package microarch.delivery.core.domain.model.courier;

import libs.ddd.Aggregate;
import libs.errs.*;
import libs.errs.Error;
import libs.util.ReasonedResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.Volume;

import java.util.*;

/**
 * Courier - Aggregate Root, представляющий курьера.
 * <p>
 * Бизнес-правила: - Courier состоит из: Id, Name, Location, MaxVolume, Assignments - Каждый курьер может взять не более
 * 20 литров объема (MaxVolume = 20) - Курьер должен уметь проверять «может ли он взять +1 заказ?». Курьер может брать
 * новые заказы, если сумма объемов всех заказов с учетом нового не превышает максимум. - Если курьер взял заказ, то в
 * Assignment при этом устанавливается ID заказа. - Курьер может завершить Assignment, но только если он находится в 1
 * клетке от заказа или ближе. - Курьер может изменить свой Location. Курьер может переместиться в любой валидный
 * Location.
 */
@Getter
public class Courier extends Aggregate<UUID> {

    private static final int MAX_VOLUME_LIMIT = 20;

    private final String name;
    private Location location;
    private final Volume maxVolume;
    private final List<Assignment> assignments = new ArrayList<>();

    /**
     * Конструктор для использования в фабричном методе create и в инфраструктуре (маппинг). Имеет видимость
     * package-private для доступа из слоя persistence.
     */
    private Courier(UUID id, String name, Location location, Volume maxVolume) {
        super(id);
        this.name = name;
        this.location = location;
        this.maxVolume = maxVolume;
    }

    /**
     * Фабричный метод для создания Courier.
     * <p>
     * Курьер может быть создан при передаче Name, Location. MaxVolume автоматически устанавливается в 20.
     *
     * @param id       уникальный идентификатор курьера (UUID)
     * @param name     имя курьера (String)
     * @param location местоположение курьера (Location)
     * @return Result с Courier при успехе или Error при неудаче
     */
    public static Result<Courier, Error> create(UUID id, String name, Location location) {
        var error = Guard.combine(
                Guard.againstNullOrEmpty(id, "id"),
                Guard.againstNullOrEmpty(name, "name"),
                location == null ? GeneralErrors.valueIsRequired("location") : null);

        if (error != null) {
            return Result.failure(error);
        }

        var maxVolume = Volume.create(MAX_VOLUME_LIMIT).getValueOrThrow();
        return Result.success(new Courier(id, name, location, maxVolume));
    }

    /**
     * Проверяет, может ли курьер взять заказ с указанным объемом.
     * <p>
     * Курьер может брать новые заказы, если сумма объемов всех заказов с учетом нового не превышает максимум (20
     * литров).
     *
     * @param order - заказ, который нужно проверить
     * @return ReasonedResult с true при успехе или причинами отказа при неудаче
     */
    public ReasonedResult<Boolean> canTakeOrder(Order order) {
        Objects.requireNonNull(order, "Order must not be null");

        var newCurrentVolumeResult = getCurrentVolume()
                .map(volume -> volume.plus(order.getVolume()))
                .orElse(Result.success(order.getVolume()));

        if (newCurrentVolumeResult.isFailure()) {
            return ReasonedResult.withReason(false, newCurrentVolumeResult.getError());
        }

        var newVolumeCandidate = newCurrentVolumeResult.getValue();
        if (newVolumeCandidate.lessOrEqual(maxVolume)) {
            return ReasonedResult.withNoReason(true);
        } else {
            return ReasonedResult.withReason(false, String.format(
                    "Cannot take assignment: new volume (%s) exceeds max volume (%s)",
                    newVolumeCandidate, maxVolume));
        }
    }

    /**
     * Берет заказ в работу.
     * <p>
     * Если курьер взял заказ, то создаётся Assignment и добавляется assignments.
     *
     * @param order - заказ
     * @return Result с void при успехе или Error при неудаче
     */
    public Result<Void, Error> takeOrder(Order order) {
        Objects.requireNonNull(order, "Order must not be null");

        var result = canTakeOrder(order);
        var canTakeOrder = result.getValue();
        if (!canTakeOrder) {
            return Result.failure(GeneralErrors
                    .invalidOperation(String.format("Cannot take order. Reasons: %s", result.getReasons())));
        } else {
            var newCurrentVolumeResult = getCurrentVolume()
                    .map(volume -> volume.plus(order.getVolume()))
                    .orElse(Result.success(order.getVolume()));

            if (newCurrentVolumeResult.isFailure()) {
                return Result.failure(GeneralErrors
                        .invalidOperation(String.format("Cannot take order. Reasons: %s", result.getReasons())));
            }

            var assignmentResult = Assignment.create(UUID.randomUUID(), order.getId(), order.getVolume(), order.getLocation());
            if (assignmentResult.isFailure()) {
                return Result.failure(GeneralErrors
                        .invalidOperation(String.format("Cannot take order. Reasons: %s", result.getReasons())));
            }

            assignments.add(assignmentResult.getValue());
            return Result.success();
        }
    }

    /**
     * Завершает Assignment.
     * <p>
     * Курьер может завершить Assignment, только если он находится в 1 клетке от заказа или ближе.
     *
     * @return true если хотя бы один Assignment был завершён, false в противном случае
     */
    public boolean completeAssignment() {
        if (assignments.isEmpty())
            return false;

        return assignments.removeIf(assignment -> assignment.complete(this.location).isSuccess());
    }

    /**
     * Перемещает курьера в новое местоположение.
     * <p>
     * Курьер может переместиться в любой валидный Location.
     *
     * @param newLocation новое местоположение курьера
     * @return Result с void при успехе или Error при неудаче
     */
    public Result<Void, Error> move(Location newLocation) {
        Objects.requireNonNull(newLocation, "New location must not be null");

        this.location = newLocation;
        return Result.success();
    }

    /**
     * @return возвращает суммарный Volume всех заказов
     */
    public Optional<Volume> getCurrentVolume() {
        return this.assignments.stream()
                .map(Assignment::getVolume)
                .reduce((first, second) -> first.plus(second).getValueOrThrow());
    }
}
