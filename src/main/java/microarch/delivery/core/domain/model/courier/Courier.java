package microarch.delivery.core.domain.model.courier;

import libs.ddd.Aggregate;
import libs.errs.*;
import libs.errs.Error;
import libs.util.ReasonedResult;
import lombok.Getter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.assignment.Assignment;
import microarch.delivery.core.domain.model.order.Volume;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
    private final Volume maxVolume; // не уверен, что maxVolume должен быть отдельным свойством, но оставил как описано
    private Volume currentVolume;
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
        var error = Guard.combine(Guard.againstNullOrEmpty(id, "id"), Guard.againstNullOrEmpty(name, "name"),
                location == null ? GeneralErrors.valueIsRequired("location") : null);

        if (error != null) {
            return Result.failure(error);
        }

        var maxVolume = Volume.create(MAX_VOLUME_LIMIT).getValue();
        return Result.success(new Courier(id, name, location, maxVolume));
    }

    /**
     * Проверяет, может ли курьер взять заказ с указанным объемом.
     * <p>
     * Курьер может брать новые заказы, если сумма объемов всех заказов с учетом нового не превышает максимум (20
     * литров).
     *
     * @param assignment заказа, который нужно проверить
     * @return ReasonedResult с true при успехе или причинами отказа при неудаче
     */
    public ReasonedResult<Boolean> canTakeAssignment(Assignment assignment) {
        Objects.requireNonNull(assignment, "Assignment volume must not be null");
        if (this.assignments.contains(assignment)) {
            return ReasonedResult.withReason(false,
                    String.format("Can't take assignment (%s), already taken", assignment.getId()));
        }

        var assignmentVolume = assignment.getVolume();
        var volumeCandidateResult = assignmentVolume.newVolumeSafe(currentVolume);
        if (volumeCandidateResult.isFailure()) {
            return ReasonedResult.withReason(false, volumeCandidateResult.getError());
        }
        var newVolumeCandidate = volumeCandidateResult.getValue();
        if (newVolumeCandidate.lessOrEqual(maxVolume)) {
            return ReasonedResult.withNoReason(true);
        } else {
            return ReasonedResult.withReason(false, String.format(
                    "Cannot take assignment: current volume (%s) + assignment volume (%s) exceeds max volume (%s)",
                    currentVolume, assignmentVolume, maxVolume));
        }
    }

    /**
     * Берет заказ в работу.
     * <p>
     * Если курьер взял заказ, то в assignments добавляется assignments.
     *
     * @param assignment назначение (Assignment) с объемом заказа
     * @return Result с void при успехе или Error при неудаче
     */
    public Result<Void, Error> takeAssignment(Assignment assignment) {
        Objects.requireNonNull(assignment, "Assignment must not be null");

        var result = canTakeAssignment(assignment);
        var canTakeAssignment = result.getValue();
        if (!canTakeAssignment) {
            return Result.failure(GeneralErrors
                    .invalidOperation(String.format("Cannot take assignment. Reasons: %s", result.getReasons())));
        } else {
            var newCurrentVolumeResult = assignment.getVolume().newVolumeSafe(currentVolume);
            if (newCurrentVolumeResult.isFailure()) {
                // should not happen
                throw new DomainInvariantException(newCurrentVolumeResult.getError());
            }
            currentVolume = newCurrentVolumeResult.getValue();
            assignments.add(assignment);
            return Result.success();
        }
    }

    /**
     * Завершает Assignment.
     * <p>
     * Курьер может завершить Assignment, только если он находится в 1 клетке от заказа или ближе.
     *
     * @param assignment назначение для завершения
     * @return Result с void при успехе или Error при неудаче
     */
    public Result<Void, Error> completeAssignment(Assignment assignment) {
        Objects.requireNonNull(assignment, "Assignment must not be null");

        if (!assignments.contains(assignment)) {
            return Result.failure(GeneralErrors.invalidOperation(String.format(
                    "Cannot complete assignment: assignment %s is not in courier's assignments", assignment.getId())));
        }

        var result = assignment.complete(this.location);
        if (result.isFailure()) {
            return Result.failure(result.getError());
        }

        assignments.remove(assignment);
        return Result.success();
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
}
