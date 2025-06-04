package com.buildmaster.projecttracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="developers")
@Builder
public class Developer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Developer name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email")
    @Column(nullable = false, unique = true)
    private String email;

    @ElementCollection
    @CollectionTable(name = "developer_skills", joinColumns = @JoinColumn(name = "developer_id"))
    @Column(name = "skill")
    @Builder.Default
    private Set<String> skills = new HashSet<>();

    @OneToMany(mappedBy = "assignedDeveloper", fetch = FetchType.LAZY)
    private Set<Task> assignedTasks = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
