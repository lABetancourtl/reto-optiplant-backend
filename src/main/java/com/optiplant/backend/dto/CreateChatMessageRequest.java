package com.optiplant.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChatMessageRequest(
        @NotNull(message = "La conversacion es obligatoria")
        Long conversationId,

        @NotBlank(message = "El contenido del mensaje es obligatorio")
        String content
) {
}

