package nl.bunq.hackathon.adapters.out.bunq;

// import com.bunq.sdk.context.ApiContext;
// import com.bunq.sdk.context.ApiEnvironmentType;
// import com.bunq.sdk.context.BunqContext;
// import com.bunq.sdk.exception.BunqException;
// import com.bunq.sdk.model.generated.endpoint.BunqMeTab;
// import com.bunq.sdk.model.generated.endpoint.BunqMeTabEntry;
// import com.bunq.sdk.model.generated.endpoint.MonetaryAccount;
// import com.bunq.sdk.model.generated.endpoint.User;
// import com.bunq.sdk.model.generated.object.Amount;
// import io.github.cdimascio.dotenv.Dotenv;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Component;
//
// import javax.annotation.PostConstruct;
// import java.io.File;
// import java.util.HashMap;

//@Slf4j
//@Component
public class BunqSdkClient {

    // private static final Dotenv dotenv = Dotenv.load();
    // private static final String API_KEY = dotenv.get("BUNQ_USER_API_KEY");
    // private static final String CONTEXT_FILE_PATH = "bunq-api-context.json";
    // private ApiContext apiContext;
    //
    // @PostConstruct
    // public void initialize() {
    //     try {
    //         File contextFile = new File(CONTEXT_FILE_PATH);
    //
    //         if (contextFile.exists()) {
    //             // Load existing context if available
    //             apiContext = ApiContext.restore(contextFile);
    //             log.info("Loaded existing API context");
    //         } else {
    //             // Create new context if not available
    //             apiContext = ApiContext.create(
    //                 ApiEnvironmentType.SANDBOX,
    //                 API_KEY,
    //                 "Bunq Hackathon App"
    //             );
    //             apiContext.save(contextFile);
    //             log.info("Created new API context");
    //         }
    //
    //         // Load the API context into BunqContext for global access
    //         BunqContext.loadApiContext(apiContext);
    //         log.info("Bunq SDK client initialized successfully");
    //     } catch (Exception e) {
    //         log.error("Failed to initialize Bunq SDK client", e);
    //         throw new RuntimeException("Failed to initialize Bunq SDK client", e);
    //     }
    // }
    //
    // /**
    //  * Generates a bunq.me payment link
    //  *
    //  * @param amount The amount to request
    //  * @param currency The currency code (e.g., "EUR")
    //  * @param description The payment description
    //  * @param redirectUrl The URL to redirect after payment
    //  * @return The bunq.me payment link
    //  */
    // public String generatePayMeLink(String amount, String currency, String description, String redirectUrl) {
    //     try {
    //         // Get the current user
    //         User user = BunqContext.getUserContext().getUser();
    //         log.info("Using user: {}", user.getDisplayName());
    //
    //         // Get the first active monetary account
    //         MonetaryAccount monetaryAccount = MonetaryAccount.list().getValue().stream()
    //             .filter(account -> "ACTIVE".equals(account.getStatus()))
    //             .findFirst()
    //             .orElseThrow(() -> new RuntimeException("No active monetary account found"));
    //
    //         Integer monetaryAccountId = monetaryAccount.getId();
    //         log.info("Using monetary account: {} (ID: {})",
    //                  monetaryAccount.getDescription(), monetaryAccountId);
    //
    //         // Create the bunq.me tab entry
    //         Amount amountObject = new Amount(amount, currency);
    //         HashMap<String, Object> customFields = new HashMap<>();
    //
    //         // Create the bunq.me tab
    //         BunqMeTab bunqMeTab = BunqMeTab.create(
    //             new BunqMeTabEntry(
    //                 amountObject,
    //                 description,
    //                 redirectUrl,
    //                 customFields
    //             ),
    //             monetaryAccountId
    //         ).getValue();
    //
    //         // Get the payment link
    //         String paymentLink = bunqMeTab.getBunqmeTabShareUrl();
    //
    //         log.info("\n=== PAY ME LINK GENERATED ===");
    //         log.info("Description: {}", description);
    //         log.info("Amount: {} {}", amount, currency);
    //         log.info("Status: {}", bunqMeTab.getStatus());
    //         log.info("Created: {}", bunqMeTab.getCreated());
    //         log.info("Expires: {}", bunqMeTab.getTimeExpiry());
    //         log.info("Pay Me Link: {}", paymentLink);
    //         log.info("Share this link with anyone who needs to pay you!");
    //         log.info("===============================");
    //
    //         return paymentLink;
    //     } catch (Exception e) {
    //         log.error("Error generating Pay Me link: {}", e.getMessage(), e);
    //         throw new RuntimeException("Failed to generate Pay Me link", e);
    //     }
    // }
    //
    // /**
    //  * Creates a new session with bunq API
    //  * This can be used to refresh the session if needed
    //  */
    // public void refreshSession() {
    //     try {
    //         apiContext.ensureSessionActive();
    //         apiContext.save(new File(CONTEXT_FILE_PATH));
    //         log.info("Session refreshed successfully");
    //     } catch (Exception e) {
    //         log.error("Failed to refresh session", e);
    //         throw new RuntimeException("Failed to refresh session", e);
    //     }
    // }
    //
    // /**
    //  * Get the current user information
    //  * @return User object with details
    //  */
    // public User getUser() {
    //     return BunqContext.getUserContext().getUser();
    // }
}