package nl.bunq.hackathon.adapters.out.bunq;

import nl.bunq.hackathon.app.model.PaymentTab;
import nl.bunq.hackathon.app.port.out.BankPort;
import nl.bunq.hackathon.config.BunqClientProperties;

import org.springframework.stereotype.Component;

import java.util.Locale;

import lombok.RequiredArgsConstructor;

/**
 * Adapter for the bunq API
 */
@Component
@RequiredArgsConstructor
public class BunqApiAdapter implements BankPort {

    private final BunqClientProperties bunqClientProperties;

    @Override
    public PaymentTab generatePaymentTab(String description, Double amount) {
        String sessionToken = BunqApiClient.createSession(bunqClientProperties.getPrivateKeyPath());

        // Get user and monetary account information
        int userId = 0;
        int monetaryAccountId = 0;

        try {
            // These methods need to be called to get the userId and monetaryAccountId
            userId = BunqApiClient.fetchUser(sessionToken);
            monetaryAccountId = BunqApiClient.getMonetaryAccountId(sessionToken, userId);

            // Format amount to string with 2 decimal places
            String amountStr = String.format(Locale.US, "%.2f", amount);

            // Create bunq.me tab and get the payment link
            int tabId = BunqApiClient.getBunqMeTabId(
                    sessionToken,
                    amountStr,
                    "EUR",
                    description,
                    "https://bunq.com",
                    userId,
                    monetaryAccountId,
                    bunqClientProperties.getPrivateKeyPath()
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

    @Override
    public PaymentTab getPaymentTab(int tabId) {
        String sessionToken = BunqApiClient.createSession(bunqClientProperties.getPrivateKeyPath());

        try {
            // Get user and monetary account information
            int userId = BunqApiClient.fetchUser(sessionToken);
            int monetaryAccountId = BunqApiClient.getMonetaryAccountId(sessionToken, userId);

            // Check if the tab is paid
            boolean isPaid = BunqApiClient.isTabPaid(sessionToken, tabId);

            // Get the tab details - this would need to be implemented in BunqApiClient
            // For now, we'll create a basic PaymentTab with the status based on isPaid

            return PaymentTab.builder()
                    .tabId(tabId)
                    .userId(userId)
                    .monetaryAccountId(monetaryAccountId)
                    .status(isPaid ? "PAID" : "WAITING_FOR_PAYMENT")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get payment tab: " + e.getMessage(), e);
        }
    }
}
