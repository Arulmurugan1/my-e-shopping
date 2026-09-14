package com.myeshopping.logging.repository;

import com.myeshopping.logging.entity.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogRepository extends JpaRepository<LogEntry, UUID> {
}
