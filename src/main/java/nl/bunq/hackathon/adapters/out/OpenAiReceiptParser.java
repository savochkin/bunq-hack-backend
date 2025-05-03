package nl.bunq.hackathon.adapters.out;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionContentPart;
import com.openai.models.chat.completions.ChatCompletionContentPartImage;
import com.openai.models.chat.completions.ChatCompletionContentPartText;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.model.Receipt;
import nl.bunq.hackathon.app.port.out.ReceiptParser;
import nl.bunq.hackathon.config.OpenAiClientConfig;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiReceiptParser implements ReceiptParser {

    private final OpenAIClient openAIClient;
    private final OpenAiClientConfig openAiConfig;

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    @Override
    public Receipt parseReceipt(MultipartFile receiptImage) {
        try {
            String b64 = Base64.getEncoder().encodeToString(receiptImage.getBytes());
            String dataUrl = "data:image/jpeg;base64," + b64;

            log.info("Calling OpenAI model {}", openAiConfig.getVisionModel());

            ChatCompletionContentPart textPart = ChatCompletionContentPart.ofText(
                    ChatCompletionContentPartText.builder()
                            .text("""
                                     You are a precise receipt parser. Extract:
                                              1. Store name
                                              2. Date of purchase
                                              3. Total amount
                                              4. All items (name, price, quantity)
                                    
                                            Return only this JSON:
                                            {
                                              "storeName": "...",
                                              "date": "YYYY-MM-DD HH:MM:SS",
                                              "totalAmount": 0.0,
                                              "items": [
                                                { "name": "...", "price": 0.0, "quantity": 0 }
                                              ]
                                            }
                                    """)
                            .build()
            );

            ChatCompletionContentPart imagePart = ChatCompletionContentPart.ofImageUrl(
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
                            You are a precise receipt parser.
                            Extract storeName, date, totalAmount, and all items.
                            Return only valid JSON.
                            """)
                    .addUserMessageOfArrayOfContentParts(List.of(textPart, imagePart))
                    .build();

            ChatCompletion response = openAIClient
                    .chat()
                    .completions()
                    .create(params);

            String json = response
                    .choices().get(0)
                    .message().content()
                    .orElseThrow(() -> new ReceiptParsingException("Empty response"));

            log.debug("OpenAI returned JSON: {}", json);
            String trimMarkdown = json.replaceAll("```json", "").replaceAll("```", "").trim();

            JSONObject root = new JSONObject(trimMarkdown);
            String storeName = root.getString("storeName");
            LocalDateTime date = parseDate(root.getString("date"));
            double total = root.getDouble("totalAmount");

            List<Item> items = new ArrayList<>();
            JSONArray arr = root.getJSONArray("items");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                items.add(Item.builder()
                        .id(UUID.randomUUID())
                        .name(o.getString("name"))
                        .price(o.getDouble("price"))
                        .quantity(o.getInt("quantity"))
                        .matched(false)
                        .build());
            }

            return Receipt.builder()
                    .id(UUID.randomUUID())
                    .storeName(storeName)
                    .date(date)
                    .totalAmount(total)
                    .items(items)
                    .build();

        } catch (IOException e) {
            log.error("Failed to read receipt image", e);
            throw new ReceiptParsingException("Failed to read receipt image", e);
        } catch (JSONException e) {
            log.error("Failed to parse JSON from OpenAI", e);
            throw new ReceiptParsingException("Failed to parse JSON from OpenAI", e);
        }
    }

    private LocalDateTime parseDate(String s) {
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(s, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        log.warn("Could not parse date “{}”, defaulting to now", s);
        return LocalDateTime.now();
    }

    public static class ReceiptParsingException extends RuntimeException {
        public ReceiptParsingException(String msg, Throwable cause) {
            super(msg, cause);
        }

        public ReceiptParsingException(String msg) {
            super(msg);
        }
    }
}
