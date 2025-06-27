package com.buildmaster.projecttracker.util;

import com.buildmaster.projecttracker.model.Task;
import com.buildmaster.projecttracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("taskSecurity")
@RequiredArgsConstructor
public class SecurityUtil {

    private final TaskRepository taskRepository;

    /**
     * Checks if the currently authenticated user is assigned to the task with the given ID.
     * Used in @PreAuthorize: @PreAuthorize("@taskSecurity.isTaskOwner(#taskId)")
     */
    public boolean isTaskOwner(Long taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUserEmail;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            currentUserEmail = ((UserDetails) principal).getUsername();
        } else {
            return false;
        }

        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isEmpty()) {
            return false;
        }

        Task task = taskOptional.get();
        if(task.getAssignedDeveloper() == null){
            return false;
        }
        String assignedDeveloperEmail = task.getAssignedDeveloper().getEmail();


        return assignedDeveloperEmail.equals(currentUserEmail);
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return null;
    }
}