package com.example.installassistant.repository;

import com.example.installassistant.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByStatusOrderByUpdatedAtDesc(String status);
}
