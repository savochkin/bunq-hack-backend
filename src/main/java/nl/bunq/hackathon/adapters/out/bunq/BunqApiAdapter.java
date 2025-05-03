package nl.bunq.hackathon.adapters.out.bunq;

import nl.bunq.hackathon.app.model.PaymentTab;
import nl.bunq.hackathon.app.port.out.BankPort;
import org.springframework.stereotype.Component;

/**
 * Adapter for the bunq API
 */
@Component
public class BunqApiAdapter implements BankPort {

    @Override
    public PaymentTab generatePaymentTab(String description, Double amount) {
        String sessionToken = BunqApiClient.createSession();

        // Get user and monetary account information
        int userId = 0;
        int monetaryAccountId = 0;

        try {
            // These methods need to be called to get the userId and monetaryAccountId
            userId = BunqApiClient.fetchUser(sessionToken);
            monetaryAccountId = BunqApiClient.getMonetaryAccountId(sessionToken, userId);

            // Format amount to string with 2 decimal places
            String amountStr = String.format("%.2f", amount);

            // Create bunq.me tab and get the payment link
            int tabId = BunqApiClient.getBunqMeTabId(
                sessionToken,
                amountStr,
                "EUR",
                description,
                "https://bunq.com",
                userId,
                monetaryAccountId
            );

            String paymentLink = BunqApiClient.getPaymeLink(sessionToken, tabId);

            // Create and return the PaymentTab object
            return PaymentTab.builder()
                .tabId(tabId)
                .amount(amountStr)
                .currency("EUR")
                .description(description)
                .redirectUrl("https://bunq.com")
                .userId(userId)
                .monetaryAccountId(monetaryAccountId)
                .paymentLink(paymentLink)
                .status("WAITING_FOR_PAYMENT")
                .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate payment tab: " + e.getMessage(), e);
        }
    }

    public boolean checkPaymentStatus(int tabId) {
        String sessionToken = BunqApiClient.createSession();
        return BunqApiClient.isTabPaid(sessionToken, tabId);
    }
}