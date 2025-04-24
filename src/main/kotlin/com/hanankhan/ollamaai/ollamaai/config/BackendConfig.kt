package com.hanankhan.ollamaai.ollamaai.config

import com.azure.ai.openai.OpenAIClient
import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.core.credential.AzureKeyCredential
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader


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

    @Bean
    fun personaInstructions(resourceLoader: ResourceLoader): String {
        return getResource(resourceLoader,"classpath:prompts/instructions.txt")
    }

    @Bean
    fun exampleText(resourceLoader: ResourceLoader): String {
        return getResource(resourceLoader, "classpath:prompts/example.txt")
    }

    @Bean
    fun exampleImage(resourceLoader: ResourceLoader): String {
        return getResource(resourceLoader, "classpath:prompts/exampleImage.txt")
    }

    @Bean
    fun sampleAnswer(resourceLoader: ResourceLoader): String {
        return getResource(resourceLoader, "classpath:prompts/sampleAnswer.txt")
    }

    private fun getResource(resourceLoader: ResourceLoader, location: String): String {
        val res = resourceLoader.getResource(location)
        return res.inputStream.bufferedReader().use { it.readText() }
    }
}