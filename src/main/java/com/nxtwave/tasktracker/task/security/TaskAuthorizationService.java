package com.nxtwave.tasktracker.task.security;

import com.nxtwave.tasktracker.common.enums.Role;
import com.nxtwave.tasktracker.common.exception.ForbiddenException;
import com.nxtwave.tasktracker.task.entity.Task;
import com.nxtwave.tasktracker.user.entity.User;
import org.springframework.stereotype.Service;

@Service
public class TaskAuthorizationService {

    public void validateTaskViewAndStatusUpdatePermission(
            Task task,
            User currentUser
    ) {

        // MANAGER can always update

        if (currentUser.getRole() == Role.MANAGER) {
            return;
        }

        // ADMIN can also update 

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        // Assignee can update

        if (task.getAssignee()
                .getId()
                .equals(currentUser.getId())) {
            return;
        }

        throw new ForbiddenException(
                "You are not authorized to update/view this task"
        );
    }

    public void validateOrganizationAccess(Task task,User currentUser) {

        Long taskOrganizationId =
                task.getAssignee()
                        .getOrganization()
                        .getId();

        Long currentUserOrganizationId =
                currentUser
                        .getOrganization()
                        .getId();

        if (!taskOrganizationId.equals(
                currentUserOrganizationId
        )) {

            throw new ForbiddenException(
                    "You cannot access tasks from another organization"
            );
        }
    }
}
