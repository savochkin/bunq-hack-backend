package nl.bunq.hackathon.adapters.out.bunq;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import nl.bunq.hackathon.config.BunqClientProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BunqApiClient {
    public static String createSession(BunqClientProperties bunqClientProperties) {
        String payload = "{\"secret\":\"" + bunqClientProperties.getUserApiKey() + "\"}";
        String signature = signPayload(payload, bunqClientProperties.getClient().getPrivateKeyPath());
        String sessionToken = null;

        System.out.println("Using Installation Token: " + bunqClientProperties.getInstallationToken().substring(0, 10) + "...");
        System.out.println("API Key: " + bunqClientProperties.getUserApiKey().substring(0, 10) + "...");

        try {
            String response = Request.post("https://public-api.sandbox.bunq.com/v1/session-server")
                .addHeader("X-Bunq-Client-Authentication", bunqClientProperties.getInstallationToken())
                .addHeader("X-Bunq-Client-Signature", signature)
                .bodyString(payload, org.apache.hc.core5.http.ContentType.APPLICATION_JSON)
                .execute()
                .returnContent()
                .asString();

            System.out.println("Session response: " + response);

            // Extract session token from response
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray responseArray = jsonResponse.getJSONArray("Response");

            // Find the Token object in the response array
            for (int i = 0; i < responseArray.length(); i++) {
                JSONObject item = responseArray.getJSONObject(i);
                if (item.has("Token")) {
                    sessionToken = item.getJSONObject("Token").getString("token");
                    System.out.println("Session token obtained: " + sessionToken.substring(0, 10) + "...");
                    break;
                }
            }

            if (sessionToken == null) {
                throw new RuntimeException("Could not find session token in response");
            }
            return sessionToken;
        } catch (Exception e) {
            System.err.println("Error creating session: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static String generateRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    private static String signPayload(String data, String keyPath) {
        try {
            String pem = new String(Files.readAllBytes(Paths.get(keyPath)))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(keySpec);

            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(data.getBytes());

            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (Exception e) {
            System.err.println("Error signing payload: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String generatePayMeLink(String sessionToken, String amount, String currency, String description, String redirectUrl, String keyPath) {
        if (sessionToken == null) {
            throw new IllegalStateException("No session token available. Call createSession() first.");
        }

        try {
            int userId = fetchUser(sessionToken);
            int monetaryAccountId = getMonetaryAccountId(sessionToken, userId);
            if (monetaryAccountId == 0) {
                throw new RuntimeException("Could not find monetary account ID in response");
            }
            System.out.println("Using Monetary Account ID: " + monetaryAccountId);
            int bunqMeTabId = getBunqMeTabId(sessionToken, amount, currency, description, redirectUrl, userId, monetaryAccountId, keyPath);
            return getPaymeLink(sessionToken, bunqMeTabId);
        } catch (Exception e) {
            System.err.println("Error generating Pay Me link: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static int getBunqMeTabId(String sessionToken, String amount, String currency, String description, String redirectUrl, int userId,
                                      int monetaryAccountId, String keyPath ) throws Exception {
        // Escape special characters in description to prevent JSON formatting issues
        String escapedDescription = description.replace("\"", "\\\"");

        // Create a proper JSON object instead of string concatenation to avoid formatting issues
        JSONObject amountInquired = new JSONObject();
        amountInquired.put("value", amount);
        amountInquired.put("currency", currency);

        JSONObject bunqMeTabEntry = new JSONObject();
        bunqMeTabEntry.put("amount_inquired", amountInquired);
        bunqMeTabEntry.put("description", escapedDescription);
        bunqMeTabEntry.put("redirect_url", redirectUrl);

        JSONObject requestBody = new JSONObject();
        requestBody.put("bunqme_tab_entry", bunqMeTabEntry);
        String payload = requestBody.toString();
        System.out.println("Request payload: " + payload);

        String signature = signPayload(payload, keyPath);
        System.out.println("Generated signature: " + signature.substring(0, 20) + "...");

        int bunqMeTabId1 = 0;
        try {
            // Use a more detailed approach to see the full error response
            HttpPost httpPost = new HttpPost(
                "https://public-api.sandbox.bunq.com/v1/user/" + userId +
                    "/monetary-account/" + monetaryAccountId + "/bunqme-tab");

            // Add all required headers
            httpPost.addHeader("X-Bunq-Client-Authentication", sessionToken);
            httpPost.addHeader("X-Bunq-Client-Signature", signature);
            httpPost.addHeader("X-Bunq-Client-Request-Id", generateRequestId());
            httpPost.addHeader("Cache-Control", "no-cache");
            httpPost.addHeader("X-Bunq-Language", "en_US");
            httpPost.addHeader("X-Bunq-Region", "en_US");
            httpPost.addHeader("X-Bunq-Geolocation", "0 0 0 0 NL");
            httpPost.addHeader("User-Agent", "BunqJavaClient/1.0");
            httpPost.addHeader("Content-Type", "application/json");

            // Set the request body
            httpPost.setEntity(new StringEntity(payload));

            // Execute the request and get the full response
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpPost);

            // Get the response status
            int statusCode = response.getCode();
            System.out.println("Response status code: " + statusCode);

            // Get the response body
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println("Response body: " + responseBody);

            if (statusCode == 200) {
                // Extract the ID from the response
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray responseArray = jsonResponse.getJSONArray("Response");

                for (int i = 0; i < responseArray.length(); i++) {
                    JSONObject item = responseArray.getJSONObject(i);
                    if (item.has("Id")) {
                        bunqMeTabId1 = item.getJSONObject("Id").getInt("id");
                        System.out.println("bunq.me tab created with ID: " + bunqMeTabId1);

                        break;
                    }
                }
            } else {
                System.err.println("Error: Received status code " + statusCode);
                System.err.println("Response body: " + responseBody);
                throw new RuntimeException("Failed to create bunq.me tab: " + responseBody);
            }
        } catch (Exception e) {
            System.err.println("Error in HTTP request: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return bunqMeTabId1;
    }

    public static int getMonetaryAccountId(String sessionToken, int userId) throws IOException {
        String accountsResponse =
            Request.get("https://public-api.sandbox.bunq.com/v1/user/" + userId + "/monetary-account")
                .addHeader("X-Bunq-Client-Authentication", sessionToken)
                .execute()
                .returnContent()
                .asString();

        JSONObject accountsJson = new JSONObject(accountsResponse);
        JSONArray accountsArray = accountsJson.getJSONArray("Response");
        int monetaryAccountId = 0;

        // Find the first monetary account
        for (int i = 0; i < accountsArray.length(); i++) {
            JSONObject accountItem = accountsArray.getJSONObject(i);
            if (accountItem.has("MonetaryAccountBank")) {
                monetaryAccountId = accountItem.getJSONObject("MonetaryAccountBank").getInt("id");
                break;
            }
        }
        return monetaryAccountId;
    }

    public static int fetchUser(String sessionToken) throws IOException {
        // First, get the user ID
        String userResponse = Request.get("https://public-api.sandbox.bunq.com/v1/user")
            .addHeader("X-Bunq-Client-Authentication", sessionToken)
            .execute()
            .returnContent()
            .asString();

        JSONObject userJson = new JSONObject(userResponse);
        JSONArray userArray = userJson.getJSONArray("Response");
        int userId = 0;

        // Find the first UserPerson or UserCompany object
        for (int i = 0; i < userArray.length(); i++) {
            JSONObject item = userArray.getJSONObject(i);
            if (item.has("UserPerson")) {
                userId = item.getJSONObject("UserPerson").getInt("id");
                break;
            } else if (item.has("UserCompany")) {
                userId = item.getJSONObject("UserCompany").getInt("id");
                break;
            }
        }

        if (userId == 0) {
            throw new RuntimeException("Could not find user ID in response");
        }

        System.out.println("Using User ID: " + userId);
        return userId;
    }

    public static String getPaymeLink(String sessionToken, int bunqMeTabId) throws Exception {
        if (sessionToken == null) {
            throw new IllegalStateException("No session token available. Call createSession() first.");
        }

        String paymentLink = null;
        try {
            // First, get the user ID
            String userResponse = Request.get("https://public-api.sandbox.bunq.com/v1/user")
                .addHeader("X-Bunq-Client-Authentication", sessionToken)
                .execute()
                .returnContent()
                .asString();

            JSONObject userJson = new JSONObject(userResponse);
            JSONArray userArray = userJson.getJSONArray("Response");
            int userId = 0;

            // Find the first UserPerson or UserCompany object
            for (int i = 0; i < userArray.length(); i++) {
                JSONObject item = userArray.getJSONObject(i);
                if (item.has("UserPerson")) {
                    userId = item.getJSONObject("UserPerson").getInt("id");
                    break;
                } else if (item.has("UserCompany")) {
                    userId = item.getJSONObject("UserCompany").getInt("id");
                    break;
                }
            }

            if (userId == 0) {
                throw new RuntimeException("Could not find user ID in response");
            }

            // Get the first monetary account ID
            int monetaryAccountId = getMonetaryAccountId(sessionToken, userId);

            if (monetaryAccountId == 0) {
                throw new RuntimeException("Could not find monetary account ID in response");
            }

            // Now get the bunq.me tab details including the payment link
            String tabResponse = Request.get(
                    "https://public-api.sandbox.bunq.com/v1/user/" + userId + "/monetary-account/" + monetaryAccountId + "/bunqme-tab/" + bunqMeTabId)
                .addHeader("X-Bunq-Client-Authentication", sessionToken)
                .execute()
                .returnContent()
                .asString();

            // Parse the response to get the payment link
            JSONObject tabJson = new JSONObject(tabResponse);
            JSONArray tabArray = tabJson.getJSONArray("Response");

            for (int i = 0; i < tabArray.length(); i++) {
                JSONObject item = tabArray.getJSONObject(i);
                if (item.has("BunqMeTab")) {
                    JSONObject bunqMeTab = item.getJSONObject("BunqMeTab");
                    paymentLink = bunqMeTab.getString("bunqme_tab_share_url");
                    String status = bunqMeTab.getString("status");
                    String created = bunqMeTab.getString("created");
                    String expires = bunqMeTab.getString("time_expiry");

                    JSONObject bunqMeTabEntry = bunqMeTab.getJSONObject("bunqme_tab_entry");
                    JSONObject amountInquired = bunqMeTabEntry.getJSONObject("amount_inquired");
                    String amount = amountInquired.getString("value");
                    String currency = amountInquired.getString("currency");
                    String description = bunqMeTabEntry.getString("description");

                    System.out.println("\n=== PAY ME LINK GENERATED ===");
                    System.out.println("Description: " + description);
                    System.out.println("Amount: " + amount + " " + currency);
                    System.out.println("Status: " + status);
                    System.out.println("Created: " + created);
                    System.out.println("Expires: " + expires);
                    System.out.println("Pay Me Link: " + paymentLink);
                    System.out.println("Share this link with anyone who needs to pay you!");
                    System.out.println("===============================");
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving Pay Me link: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return paymentLink;
    }

    public static boolean isTabPaid(String sessionToken, int bunqMeTabId) {
        try {
            // First, get the user ID
            int userId = fetchUser(sessionToken);
            int monetaryAccountId = getMonetaryAccountId(sessionToken, userId);

            // Get the bunq.me tab details
            String tabResponse = Request.get(
                    "https://public-api.sandbox.bunq.com/v1/user/" + userId +
                        "/monetary-account/" + monetaryAccountId +
                        "/bunqme-tab/" + bunqMeTabId)
                .addHeader("X-Bunq-Client-Authentication", sessionToken)
                .execute()
                .returnContent()
                .asString();

            // Parse the response to get the status
            JSONObject tabJson = new JSONObject(tabResponse);
            JSONArray tabArray = tabJson.getJSONArray("Response");

            for (int i = 0; i < tabArray.length(); i++) {
                JSONObject item = tabArray.getJSONObject(i);
                if (item.has("BunqMeTab")) {
                    JSONObject bunqMeTab = item.getJSONObject("BunqMeTab");
                    String status = bunqMeTab.getString("status");

                    // Check if the status indicates payment
                    return "PAID".equals(status);
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error checking tab payment status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

