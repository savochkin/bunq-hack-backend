package nl.bunq.hackathon.adapters.out;

import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.model.Receipt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;


/**
 * Integration test for the OpenAI-powered receipt parser.
 * This test uses the actual OpenAI API and real receipt image.
 * <p>
 * Uses the test profile with properties from application-test.yaml
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class OpenAiReceiptParserIT {

    @Autowired
    private OpenAiReceiptParser receiptParser;

    @Test
    @DisplayName("Should parse receipt image and extract store name, date, total amount, and items")
    void testParseReceiptImage() throws IOException {
        ClassPathResource resource = new ClassPathResource("samples/receipt.jpg");
        byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());

        MockMultipartFile receiptImage = new MockMultipartFile(
                "receipt",
                "receipt.jpg",
                "image/jpeg",
                fileContent
        );

        Receipt receipt = receiptParser.parseReceipt(receiptImage);


        assertThat(receipt.getStoreName()).isEqualTo("Barista Jun");
        List<Item> items = receipt.getItems();
        assertThat(items)
                .hasSize(5)
                .extracting(Item::getName, Item::getPrice, Item::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple("Tosti Ham Kaas", 7.95, 1),
                        tuple("Slagroom", 0.8, 1),
                        tuple("Appeltaart", 5.95, 1),
                        tuple("San Pellegrino 20cl", 3.15, 1),
                        tuple("Cola 20cl", 3.15, 1)
                );
    }
}
