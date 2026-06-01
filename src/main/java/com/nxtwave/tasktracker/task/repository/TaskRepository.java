package com.nxtwave.tasktracker.task.repository;

import com.nxtwave.tasktracker.task.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface TaskRepository
        extends JpaRepository<Task, Long>,
                JpaSpecificationExecutor<Task> {

    @Override
    @EntityGraph(attributePaths = {"assignee", "assignee.organization"})
    Optional<Task> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"assignee", "assignee.organization"})
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);
}