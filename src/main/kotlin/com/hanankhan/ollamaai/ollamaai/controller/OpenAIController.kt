package com.hanankhan.ollamaai.ollamaai.controller

import com.azure.ai.openai.OpenAIClient
import com.azure.ai.openai.models.*
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("api/v1/persona")
class OpenAIController(
    private val client: OpenAIClient,
    @Value("\${spring.ai.azure.openai.chat.options.model}")
    private val deploymentModel: String,
    private val personaInstructions: String,
    private val exampleText: String,
    private val exampleImage: String,
    private val sampleAnswer: String
) {
    @PostMapping("/chat")
    fun generate(@RequestParam text: String, @RequestParam("temp") temperature: Double = .7): ResponseEntity<String> {
        val prompts = listOf<ChatRequestMessage>(
            ChatRequestSystemMessage(personaInstructions),
            ChatRequestUserMessage(exampleText),
            ChatRequestAssistantMessage(sampleAnswer),
            ChatRequestUserMessage(text)
        )

        return responseEntityWithChatCompletion(prompts, temperature)
    }

    @PostMapping(path = ["/vision"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun generateWithImage(
        @RequestPart("image") image: MultipartFile,
        @RequestParam("temp") temperature: Double = .7
    ): ResponseEntity<String> {
        val bytes = image.bytes
        val base64 = Base64.getEncoder().encodeToString(bytes)
        val dataUrl = "data:${image.contentType};base64,$base64"

        val textItem = ChatMessageTextContentItem(personaInstructions)
        val imageItem = ChatMessageImageContentItem(ChatMessageImageUrl(dataUrl))

        val userMessage = ChatRequestUserMessage(listOf(textItem, imageItem))
        val prompts = listOf<ChatRequestMessage>(userMessage)

        return responseEntityWithChatCompletion(prompts, temperature)
    }

    private fun responseEntityWithChatCompletion(
        prompts: List<ChatRequestMessage>,
        temperature: Double
    ): ResponseEntity<String> {
        val options = ChatCompletionsOptions(prompts)
            .setMaxTokens(800)
            .setTemperature(temperature)
            .setTopP(0.95)
            .setFrequencyPenalty(0.0)
            .setPresencePenalty(0.0)
            .setStop(null)

        return try {
            val chatCompletions = client.getChatCompletions(deploymentModel, options)

            val parsed = extractMessageContent(chatCompletions.toJsonString())
            val cleaned = parsed.content.removePrefix("```json").removeSuffix("```").trim()

            ResponseEntity.ok(cleaned)
        } catch (e: Exception) {
            println("Error: ${e.message}")
            ResponseEntity.internalServerError().body(e.message)
        }
    }
}

data class ChatResponseMessage(val content: String)

fun extractMessageContent(jsonResponse: String): ChatResponseMessage {
    val json = JSONObject(jsonResponse)
    val messageContent = json
        .getJSONArray("choices")
        .getJSONObject(0)
        .getJSONObject("message")
        .getString("content")

    return ChatResponseMessage(messageContent)
}
