package nl.bunq.hackathon.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    private UUID id;
    private String name;
    private LocalDateTime createdAt;
    @Builder.Default
    private List<Receipt> receipts = new ArrayList<>();
    private String shareCode;

    public void addReceipt(Receipt receipt) {
        if (receipts == null) {
            receipts = new ArrayList<>();
        }
        receipts.add(receipt);
    }

    public List<Item> getAllItems() {
        if (receipts == null) {
            return List.of();
        }

        List<Item> allItems = new ArrayList<>();
        for (Receipt receipt : receipts) {
            allItems.addAll(receipt.getItems());
        }
        return allItems;
    }

    public double getTotalAmount() {
        if (receipts == null) {
            return 0.0;
        }

        double total = 0.0;
        for (Receipt receipt : receipts) {
            for (Item item : receipt.getItems()) {
                total += item.getPrice();
            }
        }
        return total;
    }
}
