package com.hanankhan.ollamaai.ollamaai.config

import com.azure.ai.openai.OpenAIClient
import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.core.credential.AzureKeyCredential
import com.azure.core.credential.TokenCredential
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class BackendConfig(
    @Value("\${spring.ai.azure.openai.endpoint}")
    private val endpoint: String,
) {

    @Value("\${API_KEY}")
    private lateinit var apiKey: String


    @Bean
    fun chatModel(): OpenAIClient {
        return OpenAIClientBuilder()
            .credential(AzureKeyCredential(apiKey))
            .endpoint(endpoint)
            .buildClient()
    }
}