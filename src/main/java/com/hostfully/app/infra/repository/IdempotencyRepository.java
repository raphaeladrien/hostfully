package com.hostfully.app.infra.repository;

import com.hostfully.app.infra.entity.Idempotency;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<Idempotency, UUID> {}
