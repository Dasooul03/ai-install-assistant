package com.example.installassistant.repository;

import com.example.installassistant.model.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    List<OperationLog> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
}
