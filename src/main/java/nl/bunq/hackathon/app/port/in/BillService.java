package nl.bunq.hackathon.app.port.in;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.model.PaymentTab;
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
    List<Item> matchFriendOrder(String shareCode, MultipartFile orderImage);

    /**
     * Generate a payment link for a shared bill
     *
     * @param shareCode Share code of the bill
     * @param amount Amount to pay
     * @return Payment link URL
     */
    String generatePaymentLink(String shareCode, Double amount);

    /**
     * Retrieves a bill by its share code.
     *
     * @param shareCode The share code of the bill
     * @return The bill associated with the share code
     */
    Bill getBillByShareCode(String shareCode);

    /**
     * Refreshes the payment tabs status for all bills.
     * Checks all tabs that are not in PAID status and updates them if they have been paid.
     */
    void refreshPaymentTabs();

    PaymentTab markTabAsPaid(UUID billId, int tabId);
}