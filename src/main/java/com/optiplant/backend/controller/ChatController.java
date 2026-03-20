package com.optiplant.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optiplant.backend.dto.ChatConversationResponse;
import com.optiplant.backend.dto.ChatMessageResponse;
import com.optiplant.backend.dto.CreateAdminConversationRequest;
import com.optiplant.backend.dto.CreateBranchConversationRequest;
import com.optiplant.backend.dto.CreateChatMessageRequest;
import com.optiplant.backend.service.ChatService;

import jakarta.validation.Valid;

/**
 * Controlador REST para el chat en tiempo real entre ADMIN y sucursales.
 * Expone endpoints para crear conversaciones, enviar mensajes y consultar historiales.
 * Seguridad: Solo ADMIN y SUCURSAL pueden acceder según permisos.
 */
@RestController
@RequestMapping("/chat")
@Validated
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * POST /chat/conversations/admin-branch
     * Crea una conversación entre ADMIN y una sucursal.
     * Body: { branchId }
     * Solo ADMIN puede acceder.
     */
    @PostMapping("/conversations/admin-branch")
    @PreAuthorize("hasRole('ADMIN')")
    public ChatConversationResponse createAdminBranchConversation(@Valid @RequestBody CreateAdminConversationRequest request, Principal principal) {
        return chatService.createAdminConversation(principal.getName(), request.branchId());
    }

    /**
     * POST /chat/conversations/branch-branch
     * Crea conversación entre dos sucursales (o ADMIN).
     * Body: { sourceBranchId, destinationBranchId }
     * ADMIN o SUCURSAL pueden acceder.
     */
    @PostMapping("/conversations/branch-branch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUCURSAL')")
    public ChatConversationResponse createBranchConversation(@Valid @RequestBody CreateBranchConversationRequest request,
                                                             Principal principal) {
        return chatService.createBranchConversation(
                principal.getName(),
                request.sourceBranchId(),
                request.destinationBranchId()
        );
    }

    /**
     * GET /chat/conversations
     * Devuelve todas las conversaciones del usuario autenticado.
     * ADMIN: todas; SUCURSAL: solo las de su sucursal.
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUCURSAL')")
    public List<ChatConversationResponse> getMyConversations(Principal principal) {
        return chatService.getMyConversations(principal.getName());
    }

    /**
     * GET /chat/conversations/{conversationId}/messages
     * Devuelve los mensajes de una conversación.
     * Permite paginación opcional (page, size).
     * ADMIN o SUCURSAL pueden acceder.
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUCURSAL')")
    public List<ChatMessageResponse> getMessages(@PathVariable Long conversationId,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer size,
                                                 Principal principal) {
        return chatService.getConversationMessages(principal.getName(), conversationId, page, size);
    }

    /**
     * POST /chat/messages
     * Envía un mensaje en una conversación.
     * Body: { conversationId, content }
     * ADMIN o SUCURSAL pueden acceder.
     */
    @PostMapping("/messages")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUCURSAL')")
    public ChatMessageResponse sendMessage(@Valid @RequestBody CreateChatMessageRequest request, Principal principal) {
        return chatService.sendMessage(principal.getName(), request);
    }
}
