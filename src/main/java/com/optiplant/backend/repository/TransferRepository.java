package com.optiplant.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.optiplant.backend.entity.Branch;
import com.optiplant.backend.entity.Transfer;
import com.optiplant.backend.entity.TransferStatus;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findBySourceBranch(Branch sourceBranch);

    List<Transfer> findByDestBranch(Branch destBranch);

    List<Transfer> findByStatus(TransferStatus status);

    List<Transfer> findByRequestedById(Long userId);

    Optional<Transfer> findByTrackingCode(String trackingCode);
}
