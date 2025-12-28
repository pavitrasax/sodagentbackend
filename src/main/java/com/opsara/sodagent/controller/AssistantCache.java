package com.opsara.sodagent.controller;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Simple per-user memory manager.
 */
@Service
public class AssistantCache {

    private final ConcurrentMap<String, SODAgentController.Assistant> cache = new ConcurrentHashMap<>();
    private static final DateTimeFormatter KEY_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    /**
     * Returns existing assistant for the key organisationId:userCredentials:DDMMYYYY or creates and stores one using the supplier.
     */
    public SODAgentController.Assistant getOrCreate(String organisationId, String userCredentials, Supplier<SODAgentController.Assistant> supplier) {
        String safeOrg = organisationId != null ? organisationId : "unknownOrg";
        String safeUser = userCredentials != null ? userCredentials : "anonymous";
        String today = LocalDate.now().format(KEY_DATE_FORMAT);
        String key = String.join(":", safeOrg, safeUser, today);

        return cache.computeIfAbsent(key, k -> Objects.requireNonNull(supplier.get()));
    }

    /**
     * Remove cached assistants whose date (last segment in key) is before today.
     * Key format: organisation:user:ddMMyyyy
     */
    public void clearAlltillYesterday() {
        LocalDate today = LocalDate.now();
        Set<String> keys = cache.keySet();
        for (String key : keys) {
            try {
                String[] parts = key.split(":");
                if (parts.length == 0) continue;
                String datePart = parts[parts.length - 1];
                LocalDate entryDate = LocalDate.parse(datePart, KEY_DATE_FORMAT);
                if (entryDate.isBefore(today)) {
                    cache.remove(key);
                }
            } catch (Exception e) {
                // ignore malformed keys and do not remove
            }
        }
    }
}