package com.nxtwave.tasktracker.task.specification;

import com.nxtwave.tasktracker.task.dto.TaskFilterRequest;
import com.nxtwave.tasktracker.task.entity.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task>
    withFilters(
            TaskFilterRequest filter
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if (filter.getStatus() != null) {

                predicates.add(
                        cb.equal(
                                root.get("status"),
                                filter.getStatus()
                        )
                );
            }

            if (filter.getPriority() != null) {

                predicates.add(
                        cb.equal(
                                root.get("priority"),
                                filter.getPriority()
                        )
                );
            }

            if (filter.getAssigneeId() != null) {

                predicates.add(
                        cb.equal(
                                root.get("assignee")
                                        .get("id"),
                                filter.getAssigneeId()
                        )
                );
            }

            return cb.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }
}