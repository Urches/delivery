package microarch.delivery.adapters.out.postgres;

public class JpaException extends RuntimeException {
    public JpaException(String message) {
        super(message);
    }
}
