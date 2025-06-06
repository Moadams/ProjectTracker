package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.Developer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {
    @Override
    Page<Developer> findAll(Pageable pageable);

    boolean existsByEmail(String email);



    @Query(value = "SELECT d.id, d.name, d.email, COUNT(t.id) as task_count \n" +
            "FROM developers d \n" +
            "LEFT JOIN tasks t ON d.id = t.assigned_developer_id \n" +
            "GROUP BY d.id, d.name, d.email \n" +
            "ORDER BY task_count DESC \n" +
            "LIMIT 5",
            nativeQuery = true)
    List<Object[]> findTop5DevelopersWithMostTasks();
}
