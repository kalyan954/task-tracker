package com.nxtwave.tasktracker.project.repository;

import com.nxtwave.tasktracker.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Override
    @EntityGraph(attributePaths = {"organization", "createdBy"})
    Optional<Project> findById(Long id);

    @EntityGraph(attributePaths = {"organization", "createdBy"})
    Page<Project> findByOrganizationId(Long organizationId, Pageable pageable);
}
