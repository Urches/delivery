package libs.util;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import libs.errs.Error;

/**
 * Результат со списком причин/комментариев к полученному результату.
 * <p>
 * Используется для случаев, когда нужно вернуть результат и дополнительную информацию (причины, комментарии,
 * предупреждения) о том, как он был получен.
 *
 * @param <T> тип результата
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReasonedResult<T> {

    private final T value;
    private final List<String> reasons;

    /* ---------- factory ---------- */

    public static <T> ReasonedResult<T> withNoReason(T value) {
        Objects.requireNonNull(value);
        return new ReasonedResult<>(value, List.of());
    }

    public static <T> ReasonedResult<T> withReason(T value, String reason) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(reason);
        return new ReasonedResult<>(value, List.of(reason));
    }

    public static <T> ReasonedResult<T> withReason(T value, Error error) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(error);
        return new ReasonedResult<>(value, List.of(error.getMessage()));
    }

    public static <T> ReasonedResult<T> withReasons(T value, List<String> reasons) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(reasons);
        return new ReasonedResult<>(value, new ArrayList<>(reasons));
    }
}
