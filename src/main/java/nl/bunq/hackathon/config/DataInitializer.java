package nl.bunq.hackathon.config;

import lombok.RequiredArgsConstructor;
import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.model.Receipt;
import nl.bunq.hackathon.app.port.out.BillRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Component to initialize the repository with sample restaurant bills on application startup.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BillRepository billRepository;

    // Fixed UUIDs for Italian restaurant items
    private static final UUID PIZZA_ID = UUID.fromString("a1b2c3d4-e5f6-47a8-b9c0-d1e2f3a4b5c6");
    private static final UUID SPAGHETTI_ID = UUID.fromString("b2c3d4e5-f6a7-48b9-c0d1-e2f3a4b5c6d7");
    private static final UUID TIRAMISU_ID = UUID.fromString("c3d4e5f6-a7b8-49c0-d1e2-f3a4b5c6d7e8");
    private static final UUID WINE_ID = UUID.fromString("d4e5f6a7-b8c9-40d1-e2f3-a4b5c6d7e8f9");

    // Fixed UUIDs for Sushi restaurant items
    private static final UUID CALIFORNIA_ROLL_ID = UUID.fromString("e5f6a7b8-c9d0-41e2-f3a4-b5c6d7e8f9a0");
    private static final UUID SALMON_NIGIRI_ID = UUID.fromString("f6a7b8c9-d0e1-42f3-a4b5-c6d7e8f9a0b1");
    private static final UUID MISO_SOUP_ID = UUID.fromString("a7b8c9d0-e1f2-43a4-b5c6-d7e8f9a0b1c2");
    private static final UUID GREEN_TEA_ID = UUID.fromString("b8c9d0e1-f2a3-44b5-c6d7-e8f9a0b1c2d3");

    // Fixed UUIDs for Burger joint items
    private static final UUID CHEESEBURGER_ID = UUID.fromString("c9d0e1f2-a3b4-45c6-d7e8-f9a0b1c2d3e4");
    private static final UUID FRIES_ID = UUID.fromString("d0e1f2a3-b4c5-46d7-e8f9-a0b1c2d3e4f5");
    private static final UUID MILKSHAKE_ID = UUID.fromString("e1f2a3b4-c5d6-47e8-f9a0-b1c2d3e4f5a6");
    private static final UUID COLA_ID = UUID.fromString("f2a3b4c5-d6e7-48f9-a0b1-c2d3e4f5a6b7");

    // Fixed UUIDs for receipts
    private static final UUID ITALIAN_RECEIPT_ID = UUID.fromString("a3b4c5d6-e7f8-49a0-b1c2-d3e4f5a6b7c8");
    private static final UUID SUSHI_RECEIPT_ID = UUID.fromString("b4c5d6e7-f8a9-40b1-c2d3-e4f5a6b7c8d9");
    private static final UUID BURGER_RECEIPT_ID = UUID.fromString("c5d6e7f8-a9b0-41c2-d3e4-f5a6b7c8d9e0");

    // Fixed UUIDs for bills
    private static final UUID ITALIAN_BILL_ID = UUID.fromString("d6e7f8a9-b0c1-42d3-e4f5-a6b7c8d9e0f1");
    private static final UUID SUSHI_BILL_ID = UUID.fromString("e7f8a9b0-c1d2-43e4-f5a6-b7c8d9e0f1a2");
    private static final UUID BURGER_BILL_ID = UUID.fromString("f8a9b0c1-d2e3-44f5-a6b7-c8d9e0f1a2b3");

    @Override
    public void run(String... args) {
        // Create and save sample restaurant bills
        createSampleRestaurantBills();
    }

    private void createSampleRestaurantBills() {
        // Create first bill - Italian Restaurant
        Bill italianBill = createItalianRestaurantBill();
        billRepository.save(italianBill);

        // Create second bill - Sushi Restaurant
        Bill sushiBill = createSushiRestaurantBill();
        billRepository.save(sushiBill);

        // Create third bill - Burger Joint
        Bill burgerBill = createBurgerJointBill();
        billRepository.save(burgerBill);

        System.out.println("Sample restaurant bills created: " + billRepository.findAll().size());
    }

    private Bill createItalianRestaurantBill() {
        // Create items for the Italian restaurant receipt
        List<Item> italianItems = new ArrayList<>();
        italianItems.add(Item.builder()
                .id(PIZZA_ID)
                .name("Pizza Margherita")
                .price(8.99)
                .quantity(1)
                .matched(false)
                .build());

        italianItems.add(Item.builder()
                .id(SPAGHETTI_ID)
                .name("Spaghetti Carbonara")
                .price(12.50)
                .quantity(1)
                .matched(false)
                .build());

        italianItems.add(Item.builder()
                .id(TIRAMISU_ID)
                .name("Tiramisu")
                .price(5.99)
                .quantity(1)
                .matched(false)
                .build());

        italianItems.add(Item.builder()
                .id(WINE_ID)
                .name("House Wine")
                .price(18.00)
                .quantity(1)
                .matched(false)
                .build());

        // Create Italian restaurant receipt
        Receipt italianReceipt = Receipt.builder()
                .id(ITALIAN_RECEIPT_ID)
                .storeName("Bella Italia")
                .date(LocalDateTime.now().minusDays(2))
                .items(italianItems)
                .totalAmount(45.48) // 8.99 + 12.50 + 5.99 + 18.00
                .build();

        // Create Italian restaurant bill
        return Bill.builder()
                .id(ITALIAN_BILL_ID)
                .name("Italian dinner with friends")
                .createdAt(LocalDateTime.now().minusDays(2))
                .receipts(List.of(italianReceipt))
                .shareCode("ITALIAN1")
                .build();
    }

    private Bill createSushiRestaurantBill() {
        // Create items for the sushi restaurant receipt
        List<Item> sushiItems = new ArrayList<>();
        sushiItems.add(Item.builder()
                .id(CALIFORNIA_ROLL_ID)
                .name("California Roll")
                .price(6.99)
                .quantity(2)
                .matched(false)
                .build());

        sushiItems.add(Item.builder()
                .id(SALMON_NIGIRI_ID)
                .name("Salmon Nigiri")
                .price(8.49)
                .quantity(1)
                .matched(false)
                .build());

        sushiItems.add(Item.builder()
                .id(MISO_SOUP_ID)
                .name("Miso Soup")
                .price(3.29)
                .quantity(2)
                .matched(false)
                .build());

        sushiItems.add(Item.builder()
                .id(GREEN_TEA_ID)
                .name("Green Tea")
                .price(2.99)
                .quantity(2)
                .matched(false)
                .build());

        // Create sushi restaurant receipt
        Receipt sushiReceipt = Receipt.builder()
                .id(SUSHI_RECEIPT_ID)
                .storeName("Tokyo Sushi")
                .date(LocalDateTime.now().minusDays(5))
                .items(sushiItems)
                .totalAmount(35.03) // 6.99*2 + 8.49 + 3.29*2 + 2.99*2
                .build();

        // Create sushi restaurant bill
        return Bill.builder()
                .id(SUSHI_BILL_ID)
                .name("Sushi night")
                .createdAt(LocalDateTime.now().minusDays(5))
                .receipts(List.of(sushiReceipt))
                .shareCode("SUSHI123")
                .build();
    }

    private Bill createBurgerJointBill() {
        // Create items for the burger joint receipt
        List<Item> burgerItems = new ArrayList<>();
        burgerItems.add(Item.builder()
                .id(CHEESEBURGER_ID)
                .name("Cheeseburger")
                .price(9.99)
                .quantity(2)
                .matched(false)
                .build());

        burgerItems.add(Item.builder()
                .id(FRIES_ID)
                .name("French Fries")
                .price(3.99)
                .quantity(2)
                .matched(false)
                .build());

        burgerItems.add(Item.builder()
                .id(MILKSHAKE_ID)
                .name("Chocolate Milkshake")
                .price(4.50)
                .quantity(1)
                .matched(false)
                .build());

        burgerItems.add(Item.builder()
                .id(COLA_ID)
                .name("Coca Cola")
                .price(2.50)
                .quantity(1)
                .matched(false)
                .build());

        // Create burger joint receipt
        Receipt burgerReceipt = Receipt.builder()
                .id(BURGER_RECEIPT_ID)
                .storeName("Burger Palace")
                .date(LocalDateTime.now().minusDays(10))
                .items(burgerItems)
                .totalAmount(34.96) // 9.99*2 + 3.99*2 + 4.50 + 2.50
                .build();

        // Create burger joint bill
        return Bill.builder()
                .id(BURGER_BILL_ID)
                .name("Burger lunch")
                .createdAt(LocalDateTime.now().minusDays(10))
                .receipts(List.of(burgerReceipt))
                .build(); // No share code for this one
    }
}