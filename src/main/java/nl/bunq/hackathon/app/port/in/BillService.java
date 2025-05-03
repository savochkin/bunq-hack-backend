package nl.bunq.hackathon.app.port.in;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.model.Receipt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface BillService {
    List<Bill> getAllBills();
    Bill createBill(String name);
    Bill getBillById(UUID billId);
    void deleteBill(UUID billId);
    Receipt addReceiptToBill(UUID billId, MultipartFile receiptImage);
    String generateShareLink(UUID billId);
    List<Item> matchFriendOrder(String shareCode, MultipartFile orderImage);

    /**
     * Generate a payment link for a shared bill
     *
     * @param shareCode Share code of the bill
     * @param amount Amount to pay
     * @return Payment link URL
     */
    String generatePaymentLink(String shareCode, Double amount);
}