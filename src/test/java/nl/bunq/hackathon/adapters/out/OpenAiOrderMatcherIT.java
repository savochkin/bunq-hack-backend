package nl.bunq.hackathon.adapters.out;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.model.Receipt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the OpenAI-powered order matcher.
 * This test uses the actual OpenAI API and real order images.
 * <p>
 * Uses the test profile with properties from application-test.yaml
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "openai.api-key=${BUNQ_HACKATHON_OPENAI_API_KEY}"
})
class OpenAiOrderMatcherIT {

    @BeforeAll
    static void setup() {
        String apiKey = System.getenv("BUNQ_HACKATHON_OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("WARNING: BUNQ_HACKATHON_OPENAI_API_KEY environment variable is not set!");
            System.err.println("Integration tests that call the OpenAI API will fail.");
        } else {
            System.out.println("BUNQ_HACKATHON_OPENAI_API_KEY is set with length: " + apiKey.length());
            System.setProperty("openai.api-key", apiKey);
        }
    }

    @Autowired
    private OpenAiOrderMatcher orderMatcher;

    @Test
    @DisplayName("Should match a single sandwich image with the right item in bill")
    void testMatchOrderWithSingleSandwich() throws IOException {
        Bill bill = createBillWithItems();

        ClassPathResource resource = new ClassPathResource("samples/single_sandwich.jpg");
        byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile orderImage = new MockMultipartFile(
                "order",
                "single_sandwich.jpg",
                "image/jpeg",
                fileContent
        );

        List<Item> matchedItems = orderMatcher.matchOrderWithBill(bill, orderImage);

        assertThat(matchedItems).isNotEmpty();

        boolean hasSandwich = matchedItems.stream()
                .anyMatch(item -> item.getName().toLowerCase().contains("sandwich") ||
                        item.getName().toLowerCase().contains("tosti"));

        assertThat(hasSandwich).isTrue();
    }

    @Test
    @DisplayName("Should match a coke image with the right item in bill")
    void testMatchOrderWithCoke() throws IOException {
        Bill bill = createBillWithItems();

        ClassPathResource resource = new ClassPathResource("samples/coke.jpg");
        byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile orderImage = new MockMultipartFile(
                "order",
                "coke.jpg",
                "image/jpeg",
                fileContent
        );

        List<Item> matchedItems = orderMatcher.matchOrderWithBill(bill, orderImage);

        assertThat(matchedItems).isNotEmpty();

        boolean hasCola = matchedItems.stream()
                .anyMatch(item -> item.getName().toLowerCase().contains("cola") ||
                        item.getName().toLowerCase().contains("coke"));

        assertThat(hasCola).isTrue();
    }

    @Test
    @DisplayName("Should match multiple items in one image")
    void testMatchOrderWithMultipleItems() throws IOException {
        Bill bill = createBillWithItems();

        ClassPathResource resource = new ClassPathResource("samples/sandwich_lemonade_pie.jpg");
        byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile orderImage = new MockMultipartFile(
                "order",
                "sandwich_lemonade_pie.jpg",
                "image/jpeg",
                fileContent
        );

        List<Item> matchedItems = orderMatcher.matchOrderWithBill(bill, orderImage);

        assertThat(matchedItems).hasSizeGreaterThanOrEqualTo(2);

        List<String> matchedNames = matchedItems.stream()
                .map(Item::getName)
                .toList();

        assertThat(matchedNames).anyMatch(name -> name.contains("Sandwich") || name.contains("Tosti"));
        assertThat(matchedNames).anyMatch(name -> name.contains("Limonade") || name.contains("Lemonade") ||
                name.contains("Pellegrino") || name.contains("Drink"));
    }

    private Bill createBillWithItems() {
        Receipt receipt = Receipt.builder()
                .id(UUID.randomUUID())
                .date(LocalDateTime.now())
                .storeName("Test Store")
                .items(List.of(
                        Item.builder()
                                .id(UUID.randomUUID())
                                .name("Sandwich Ham Kaas")
                                .price(7.95)
                                .quantity(1)
                                .build(),
                        Item.builder()
                                .id(UUID.randomUUID())
                                .name("Tosti Kaas")
                                .price(6.95)
                                .quantity(1)
                                .build(),
                        Item.builder()
                                .id(UUID.randomUUID())
                                .name("Cola")
                                .price(3.15)
                                .quantity(1)
                                .build(),
                        Item.builder()
                                .id(UUID.randomUUID())
                                .name("San Pellegrino Limonade")
                                .price(3.15)
                                .quantity(1)
                                .build(),
                        Item.builder()
                                .id(UUID.randomUUID())
                                .name("Appeltaart")
                                .price(5.95)
                                .quantity(1)
                                .build()
                ))
                .build();

        Bill bill = Bill.builder()
                .id(UUID.randomUUID())
                .name("Test Bill")
                .createdAt(LocalDateTime.now())
                .build();

        bill.addReceipt(receipt);

        return bill;
    }
}
