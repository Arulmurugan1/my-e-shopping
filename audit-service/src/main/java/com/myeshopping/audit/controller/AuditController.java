package com.myeshopping.audit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.audit.entity.AuditRecord;
import com.myeshopping.audit.repository.AuditRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditRepository repository;
    private final ObjectMapper objectMapper;

    public AuditController(AuditRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public Map<String, Object> record(@RequestBody Map<String, Object> event) throws JsonProcessingException {
        AuditRecord record = new AuditRecord();
        record.setEventType(asString(event.get("eventType")));
        record.setActor(asString(event.get("actor")));
        record.setTimestamp(Instant.now());
        record.setPayload(objectMapper.writeValueAsString(event));
        repository.save(record);

        Map<String, Object> response = new LinkedHashMap<>(event);
        response.put("auditId", record.getId());
        response.put("timestamp", record.getTimestamp());
        return response;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return repository.findAll().stream().map(this::toMap).collect(Collectors.toList());
    }

    private Map<String, Object> toMap(AuditRecord record) {
        try {
            Map<String, Object> map = record.getPayload() != null
                    ? objectMapper.readValue(record.getPayload(), new TypeReference<Map<String, Object>>() {})
                    : new LinkedHashMap<>();
            map.put("auditId", record.getId());
            map.put("timestamp", record.getTimestamp());
            return map;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize stored audit payload", ex);
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
