package com.hanankhan.ollamaai.ollamaai.controller

import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.model.Media
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@RestController
@RequestMapping("/api/ai/v1")
class ImageDetectionController(
    private val chatModel: OllamaChatModel
) {

    private var prompt = """You are a persona generator and you will generate based on the scenario description 
                in the provided image a persona. Here's an example (in german):
                
                'Ein regionales Krankenhaus führt ein digitales Gesundheitsportal ein. Über dieses Portal sollen 
                Patient:innen künftig ihre Arzttermine online verwalten, medizinische Unterlagen einsehen und mit dem 
                medizinischen Personal kommunizieren können. Die Nutzung ist sowohl von zuhause als auch mobil möglich. 
                Ziel ist es, Abläufe zu vereinfachen und die Patientenzufriedenheit zu erhöhen. 
                Dabei ist zu berücksichtigen, dass Patient:innen sehr unterschiedliche technische Vorkenntnisse, 
                Lebenssituationen und gesundheitliche Bedürfnisse mitbringen. Auch Themen wie Datenschutz, 
                Barrierefreiheit und Vertrauen in digitale Angebote spielen eine zentrale Rolle.'
                
                Your output should be a JSON formatted like so (schema provided):
                
                {
                 "firstname": <generated>,
                 "lastname": <generated>,
                 "age": <generated>,
                 "profession": <generated>,
                  "message" <generated> (what the persona has to say),
                  "behaviours" [<generated behaviours in array>],
                  "goals": [<generated goals in array>],
                  "frustrations": [<generated frustrations in array>]
                }
                
                Here's my scenario:
                """.trimMargin()

    @PostMapping("/generate-text")
    fun generateText(@RequestParam("text") text: String): ResponseEntity<String> {
        val response = this.chatModel.call(
            Prompt(prompt.plus(text), OllamaOptions.builder().model("gemma3").build())
        )

        return ResponseEntity.ok(response.result.output.text)
    }

    @PostMapping("/generate-image")
    fun generate(@RequestParam("image") image: MultipartFile): ResponseEntity<String> {
        return try {
            val media = Media(MediaType.IMAGE_PNG, image.resource)

//            val prompt = """I have provided you an image and you should NOT explain what the image entails.
//                Instead you should parse the content and give it as an output here. Please no additional information.
//                """.trimMargin()
            val userMessage = UserMessage(prompt, listOf(media))

            val response = this.chatModel.call(
                Prompt(
                    userMessage,
                    OllamaOptions.builder().model("llava-llama3").build()
                )
            )

            ResponseEntity.ok(response.result.output.text)
        } catch (ex: IOException) {
            ex.printStackTrace()
            ResponseEntity.status(500).body("Error processing the image: ${ex.message}")
        }
    }
}