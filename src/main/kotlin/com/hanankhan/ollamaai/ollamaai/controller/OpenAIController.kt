package com.hanankhan.ollamaai.ollamaai.controller

import com.azure.ai.openai.OpenAIClient
import com.azure.ai.openai.OpenAIClientBuilder
import com.azure.ai.openai.models.ChatCompletionsOptions
import com.azure.ai.openai.models.ChatRequestAssistantMessage
import com.azure.ai.openai.models.ChatRequestMessage
import com.azure.ai.openai.models.ChatRequestSystemMessage
import com.azure.ai.openai.models.ChatRequestUserMessage
import com.azure.core.credential.AzureKeyCredential
import jakarta.annotation.PostConstruct
import org.json.JSONObject
import org.springframework.ai.azure.openai.AzureOpenAiChatModel
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/persona")
class OpenAIController(
    @Value("\${spring.ai.azure.openai.chat.options.model}")
    private val deploymentModel: String,

    private val client: OpenAIClient
) {
    private val instructions = """
        You are a persona generator specialized in creating realistic user personas based on provided UX scenario OR UX problem statement descriptions. Your output must strictly follow the provided JSON structure without any additional text before or after the JSON object.

                        Examples can be in any language. Based solely on the scenario/problem statement description provided in as text, generate a comprehensive persona reflecting realistic human characteristics, attitudes, and behaviors.

                        Your response MUST ONLY be JSON formatted exactly as below:

                        ```json
                        {
                          "firstname": "<realistic first name>",
                          "lastname": "<realistic last name>",
                          "age": "<realistic age number>",
                          "profession": "<realistic profession>",
                          "message": "<brief statement (1-2 sentences) in first person that reflects the persona’s thoughts or concerns about the scenario/problem statement>",
                          "behaviours": [
                            "<realistic behavior reflecting how persona interacts with or perceives the described scenario/problem statement>",
                            "..."
                          ],
                          "goals": [
                            "<realistic goal relevant to the scenario>",
                            "..."
                          ],
                          "frustrations": [
                            "<realistic frustration or pain point relevant to the scenario/problem statement>",
                            "..."
                          ],
                          "backgroundColor": <hexacode color like #FFD3E36B. It MUST be only a vibrant flat color from any color range!>
                        }
                        ```
                        Important instructions for persona generation:

                        Ensure that the persona is realistically tailored to the scenario provided in the uploaded image.

                        Consider diverse dimensions such as technical expertise, personal circumstances, accessibility requirements, privacy concerns, and trust towards digital services.
    """.trimIndent()

    private val example = """
        Ein regionales Krankenhaus führt ein digitales Gesundheitsportal ein. 
        Über dieses Portal sollen Patient:innen künftig ihre Arzttermine online verwalten, medizinische Unterlagen einsehen und mit dem medizinischen Personal kommunizieren können. 
        Die Nutzung ist sowohl von zuhause als auch mobil möglich. Ziel ist es, Abläufe zu vereinfachen und die Patientenzufriedenheit zu erhöhen. 
        Dabei ist zu berücksichtigen, dass Patient:innen sehr unterschiedliche technische Vorkenntnisse, Lebenssituationen und gesundheitliche Bedürfnisse mitbringen. 
        Auch Themen wie Datenschutz, Barrierefreiheit und Vertrauen in digitale Angebote spielen eine zentrale Rolle.
    """.trimIndent()

    private val sampleAnswer = """
        {  
          "firstname": "Anna",  
          "lastname": "Müller",  
          "age": 52,  
          "profession": "Grundschullehrerin",  
          "message": "Ich finde es spannend, dass ich meine Arzttermine und Unterlagen jetzt online verwalten kann, aber ich mache mir Sorgen, ob das alles sicher ist und wie einfach es für mich zu bedienen sein wird.",  
          "behaviours": [  
            "Prüft neue digitale Angebote zunächst skeptisch, insbesondere bezüglich Datenschutz.",  
            "Nutzt digitale Geräte wie Laptop und Smartphone regelmäßig, aber bevorzugt einfache und intuitive Anwendungen.",  
            "Fragt bei Unsicherheiten oft ihre Kinder oder jüngere Kolleg:innen um Hilfe.",  
            "Nutzt digitale Angebote vor allem von zuhause aus, da sie mobiles Surfen unterwegs als unpraktisch empfindet."  
          ],  
          "goals": [  
            "Möchte Arzttermine flexibel verwalten können, ohne lange in der Warteschleife einer Praxis zu hängen.",  
            "Wünscht sich eine zentrale und übersichtliche Möglichkeit, ihre medizinischen Unterlagen einzusehen.",  
            "Hofft, durch das Portal die Kommunikation mit Ärzt:innen zu erleichtern und schneller Antworten zu erhalten."  
          ],  
          "frustrations": [  
            "Hat Sorge, dass ihre persönlichen medizinischen Daten nicht ausreichend geschützt sind.",  
            "Empfindet komplizierte Benutzeroberflächen als abschreckend und ist schnell frustriert, wenn etwas nicht funktioniert.",  
            "Befürchtet, dass sie bei technischen Problemen keine schnelle Hilfe bekommt.",  
            "Ist unsicher, ob das Portal auch für ältere oder weniger technikaffine Menschen geeignet ist."  
          ],
          "backgroundColor": "#FFD3E36B" 
        }  
    """.trimIndent()

//    @PostMapping("/chat-old")
//    fun generateText(@RequestParam text: String, @RequestParam("temp") temperature: Double): ResponseEntity<String> {
//        val options = AzureOpenAiChatOptions.builder()
//            .temperature(temperature)
//            .build()
//        val response = this.chatModel.call(
//            Prompt(text, options)
//        )
//
//        return ResponseEntity.ok(response.result.output.text)
//    }

    @PostMapping("/chat")
    fun generate(@RequestParam text: String, @RequestParam("temp") temperature: Double): ResponseEntity<String> {
        val prompts = listOf<ChatRequestMessage>(
            ChatRequestSystemMessage(instructions),
            ChatRequestUserMessage(example),
            ChatRequestAssistantMessage(sampleAnswer),
            ChatRequestUserMessage(text)
        )

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
