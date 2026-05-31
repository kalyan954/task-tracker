package com.nxtwave.tasktracker.task.validator;

import com.nxtwave.tasktracker.common.enums.TaskStatus;

import java.util.Map;
import java.util.Set;

public class TaskStatusTransitionValidator {

    private static final Map<TaskStatus,
            Set<TaskStatus>> ALLOWED_TRANSITIONS =
            Map.of(

                    TaskStatus.TODO,
                    Set.of(
                            TaskStatus.IN_PROGRESS,
                            TaskStatus.BLOCKED
                    ),

                    TaskStatus.IN_PROGRESS,
                    Set.of(
                            TaskStatus.IN_REVIEW,
                            TaskStatus.BLOCKED
                    ),

                    TaskStatus.IN_REVIEW,
                    Set.of(
                            TaskStatus.DONE,
                            TaskStatus.BLOCKED
                    ),

                    TaskStatus.DONE,
                    Set.of(),

                    TaskStatus.BLOCKED,
                    Set.of(
                            TaskStatus.IN_PROGRESS
                    )
            );

        public static boolean isValidTransition(TaskStatus currentStatus, TaskStatus newStatus) {

            return ALLOWED_TRANSITIONS
                    .getOrDefault(
                            currentStatus,
                            Set.of()
                    )
                    .contains(newStatus);
        }

}