package com.hanankhan.ollamaai.ollamaai.controller

import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.Base64

@RestController
@RequestMapping("/ai")
class ImageDetectionController(
    private val chatModel: OllamaChatModel
) {

    @PostMapping("/generate")
    fun generate(@RequestParam("image") image: MultipartFile): ResponseEntity<String> {
        return try {
            val base64Image = Base64.getEncoder().encodeToString(image.bytes)

            val prompt = "Extract text from this image carefully!"
            val fullPrompt = "$prompt\n\n![image](data:image/png;base64,$base64Image)"

            val response = this.chatModel.call(
                Prompt(
                    fullPrompt,
                    OllamaOptions.builder().model("deepseek-r1:latest").build()
                )
            )

            return ResponseEntity.ok(response.result.output.text)
        } catch (ex: IOException) {
            ex.printStackTrace()
            ResponseEntity.status(500).body("Error processing the image: ${ex.message}")
        }
    }
}