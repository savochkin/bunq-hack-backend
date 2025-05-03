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
        String envApiKey = System.getenv("BUNQ_HACKATHON_OPENAI_API_KEY");
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey == null ? envApiKey : apiKey)
                .build();
    }

    public String getVisionModel() {
        return visionModel;
    }
}
