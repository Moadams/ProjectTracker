package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.Developer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {
    @Override
    Page<Developer> findAll(Pageable pageable);

    @Query(value = "SELECT d.id, d.name, d.email, COUNT(t.id) as task_count \n" +
            "FROM developers d \n" +
            "LEFT JOIN tasks t ON d.id = t.assigned_developer_id \n" +
            "GROUP BY d.id, d.name, d.email \n" +
            "ORDER BY task_count DESC \n" +
            "LIMIT 5",
            nativeQuery = true)
    List<Object[]> findTop5DevelopersWithMostTasks();

    Optional<Developer> findByEmail(@Email(message = "Enter a valid email address") @NotBlank(message = "Email must not be blank") String email);
}
