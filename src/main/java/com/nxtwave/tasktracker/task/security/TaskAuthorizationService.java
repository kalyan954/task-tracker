package com.nxtwave.tasktracker.task.security;

import com.nxtwave.tasktracker.common.enums.Role;
import com.nxtwave.tasktracker.common.exception.UnauthorizedException;
import com.nxtwave.tasktracker.task.entity.Task;
import com.nxtwave.tasktracker.user.entity.User;
import org.springframework.stereotype.Service;

@Service
public class TaskAuthorizationService {

    public void validateTaskStatusUpdatePermission(
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

        throw new UnauthorizedException(
                "You are not authorized to update this task"
        );
    }
}