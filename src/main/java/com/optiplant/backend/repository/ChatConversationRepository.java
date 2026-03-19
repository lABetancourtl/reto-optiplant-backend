package com.optiplant.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.optiplant.backend.entity.ChatConversation;
import com.optiplant.backend.entity.ChatConversationType;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByTypeAndBranchAIdAndBranchBIsNull(ChatConversationType type, Long branchAId);

    @Query("""
            select c from ChatConversation c
            where c.type = com.optiplant.backend.entity.ChatConversationType.BRANCH_TO_BRANCH
              and ((c.branchA.id = :branchAId and c.branchB.id = :branchBId)
                or (c.branchA.id = :branchBId and c.branchB.id = :branchAId))
            """)
    Optional<ChatConversation> findBranchToBranchConversation(@Param("branchAId") Long branchAId,
                                                              @Param("branchBId") Long branchBId);

    @Query("""
            select c from ChatConversation c
            where c.branchA.id = :branchId or c.branchB.id = :branchId
            order by c.updatedAt desc
            """)
    List<ChatConversation> findByBranchParticipant(@Param("branchId") Long branchId);

    @Query("select c from ChatConversation c order by c.updatedAt desc")
    List<ChatConversation> findAllOrderByUpdatedAtDesc();
}

