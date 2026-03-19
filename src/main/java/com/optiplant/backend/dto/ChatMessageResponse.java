package com.optiplant.backend.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderUsername,
        Long senderBranchId,
        String senderBranchName,
        String content,
        LocalDateTime createdAt
) {
}

