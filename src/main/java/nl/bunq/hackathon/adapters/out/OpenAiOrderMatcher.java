package nl.bunq.hackathon.adapters.out;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.port.out.OrderMatcher;
import nl.bunq.hackathon.config.OpenAiClientConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiOrderMatcher implements OrderMatcher {

    private final OpenAIClient openAIClient;
    private final OpenAiClientConfig openAiConfig;

    @Override
    public List<Item> matchOrderWithBill(Bill bill, MultipartFile orderImage) {
        try {
            List<Item> billItems = bill.getAllItems();

            if (billItems.isEmpty()) {
                log.warn("Bill has no items to match against");
                return List.of();
            }

            String itemsJson = billItems.stream()
                    .map(item -> String.format(
                            """
                                    { "id": "%s", "name": "%s", "price": %.2f, "quantity": %d }""",
                            item.getId(), item.getName(), item.getPrice(), item.getQuantity()))
                    .collect(Collectors.joining(",\n", "[\n", "\n]"));

            String b64 = Base64.getEncoder().encodeToString(orderImage.getBytes());

            String prompt = String.format("""
                    Here is an order image in base64:
                    data:image/jpeg;base64,%s
                    
                    Match this order with the available items:
                    %s
                    
                    Instructions:
                    1. Look at each item in the order image
                    2. Find matching items in the list above
                    3. Return a JSON array with the IDs of matching items
                    4. If an item appears multiple times, include its ID multiple times
                    5. Ignore items that don't match anything in the list
                    
                    Return *only* a JSON array of IDs like this:
                    ["item-id-1", "item-id-2"]
                    """, b64, itemsJson);

            log.info("Calling OpenAI model {}", openAiConfig.getVisionModel());

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(openAiConfig.getVisionModel())
                    .addUserMessage(prompt)
                    .build();

            ChatCompletion response = openAIClient
                    .chat()
                    .completions()
                    .create(params);

            String json = response
                    .choices().get(0)
                    .message().content()
                    .orElseThrow(() -> new OrderMatchingException("Empty response"));

            log.debug("OpenAI returned: {}", json);

            JSONArray matchedItemIds = new JSONArray(json);

            List<Item> matchedItems = new ArrayList<>();
            for (int i = 0; i < matchedItemIds.length(); i++) {
                String itemId = matchedItemIds.getString(i);

                billItems.stream()
                        .filter(item -> item.getId().toString().equals(itemId) && !item.isMatched())
                        .findFirst()
                        .ifPresent(item -> {
                            item.setMatched(true);
                            matchedItems.add(item);
                        });
            }

            log.info("Matched {} items from the order", matchedItems.size());
            return matchedItems;

        } catch (IOException e) {
            log.error("Failed to read order image", e);
            throw new OrderMatchingException("Failed to read order image", e);
        } catch (Exception e) {
            log.error("Error while matching order with bill", e);
            throw new OrderMatchingException("Error while matching order with bill", e);
        }
    }

    public static class OrderMatchingException extends RuntimeException {
        public OrderMatchingException(String message) {
            super(message);
        }

        public OrderMatchingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
