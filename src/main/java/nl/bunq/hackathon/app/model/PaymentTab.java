package nl.bunq.hackathon.app.model;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Builder
@RequiredArgsConstructor
public class PaymentTab {
    private int tabId;
    private String amount;
    private String currency;
    private String description;
    private String redirectUrl;
    private int userId;
    private int monetaryAccountId;
    private String paymentLink;
    private String status;
}