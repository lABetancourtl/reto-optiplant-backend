package com.optiplant.backend.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import com.optiplant.backend.dto.ChatMessageResponse;
import com.optiplant.backend.dto.CreateChatMessageRequest;
import com.optiplant.backend.service.ChatService;

import jakarta.validation.Valid;

@RestController
@Validated
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUCURSAL')")
    public ChatMessageResponse sendMessage(@Valid CreateChatMessageRequest request, Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new RuntimeException("Sesion WebSocket no autenticada");
        }
        return chatService.sendMessage(principal.getName(), request);
    }
}

