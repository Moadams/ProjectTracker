package com.buildmaster.projecttracker.service;

import com.buildmaster.projecttracker.event.TaskAssignedEvent;
import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.model.Task;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    @EventListener
    @Async
    public void handleTaskAssignedEvent(TaskAssignedEvent event) {
        Task task = event.getTask();
        Developer assignedDeveloper = task.getAssignedDeveloper();

        if(assignedDeveloper == null) {
            return;
        }

        System.out.println("\n--- Sending Email Notification for task assignment ---");
        String recipientEmail = assignedDeveloper.getEmail();
        String subject = String.format("New Task Assignment: '%s' (Project: %s)", task.getTitle(), task.getProject().getName());
        String text = String.format(
                "Dear %s,\n\n" +
                        "A new task has been assigned to you:\n\n" +
                        "Task Title: %s\n" +
                        "Description: %s\n" +
                        "Project: %s\n" +
                        "Due Date: %s\n" +
                        "Status: %s\n\n" +
                        "Please log in to the Project Tracker to view details and update your progress.\n\n" +
                        "Regards,\n" +
                        "BuildMaster Project Tracker Bot",
                assignedDeveloper.getName(),
                task.getTitle(),
                task.getDescription(),
                task.getProject().getName(),
                task.getDueDate(),
                task.getStatus()
        );

        System.out.println("Subject: " + subject);
        System.out.println("To: " + recipientEmail);
        System.out.println("From: " + "fromEmail");
        System.out.println("Body:\n" + text);
        System.out.println("---------------------------------------------------\n");

    }
}
