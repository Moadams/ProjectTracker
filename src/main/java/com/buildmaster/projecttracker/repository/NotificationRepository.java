package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipientEmailOrderByTimestampDesc(String recipientEmail);
    List<Notification> findByRecipientEmailAndReadFalseOrderByTimestampDesc(String recipientEmail);

    Optional<Notification> findByIdAndRecipientEmail(String notificationId, String currentUserEmail);

    boolean existsByIdAndRecipientEmail(String notificationId, String currentUserEmail);
}
