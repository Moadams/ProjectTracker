package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.enums.TaskStatus;
import com.buildmaster.projecttracker.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Override
    Page<Task> findAll(Pageable pageable);


    List<Task> findByProjectId(Long projectId);

    @Query("SELECT t FROM Task t WHERE t.assignedDeveloper.id = :developerId")
    List<Task> findTasksByDeveloperId(@Param("developerId") Long developerId);

    List<Task> findByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();

}
