package nl.bunq.hackathon.app.port.out;

import nl.bunq.hackathon.app.model.PaymentTab;

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
    PaymentTab generatePaymentTab(String description, Double amount);
}