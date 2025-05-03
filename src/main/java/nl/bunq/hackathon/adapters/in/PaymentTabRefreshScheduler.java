package nl.bunq.hackathon.adapters.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.bunq.hackathon.app.port.in.BillService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTabRefreshScheduler {

    private final BillService billService;

    /**
     * Scheduled task that runs every 10 seconds to refresh payment tab statuses.
     * This checks all bills for payment tabs that need status updates.
     */
    @Scheduled(fixedRate = 10000) // 10 seconds in milliseconds
    public void refreshPaymentTabs() {
        log.info("Starting scheduled payment tab refresh");
        try {
            billService.refreshPaymentTabs();
            log.info("Completed payment tab refresh");
        } catch (Exception e) {
            log.error("Error during payment tab refresh: {}", e.getMessage(), e);
        }
    }
}