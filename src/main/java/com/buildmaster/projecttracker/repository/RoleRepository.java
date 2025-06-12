package com.buildmaster.projecttracker.repository;

import com.buildmaster.projecttracker.enums.RoleName;
import com.buildmaster.projecttracker.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
