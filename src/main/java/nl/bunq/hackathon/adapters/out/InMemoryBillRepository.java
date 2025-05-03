package nl.bunq.hackathon.adapters.out;

import nl.bunq.hackathon.app.model.Bill;
import nl.bunq.hackathon.app.port.out.BillRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the BillRepository interface.
 * Uses a HashMap to store bills for development and testing purposes.
 */
@Repository
public class InMemoryBillRepository implements BillRepository {

    // Using ConcurrentHashMap for thread safety
    private final Map<UUID, Bill> bills = new ConcurrentHashMap<>();
    private final Map<String, UUID> shareCodeToBillId = new ConcurrentHashMap<>();

    @Override
    public List<Bill> findAll() {
        return new ArrayList<>(bills.values());
    }

    @Override
    public Bill save(Bill bill) {
        bills.put(bill.getId(), bill);

        // Update the share code mapping if a share code exists
        if (bill.getShareCode() != null && !bill.getShareCode().isEmpty()) {
            shareCodeToBillId.put(bill.getShareCode(), bill.getId());
        }

        return bill;
    }

    @Override
    public Optional<Bill> findById(UUID id) {
        return Optional.ofNullable(bills.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        Bill bill = bills.remove(id);

        // Remove the share code mapping if it exists
        if (bill != null && bill.getShareCode() != null) {
            shareCodeToBillId.remove(bill.getShareCode());
        }
    }

    @Override
    public Optional<Bill> findByShareCode(String shareCode) {
        UUID billId = shareCodeToBillId.get(shareCode);
        if (billId == null) {
            return Optional.empty();
        }
        return findById(billId);
    }
}