package com.nxtwave.tasktracker.user.security;

import org.springframework.stereotype.Service;

import com.nxtwave.tasktracker.common.exception.ForbiddenException;
import com.nxtwave.tasktracker.user.entity.User;

@Service
public class UserAuthorizationService {

    public void validateOrganizationAccess(
            User targetUser,
            User currentUser
    ) {

        Long targetOrganizationId =
                targetUser.getOrganization()
                        .getId();

        Long currentOrganizationId =
                currentUser.getOrganization()
                        .getId();

        if (!targetOrganizationId.equals(
                currentOrganizationId
        )) {

            throw new ForbiddenException(
                    "You cannot access users from another organization"
            );
        }
    }
}
