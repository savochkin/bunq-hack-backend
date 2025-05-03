package nl.bunq.hackathon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class OpenAiClientConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model.vision}")
    private String visionModel;

    @Bean
    public OpenAIClient openAIClient() {
        log.error("OpenAI API key loaded: {}…",
                apiKey != null && apiKey.length() > 5
                        ? apiKey.substring(0,5)+"…"
                        : "null");
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public String getVisionModel() {
        return visionModel;
    }
}
