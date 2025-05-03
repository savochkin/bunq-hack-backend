package nl.bunq.hackathon.adapters.in;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.model.Receipt;
import nl.bunq.hackathon.app.port.in.BillService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

/**
 * REST API Controller for handling incoming HTTP requests.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final BillService billService;

    /**
     * Simple health check endpoint.
     *
     * @return A status message
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "API is up and running!";
    }

    /**
     * Get all bills for the current user.
     *
     * @return List of bills
     */
    @GetMapping("/bills")
    public ResponseEntity<List<Bill>> getAllBills() {
        return ResponseEntity.ok(billService.getAllBills());
    }

    /**
     * Create a new empty bill.
     *
     * @param request Request containing bill name
     * @return Created a bill with ID
     */
    @PostMapping("/bills")
    public ResponseEntity<Bill> createBill(@RequestBody CreateBillRequest request) {
        Bill bill = billService.createBill(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }

    /**
     * Get details of a specific bill.
     *
     * @param billId ID of the bill
     * @return Bill details including all items from receipts
     */
    @GetMapping("/bills/{billId}")
    public ResponseEntity<Bill> getBillById(@PathVariable UUID billId) {
        return ResponseEntity.ok(billService.getBillById(billId));
    }

    /**
     * Delete a bill.
     *
     * @param billId ID of the bill to delete
     * @return No content response
     */
    @DeleteMapping("/bills/{billId}")
    public ResponseEntity<Void> deleteBill(@PathVariable UUID billId) {
        billService.deleteBill(billId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Upload a receipt to a bill.
     *
     * @param billId       ID of the bill
     * @param receiptImage Image file of the receipt
     * @return Parsed receipt data
     */
    @PostMapping(value = "/bills/{billId}/receipts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Receipt> uploadReceipt(
        @PathVariable UUID billId,
        @RequestParam("receipt") MultipartFile receiptImage) {
        Receipt receipt = billService.addReceiptToBill(billId, receiptImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    /**
     * Match a friend's order with a shared bill.
     *
     * @param shareCode  Share code of the bill
     * @param orderImage Image of the friend's order
     * @return List of matched items
     */
    @PostMapping(value = "/shared/{shareCode}/match", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Item>> matchFriendOrder(
        @PathVariable String shareCode,
        @RequestParam("order") MultipartFile orderImage) {
        List<Item> matchedItems = billService.matchFriendOrder(shareCode, orderImage);
        return ResponseEntity.ok(matchedItems);
    }

    /**
     * Generate a payment link for a shared bill.
     *
     * @param shareCode Share code of the bill
     * @param amount    Amount to pay
     * @return Payment link
     */
    @GetMapping("/shared/{shareCode}/pay/{amount}")
    public ResponseEntity<PaymentLinkResponse> generatePaymentLink(
        @PathVariable String shareCode,
        @PathVariable Double amount) {
        // Use billService to generate the payment link
        String paymentLink = billService.generatePaymentLink(shareCode, amount);
        return ResponseEntity.ok(new PaymentLinkResponse(paymentLink));
    }

    /**
     * Get a shared bill using its share code.
     *
     * @param shareCode Share code of the bill
     * @return Bill details for the shared bill
     */
    @GetMapping("/shared/{shareCode}")
    public ResponseEntity<Bill> getSharedBill(@PathVariable String shareCode) {
        Bill bill = billService.getBillByShareCode(shareCode);
        return ResponseEntity.ok(bill);
    }

    // Request and response DTOs
    public static class CreateBillRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class ShareLinkResponse {
        private final String shareCode;

        public ShareLinkResponse(String shareCode) {
            this.shareCode = shareCode;
        }

        public String getShareCode() {
            return shareCode;
        }
    }

    public static class PaymentLinkResponse {
        private final String paymentLink;

        public PaymentLinkResponse(String paymentLink) {
            this.paymentLink = paymentLink;
        }

        public String getPaymentLink() {
            return paymentLink;
        }
    }
}

