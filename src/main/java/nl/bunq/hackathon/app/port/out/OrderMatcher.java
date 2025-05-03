package nl.bunq.hackathon.app.port.out;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.model.Item;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OrderMatcher {
    List<Item> matchOrderWithBill(Bill bill, MultipartFile orderImage);
}