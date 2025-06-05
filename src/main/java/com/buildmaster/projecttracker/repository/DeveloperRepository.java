package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.model.Developer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {
    @Override
    Page<Developer> findAll(Pageable pageable);

    boolean existsByEmail(String email);
}
