package nl.bunq.hackathon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "bunq")
@Data
public class BunqClientProperties {

    private String userApiKey;


    private String installationToken;

    private Client client = new Client();

    @Data
    public static class Client {

        private String privateKeyPath;

        private String publicKeyPath;
    }
}
