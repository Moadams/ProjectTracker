package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.enums.EntityType;
import com.buildmaster.projecttracker.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByEntityType(EntityType entityType);
    List<AuditLog> findByActorName(String actorName);
    List<AuditLog> findByEntityTypeAndActorName(EntityType entityType, String actorName);

}
