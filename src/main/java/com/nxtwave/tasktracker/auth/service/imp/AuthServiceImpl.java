package com.nxtwave.tasktracker.auth.service.imp;

import com.nxtwave.tasktracker.auth.dto.RegisterRequest;
import com.nxtwave.tasktracker.auth.service.AuthService;
import com.nxtwave.tasktracker.organization.entity.Organization;
import com.nxtwave.tasktracker.organization.repository.OrganizationRepository;
import com.nxtwave.tasktracker.user.entity.User;
import com.nxtwave.tasktracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                        .orElseThrow(() ->
                                new RuntimeException("Organization not found"));

        User user = new User();

        user.setName(request.getName());

        user.setEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(request.getRole());

        user.setOrganization(organization);

        userRepository.save(user);
    }
}