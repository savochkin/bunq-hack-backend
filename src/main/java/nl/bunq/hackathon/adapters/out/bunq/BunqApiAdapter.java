package nl.bunq.hackathon.adapters.out.bunq;

import nl.bunq.hackathon.app.port.out.BankPort;
import org.springframework.stereotype.Component;

/**
 * Adapter for the bunq API
 */
@Component
public class BunqApiAdapter implements BankPort {

    @Override
    public String generatePaymentLink(String description, Double amount) {
        String sessionToken = BunqApiClient.createSession();
        String payMeLink = BunqApiClient.generatePayMeLink(sessionToken, String.valueOf(amount), "EUR", description, "https://bunq.com");
        return payMeLink;
    }
}