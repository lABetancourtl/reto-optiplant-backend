package com.optiplant.backend.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransferSchemaMigration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public TransferSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void makeSourceBranchNullableForInboundTransfers() {
        try {
            jdbcTemplate.execute("ALTER TABLE transfers ALTER COLUMN source_branch_id DROP NOT NULL");
            LOGGER.info("Schema update aplicada: transfers.source_branch_id ahora permite null para inbound");
        } catch (Exception ex) {
            // Si ya esta nullable o la BD no permite el cambio en este momento, solo registramos y continuamos.
            LOGGER.debug("No fue necesario aplicar cambio de schema en transfers.source_branch_id: {}", ex.getMessage());
        }
    }
}

