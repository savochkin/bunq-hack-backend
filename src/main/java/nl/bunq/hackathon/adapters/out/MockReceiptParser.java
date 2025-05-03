package nl.bunq.hackathon.adapters.out;

import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.model.Receipt;
import nl.bunq.hackathon.app.port.out.ReceiptParser;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mock implementation of the ReceiptParser interface.
 * Returns predefined receipt data for testing purposes.
 */
//@Component
public class MockReceiptParser implements ReceiptParser {

    @Override
    public Receipt parseReceipt(MultipartFile receiptImage) {
        // In a real implementation, this would analyze the image and extract receipt data
        // For now, we'll just return mock data

        // Create some predefined items
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

        items.add(Item.builder()
                .id(UUID.randomUUID())
                .name("Tiramisu")
                .price(5.99)
                .quantity(1)
                .matched(false)
                .build());

        // Calculate total amount
        double totalAmount = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Create and return a mock receipt
        return Receipt.builder()
                .id(UUID.randomUUID())
                .storeName("Pizza Restaurant")
                .date(LocalDateTime.now())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }
}
