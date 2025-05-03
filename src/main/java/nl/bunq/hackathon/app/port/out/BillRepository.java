package nl.bunq.hackathon.app.port.out;

import nl.bunq.hackathon.app.model.Bill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository {
    List<Bill> findAll();
    Bill save(Bill bill);
    Optional<Bill> findById(UUID id);
    void deleteById(UUID id);
    Optional<Bill> findByShareCode(String shareCode);
}