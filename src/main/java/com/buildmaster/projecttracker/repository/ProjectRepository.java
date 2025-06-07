package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.enums.ProjectStatus;
import com.buildmaster.projecttracker.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.id = :id")
    Optional<Project> findWithTasksById(@Param("id") Long id);

    @Override
    Page<Project> findAll(Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN p.tasks t WHERE t.id IS NULL")
    List<Project> findProjectsWithoutTasks();

    @Query("SELECT p FROM Project p WHERE p.deadLine < :now AND p.status = :status")
    List<Project> findOverdueProjects(@Param("now") LocalDate now, @Param("status") ProjectStatus status);

}
