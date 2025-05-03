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
import com.openai.models.chat.completions.ChatCompletionContentPart;
import com.openai.models.chat.completions.ChatCompletionContentPartImage;
import com.openai.models.chat.completions.ChatCompletionContentPartText;
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
            String dataUrl = "data:image/jpeg;base64," + b64;

            log.info("Calling OpenAI model {}", openAiConfig.getVisionModel());

            ChatCompletionContentPart textPart =
                    ChatCompletionContentPart.ofText(
                            ChatCompletionContentPartText.builder()
                                    .text(String.format("""
                    Bill items (JSON array):
                    %s

                    Instructions:
                    1. Visually count how many food or drink *instances* you see in the image.
                       Reply first with: {"detected_count": N}
                    2. For each detected instance, match it to one bill item by name or common synonym.
                       Examples: "tosti" → "Tosti Kaas", "coke" → "Cola"
                    3. Build a JSON array of matched IDs, with exactly one entry per instance:
                       ["<item-id-1>", "<item-id-2>", "<item-id-2>"]
                    4. Ensure the length of that array equals your detected_count.
                    5. Return **only** two JSON objects, in order:
                       {
                         "detected_count": N,
                         "matched_ids": [ ... ]
                       }
                    """, itemsJson))
                                    .build()
                    );

            ChatCompletionContentPart imagePart =
                    ChatCompletionContentPart.ofImageUrl(
                            ChatCompletionContentPartImage.builder()
                                    .imageUrl(
                                            ChatCompletionContentPartImage.ImageUrl.builder()
                                                    .url(dataUrl)
                                                    .build()
                                    )
                                    .build()
                    );

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(openAiConfig.getVisionModel())
                    .addSystemMessage("""
                            You are an expert at matching what’s in an image to a given list of bill items.
                         Always return **only** valid JSON in exactly the format described.
                         If unsure, pick the closest match but do not invent items.
                            """)
                    .addUserMessageOfArrayOfContentParts(List.of(textPart, imagePart))
                    .build();

            ChatCompletion response = openAIClient
                    .chat()
                    .completions()
                    .create(params);

            String json = response
                    .choices()
                    .get(0)
                    .message().content()
                    .orElseThrow(() -> new OrderMatchingException("Empty response"));

            log.debug("OpenAI returned: {}", json);

            // Trim any markdown code block delimiters and extra text
            String trimmedJson = json.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            if (!trimmedJson.startsWith("[")) {
                int startIndex = trimmedJson.indexOf('[');
                int endIndex = trimmedJson.lastIndexOf(']');

                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    trimmedJson = trimmedJson.substring(startIndex, endIndex + 1);
                } else {
                    log.error("Could not find a valid JSON array in the response: {}", trimmedJson);
                    throw new OrderMatchingException("Invalid response format from OpenAI");
                }
            }

            log.debug("Parsed JSON: {}", trimmedJson);
            JSONArray matchedItemIds = new JSONArray(trimmedJson);

            log.debug("Available bill items: {}", billItems.stream()
                    .map(item -> String.format("ID: %s, Name: %s", item.getId(), item.getName()))
                    .collect(Collectors.joining(", ")));

            List<Item> matchedItems = new ArrayList<>();
            for (int i = 0; i < matchedItemIds.length(); i++) {
                String itemId = matchedItemIds.getString(i);
                log.debug("Trying to match item ID: {}", itemId);

                billItems.stream()
                        .filter(item -> item.getId().toString().equals(itemId) && !item.isMatched())
                        .findFirst()
                        .ifPresent(item -> {
                            log.debug("Matched item: {}", item.getName());
                            item.setMatched(true);
                            matchedItems.add(item);
                        });
            }

            if (matchedItems.isEmpty()) {
                log.warn("No items matched from the order. This could be because the OpenAI model returned IDs that don't match any item IDs in the bill.");
            } else {
                log.info("Matched {} items from the order: {}", matchedItems.size(),
                        matchedItems.stream().map(Item::getName).collect(Collectors.joining(", ")));
            }
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
