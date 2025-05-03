package nl.bunq.hackathon.adapters.out;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.port.out.OrderMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock implementation of the OrderMatcher interface.
 * Returns a fixed list of items for testing purposes.
 */
//@Component
public class MockOrderMatcher implements OrderMatcher {

    @Override
    public List<Item> matchOrderWithBill(Bill bill, MultipartFile orderImage) {
        // Return a fixed list of items regardless of the bill content
        List<Item> items = new ArrayList<>();
        items.add(Item.builder()
                .id(UUID.randomUUID())
                .name("Pizza Margherita")
                .price(8.99)
                .quantity(1)
                .matched(false)
                .build());

        items.add(Item.builder()
                .id(UUID.randomUUID())
                .name("Coca Cola")
                .price(2.50)
                .quantity(2)
                .matched(false)
                .build());

        return items;
    }
}
