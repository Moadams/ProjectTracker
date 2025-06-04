package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.enums.ProjectStatus;
import com.buildmaster.projecttracker.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByDeadLineBefore(LocalDate date);

    @Override
    Page<Project> findAll(Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN p.tasks t WHERE t.id IS NULL")
    List<Project> findProjectsWithoutTasks();
}
