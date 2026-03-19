package com.optiplant.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.optiplant.backend.dto.ChatConversationResponse;
import com.optiplant.backend.dto.ChatMessageResponse;
import com.optiplant.backend.dto.CreateChatMessageRequest;
import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.entity.ChatConversation;
import com.optiplant.backend.entity.ChatConversationType;
import com.optiplant.backend.entity.ChatMessage;
import com.optiplant.backend.entity.User;
import com.optiplant.backend.repository.BranchRepository;
import com.optiplant.backend.repository.ChatConversationRepository;
import com.optiplant.backend.repository.ChatMessageRepository;
import com.optiplant.backend.repository.UserRepository;

@Service
public class ChatService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 50;

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatConversationRepository conversationRepository,
                       ChatMessageRepository messageRepository,
                       UserRepository userRepository,
                       BranchRepository branchRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public ChatConversationResponse createAdminConversation(String username, Long branchId) {
        User requester = getUserByUsername(username);
        ensureAdmin(requester);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Sucursal destino no encontrada"));

        ChatConversation conversation = conversationRepository
                .findByTypeAndBranchAIdAndBranchBIsNull(ChatConversationType.ADMIN_BRANCH, branch.getId())
                .orElseGet(() -> {
                    ChatConversation created = new ChatConversation();
                    created.setType(ChatConversationType.ADMIN_BRANCH);
                    created.setBranchA(branch);
                    created.setBranchB(null);
                    created.setCreatedBy(requester);
                    return conversationRepository.save(created);
                });

        return toConversationResponse(conversation);
    }

    @Transactional
    public ChatConversationResponse createBranchConversation(String username, Long sourceBranchId, Long destinationBranchId) {
        User requester = getUserByUsername(username);

        Long fromBranchId;
        if (isAdmin(requester)) {
            if (sourceBranchId == null) {
                throw new RuntimeException("sourceBranchId es obligatorio para ADMIN");
            }
            fromBranchId = sourceBranchId;
        } else if (isSucursal(requester)) {
            if (requester.getBranch() == null) {
                throw new RuntimeException("El usuario sucursal no tiene sucursal asignada");
            }
            fromBranchId = requester.getBranch().getId();
        } else {
            throw new RuntimeException("Rol no autorizado para crear conversaciones entre sucursales");
        }

        if (destinationBranchId == null) {
            throw new RuntimeException("La sucursal destino es obligatoria");
        }
        if (fromBranchId.equals(destinationBranchId)) {
            throw new RuntimeException("No puedes crear un chat con la misma sucursal");
        }

        Branch source = branchRepository.findById(fromBranchId)
                .orElseThrow(() -> new RuntimeException("Sucursal origen no encontrada"));
        Branch destination = branchRepository.findById(destinationBranchId)
                .orElseThrow(() -> new RuntimeException("Sucursal destino no encontrada"));

        ChatConversation conversation = conversationRepository
                .findBranchToBranchConversation(source.getId(), destination.getId())
                .orElseGet(() -> {
                    ChatConversation created = new ChatConversation();
                    created.setType(ChatConversationType.BRANCH_TO_BRANCH);
                    created.setBranchA(source);
                    created.setBranchB(destination);
                    created.setCreatedBy(requester);
                    return conversationRepository.save(created);
                });

        if (!canAccessConversation(requester, conversation)) {
            throw new RuntimeException("No tienes permisos para esta conversacion");
        }

        return toConversationResponse(conversation);
    }

    @Transactional(readOnly = true)
    public List<ChatConversationResponse> getMyConversations(String username) {
        User requester = getUserByUsername(username);

        List<ChatConversation> conversations;
        if (isAdmin(requester)) {
            conversations = conversationRepository.findAllOrderByUpdatedAtDesc();
        } else if (isSucursal(requester)) {
            if (requester.getBranch() == null) {
                throw new RuntimeException("El usuario sucursal no tiene sucursal asignada");
            }
            conversations = conversationRepository.findByBranchParticipant(requester.getBranch().getId());
        } else {
            throw new RuntimeException("Rol no autorizado para consultar conversaciones");
        }

        return conversations.stream()
                .map(this::toConversationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversationMessages(String username,
                                                             Long conversationId,
                                                             Integer page,
                                                             Integer size) {
        User requester = getUserByUsername(username);
        ChatConversation conversation = getConversation(conversationId);
        ensureConversationAccess(requester, conversation);

        Pageable pageable = PageRequest.of(
                page == null || page < 0 ? DEFAULT_PAGE : page,
                normalizePageSize(size)
        );

        Page<ChatMessage> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);

        return messages.getContent().stream().map(this::toMessageResponse).toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(String username, CreateChatMessageRequest request) {
        User requester = getUserByUsername(username);
        ChatConversation conversation = getConversation(request.conversationId());
        ensureConversationAccess(requester, conversation);

        String content = request.content() == null ? "" : request.content().trim();
        if (content.isEmpty()) {
            throw new RuntimeException("El contenido del mensaje es obligatorio");
        }

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(requester);
        message.setContent(content);
        ChatMessage saved = messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatMessageResponse response = toMessageResponse(saved);
        messagingTemplate.convertAndSend("/topic/chat/conversation/" + conversation.getId(), response);
        return response;
    }

    private int normalizePageSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, 200);
    }

    private ChatConversation getConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversacion no encontrada"));
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private void ensureConversationAccess(User requester, ChatConversation conversation) {
        if (!canAccessConversation(requester, conversation)) {
            throw new RuntimeException("No tienes permisos para esta conversacion");
        }
    }

    private boolean canAccessConversation(User requester, ChatConversation conversation) {
        if (isAdmin(requester)) {
            return true;
        }

        if (!isSucursal(requester) || requester.getBranch() == null) {
            return false;
        }

        Long branchId = requester.getBranch().getId();
        Long branchAId = conversation.getBranchA() != null ? conversation.getBranchA().getId() : null;
        Long branchBId = conversation.getBranchB() != null ? conversation.getBranchB().getId() : null;

        return branchId.equals(branchAId) || (branchBId != null && branchId.equals(branchBId));
    }

    private void ensureAdmin(User user) {
        if (!isAdmin(user)) {
            throw new RuntimeException("Solo ADMIN puede ejecutar esta operacion");
        }
    }

    private boolean isAdmin(User user) {
        return hasRole(user, "ADMIN");
    }

    private boolean isSucursal(User user) {
        return hasRole(user, "SUCURSAL");
    }

    private boolean hasRole(User user, String expected) {
        if (user.getRole() == null) {
            return false;
        }

        String normalized = user.getRole().trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return expected.equals(normalized);
    }

    private ChatConversationResponse toConversationResponse(ChatConversation conversation) {
        ChatMessageResponse lastMessage = messageRepository
                .findTopByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .map(this::toMessageResponse)
                .orElse(null);

        return new ChatConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getBranchA() != null ? conversation.getBranchA().getId() : null,
                conversation.getBranchA() != null ? conversation.getBranchA().getName() : null,
                conversation.getBranchB() != null ? conversation.getBranchB().getId() : null,
                conversation.getBranchB() != null ? conversation.getBranchB().getName() : null,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                lastMessage
        );
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        User sender = message.getSender();
        Long senderBranchId = sender.getBranch() != null ? sender.getBranch().getId() : null;
        String senderBranchName = sender.getBranch() != null ? sender.getBranch().getName() : null;

        return new ChatMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                sender.getId(),
                sender.getUsername(),
                senderBranchId,
                senderBranchName,
                message.getContent(),
                message.getCreatedAt()
        );
    }
}

