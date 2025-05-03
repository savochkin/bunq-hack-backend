package nl.bunq.hackathon.app.port.out;

import nl.bunq.hackathon.app.model.Receipt;
import org.springframework.web.multipart.MultipartFile;

public interface ReceiptParser {
    Receipt parseReceipt(MultipartFile receiptImage);
}