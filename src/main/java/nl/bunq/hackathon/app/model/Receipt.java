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
public class Receipt {
    private UUID id;
    private String storeName;
    private LocalDateTime date;
    @Builder.Default
    private List<Item> items = new ArrayList<>();
    private double totalAmount;
}
