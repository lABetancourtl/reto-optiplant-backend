package com.optiplant.backend.dto;

import com.optiplant.backend.entity.TransferStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTransferStatusRequest(
        @NotNull(message = "El estado es obligatorio")
        TransferStatus status
) {
}
