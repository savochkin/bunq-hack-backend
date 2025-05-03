package nl.bunq.hackathon.app.port.out;

/**
 * Port for banking operations
 */
public interface BankPort {

    /**
     * Generate a payment link for a given amount
     *
     * @param description Description for the payment
     * @param amount Amount to be paid
     * @return Payment link URL
     */
    String generatePaymentLink(String description, Double amount);
}