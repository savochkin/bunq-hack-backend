package nl.bunq.hackathon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class OpenAiClientConfig {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("BUNQ_HACKATHON_OPENAI_API_KEY");

    @Value("${openai.model.vision}")
    private String visionModel;

    @Bean
    public OpenAIClient openAIClient() {
        log.info("Initializing OpenAI client with API key: {}...",
                API_KEY != null ? API_KEY.substring(0, Math.min(5, API_KEY.length())) + "..." : "null");
        return OpenAIOkHttpClient.builder()
                .apiKey(API_KEY)
                .build();
    }
    
    public String getVisionModel() {
        return visionModel;
    }
}