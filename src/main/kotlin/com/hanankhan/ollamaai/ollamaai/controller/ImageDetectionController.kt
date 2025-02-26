package com.hanankhan.ollamaai.ollamaai.controller

import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.model.Media
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@RestController
@RequestMapping("/ai")
class ImageDetectionController(
    private val chatModel: OllamaChatModel
) {

    @PostMapping("/generate")
    fun generate(@RequestParam("image") image: MultipartFile): ResponseEntity<String> {
        return try {
            val media = Media(MediaType.IMAGE_PNG, image.resource)

            val prompt = """I have provided you an image and you should NOT explain what the image entails. 
                Instead you should parse the content and give it as an output here. Please no additional information. 
                You must NOT omit anything in the image even if they appear to be visualized in someway.
                It should be also formatted in json like so:
                {
                    content: 'your output'
                }""".trimMargin()
            val userMessage = UserMessage(prompt, listOf(media))

            val response = this.chatModel.call(
                Prompt(
                    userMessage,
                    OllamaOptions.builder().model("llama3.2-vision").build()
                )
            )

            ResponseEntity.ok(response.result.output.text)
        } catch (ex: IOException) {
            ex.printStackTrace()
            ResponseEntity.status(500).body("Error processing the image: ${ex.message}")
        }
    }
}