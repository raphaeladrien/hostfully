package com.hostfully.app.infra.repository;

import com.hostfully.app.infra.entity.PropertyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity, Long> {
    Optional<PropertyEntity> findByExternalId(String externalId);
}
