package com.buildmaster.projecttracker.controller;

import com.buildmaster.projecttracker.model.Notification;
import com.buildmaster.projecttracker.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications for the authenticated user (most recent first)", security = @SecurityRequirement(name = "bearerAuth")) // CORRECTED
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotificationsForCurrentUser() {
        List<Notification> notifications = notificationService.getNotificationsForUser();
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unread notifications for the authenticated user (most recent first)", security = @SecurityRequirement(name = "bearerAuth")) // CORRECTED
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotificationsForCurrentUser() {
        List<Notification> unreadNotifications = notificationService.getUnreadNotificationsForUser();
        return ResponseEntity.ok(unreadNotifications);
    }

    @Operation(summary = "Mark a specific notification as read", security = @SecurityRequirement(name = "bearerAuth")) // CORRECTED
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markNotificationAsRead(@PathVariable String id) {
        Notification updatedNotification = notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok(updatedNotification);
    }

    @Operation(summary = "Delete a specific notification", security = @SecurityRequirement(name = "bearerAuth")) // CORRECTED
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
