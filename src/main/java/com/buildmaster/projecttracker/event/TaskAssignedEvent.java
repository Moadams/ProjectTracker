package com.buildmaster.projecttracker.event;

import com.buildmaster.projecttracker.model.Developer;
import com.buildmaster.projecttracker.model.Task;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TaskAssignedEvent  extends ApplicationEvent {
    private final Task task;
    private final Developer assignedDeveloper;

    public TaskAssignedEvent(Object source, Task task, Developer assignedDeveloper) {
        super(source);
        this.task = task;
        this.assignedDeveloper = assignedDeveloper;
    }

}
