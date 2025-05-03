package nl.bunq.hackathon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties("bunq.client")
@Data
public class BunqClientProperties {
    private String privateKeyPath;
    private String publicKeyPath;
}
