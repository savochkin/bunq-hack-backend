package nl.bunq.hackathon.app.service;

import lombok.RequiredArgsConstructor;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.model.Item;
import nl.bunq.hackathon.app.model.PaymentTab;
import nl.bunq.hackathon.app.model.Receipt;
import nl.bunq.hackathon.app.port.in.BillService;
import nl.bunq.hackathon.app.port.out.BankPort;
import nl.bunq.hackathon.app.port.out.BillRepository;
import nl.bunq.hackathon.app.port.out.OrderMatcher;
import nl.bunq.hackathon.app.port.out.ReceiptParser;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final ReceiptParser receiptParser;
    private final OrderMatcher orderMatcher;
    private final BankPort bankPort;

    private static final String SHARE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHARE_CODE_LENGTH = 8;
    private final Random random = new Random();

    @Override
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    @Override
    public Bill createBill(String name) {
        Bill bill = Bill.builder()
            .id(UUID.randomUUID())
            .name(name)
            .createdAt(LocalDateTime.now())
            .shareCode(generateRandomShareCode())
            .build();

        return billRepository.save(bill);
    }

    private String generateRandomShareCode() {
        StringBuilder shareCode = new StringBuilder(SHARE_CODE_LENGTH);
        for (int i = 0; i < SHARE_CODE_LENGTH; i++) {
            shareCode.append(SHARE_CODE_CHARS.charAt(random.nextInt(SHARE_CODE_CHARS.length())));
        }
        return shareCode.toString();
    }

    @Override
    public Bill getBillById(UUID billId) {
        return billRepository.findById(billId)
            .orElseThrow(() -> new BillNotFoundException("Bill not found with id: " + billId));
    }

    @Override
    public Bill getBillByShareCode(String shareCode) {
        return billRepository.findByShareCode(shareCode)
            .orElseThrow(() -> new InvalidShareCodeException("Invalid share code: " + shareCode));
    }

    @Override
    public void deleteBill(UUID billId) {
        // Check if bill exists first
        getBillById(billId);
        billRepository.deleteById(billId);
    }

    @Override
    public Receipt addReceiptToBill(UUID billId, MultipartFile receiptImage) {
        Bill bill = getBillById(billId);

        // Parse the receipt from the image
        Receipt receipt = receiptParser.parseReceipt(receiptImage);

        // Add the receipt to the bill
        bill.addReceipt(receipt);

        // Save the updated bill
        billRepository.save(bill);

        return receipt;
    }

    @Override
    public String generateShareLink(UUID billId) {
        Bill bill = getBillById(billId);

        // Generate a random share code if one doesn't exist
        if (bill.getShareCode() == null || bill.getShareCode().isEmpty()) {
            String shareCode = generateRandomShareCode();
            bill.setShareCode(shareCode);
            billRepository.save(bill);
        }

        return bill.getShareCode();
    }

    @Override
    public String generatePaymentLink(String shareCode, Double amount) {
        Bill bill = billRepository.findByShareCode(shareCode)
            .orElseThrow(() -> new InvalidShareCodeException("Invalid share code: " + shareCode));

        String description = String.format("You're paying €%.2f for %s, out of a total of €%.2f.",
            amount,
            bill.getName(),
            bill.getTotalAmount());

        PaymentTab paymentTab = bankPort.generatePaymentTab(description, amount);
        bill.addPaymentTab(paymentTab);
        billRepository.save(bill);
        return paymentTab.getPaymentLink();
    }

    @Override
    public List<Item> matchFriendOrder(String shareCode, MultipartFile orderImage) {
        Bill bill = billRepository.findByShareCode(shareCode)
            .orElseThrow(() -> new InvalidShareCodeException("Invalid share code: " + shareCode));

        // Match the order with the bill's items
        List<Item> matchedItems = orderMatcher.matchOrderWithBill(bill, orderImage);

        // Update the bill with matched items
        billRepository.save(bill);

        return matchedItems;
    }

    // Custom exceptions
    public static class BillNotFoundException extends RuntimeException {
        public BillNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidShareCodeException extends RuntimeException {
        public InvalidShareCodeException(String message) {
            super(message);
        }
    }
}