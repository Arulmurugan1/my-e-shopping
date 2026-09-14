package com.myeshopping.logging.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myeshopping.logging.entity.LogEntry;
import com.myeshopping.logging.repository.LogRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private final LogRepository repository;
    private final ObjectMapper objectMapper;

    public LogController(LogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public Map<String, Object> ingest(@RequestBody Map<String, Object> log) throws JsonProcessingException {
        LogEntry entry = new LogEntry();
        entry.setSource(asString(log.get("source")));
        entry.setLevel(asString(log.get("level")));
        entry.setMessage(asString(log.get("message")));
        entry.setTimestamp(Instant.now());
        entry.setPayload(objectMapper.writeValueAsString(log));
        repository.save(entry);

        Map<String, Object> response = new LinkedHashMap<>(log);
        response.put("id", entry.getId());
        response.put("timestamp", entry.getTimestamp());
        return response;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return repository.findAll().stream().map(this::toMap).collect(Collectors.toList());
    }

    private Map<String, Object> toMap(LogEntry entry) {
        try {
            Map<String, Object> map = entry.getPayload() != null
                    ? objectMapper.readValue(entry.getPayload(), new TypeReference<Map<String, Object>>() {})
                    : new LinkedHashMap<>();
            map.put("id", entry.getId());
            map.put("timestamp", entry.getTimestamp());
            return map;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize stored log payload", ex);
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
