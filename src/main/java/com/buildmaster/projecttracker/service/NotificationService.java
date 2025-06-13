package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.exception.ResourceNotFoundException;
import com.buildmaster.projecttracker.model.Notification;
import com.buildmaster.projecttracker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    /**
     * Creates and saves a new notification.
     *
     * @param recipientEmail The email of the user to notify.
     * @param message The content of the notification.
     * @param type The type of notification (e.g., TASK_ASSIGNMENT).
     * @param entityId The ID of the related entity (optional).
     * @param entityType The type of the related entity (optional).
     * @return The created Notification object.
     */
    public Notification createNotification(String recipientEmail, String message, String type, Long entityId, String entityType) {
        Notification notification = Notification.builder()
                .recipientEmail(recipientEmail)
                .message(message)
                .notificationType(type)
                .entityId(entityId)
                .entityType(entityType)
                .timestamp(LocalDateTime.now())
                .read(false) // New notifications are unread by default
                .build();
        Notification savedNotification = notificationRepository.save(notification);
        logger.info("Created notification for {}: {}", recipientEmail, message);
        return savedNotification;
    }


    public List<Notification> getNotificationsForUser() {
        String currentUserEmail = getCurrentUserEmail();
        return notificationRepository.findByRecipientEmailOrderByTimestampDesc(currentUserEmail);
    }


    public List<Notification> getUnreadNotificationsForUser() {
        String currentUserEmail = getCurrentUserEmail();
        return notificationRepository.findByRecipientEmailAndReadFalseOrderByTimestampDesc(currentUserEmail);
    }


    @Transactional
    public Notification markNotificationAsRead(String notificationId) {
        String currentUserEmail = getCurrentUserEmail();
        Optional<Notification> optionalNotification = notificationRepository.findByIdAndRecipientEmail(notificationId, currentUserEmail);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setRead(true);
            Notification updatedNotification = notificationRepository.save(notification);
            logger.info("Marked notification {} as read for {}", notificationId, notification.getRecipientEmail());
            return updatedNotification;
        } else {
            logger.warn("Attempted to mark non-existent notification as read: {}", notificationId);
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }
    }

    /**
     * Deletes a notification by its ID.
     *
     * @param notificationId The ID of the notification to delete.
     * @throws ResourceNotFoundException if the notification is not found.
     */
    public void deleteNotification(String notificationId) {
        String currentUserEmail = getCurrentUserEmail();
        if (!notificationRepository.existsByIdAndRecipientEmail(notificationId, currentUserEmail)) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
        logger.info("Deleted notification: {}", notificationId);
    }
}
