package com.buildmaster.projecttracker.event;

import com.buildmaster.projecttracker.model.Task;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


import java.util.List;


@Getter
public class TaskOverdueEvent extends ApplicationEvent {
    List<Task> overdueTasks;

    public TaskOverdueEvent(Object source, List<Task> overdueTasks) {
        super(source);
        this.overdueTasks = overdueTasks;
    }

}
