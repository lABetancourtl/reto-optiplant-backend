package com.optiplant.backend.dto;

import java.time.LocalDateTime;

import com.optiplant.backend.entity.ChatConversationType;

public record ChatConversationResponse(
        Long id,
        ChatConversationType type,
        Long branchAId,
        String branchAName,
        Long branchBId,
        String branchBName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ChatMessageResponse lastMessage
) {
}
