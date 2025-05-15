package com.hanankhan.ollamaai.ollamaai.service

import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImagePrompt
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ImageGeneratorService(
    private val stabilityAiImageModel: ImageModel,
    @Value("\${image-generator.prompt}") private val profileImagePrompt: String
) {
    fun generateProfileImage(name: String): String? {
        val prompt = profileImagePrompt.replace("{name}", name)
        val response = stabilityAiImageModel.call(
            ImagePrompt(
                prompt,
                StabilityAiImageOptions.builder()
                .stylePreset("photographic")
                .N(1)
                .width(320)
                .height(320)
                .build()
            )
        )

        return response.result.output.b64Json
    }
}