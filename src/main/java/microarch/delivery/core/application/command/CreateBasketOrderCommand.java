package microarch.delivery.core.application.command;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * Команда на создание заказа из события корзины.
 */
@Getter
public final class CreateBasketOrderCommand {
    private final UUID basketId;
    private final String country;
    private final String city;
    private final String street;
    private final String house;
    private final String apartment;
    private final int volume;
    private final List<BasketItem> items;

    private CreateBasketOrderCommand(UUID basketId, String country, String city, String street, String house,
                                     String apartment, int volume, List<BasketItem> items) {
        this.basketId = basketId;
        this.country = country;
        this.city = city;
        this.street = street;
        this.house = house;
        this.apartment = apartment;
        this.volume = volume;
        this.items = items;
    }

    /**
     * Фабричный метод для создания команды из примитивов.
     */
    public static CreateBasketOrderCommand create(UUID basketId, String country, String city, String street,
                                                  String house, String apartment, int volume, List<BasketItem> items) {
        return new CreateBasketOrderCommand(basketId, country, city, street, house, apartment, volume, items);
    }

    /**
     * Вложенный класс для представления элемента корзины.
     */
    public record BasketItem(String id, String goodId, String title, double price, int quantity) {
    }
}
