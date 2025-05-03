package nl.bunq.hackathon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import nl.bunq.hackathon.config.BunqClientProperties;

@SpringBootApplication
@EnableConfigurationProperties(
        BunqClientProperties.class
)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
