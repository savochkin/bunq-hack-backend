package nl.bunq.hackathon.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private UUID id;
    private String name;
    private double price;
    private int quantity;
    private UUID assignedToUserId;
    private boolean matched;
}