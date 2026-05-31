package com.nxtwave.tasktracker.auth.service.imp;

import com.nxtwave.tasktracker.auth.dto.AuthResponse;
import com.nxtwave.tasktracker.auth.dto.LoginRequest;
import com.nxtwave.tasktracker.auth.dto.RegisterRequest;
import com.nxtwave.tasktracker.auth.service.AuthService;
import com.nxtwave.tasktracker.common.exception.ResourceAlreadyExistsException;
import com.nxtwave.tasktracker.common.exception.ResourceNotFoundException;
import com.nxtwave.tasktracker.common.exception.UnauthorizedException;
import com.nxtwave.tasktracker.organization.entity.Organization;
import com.nxtwave.tasktracker.organization.repository.OrganizationRepository;
import com.nxtwave.tasktracker.refreshToken.entity.RefreshToken;
import com.nxtwave.tasktracker.refreshToken.service.RefreshTokenService;
import com.nxtwave.tasktracker.security.jwt.JwtService;
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

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(() ->
                                new UnauthorizedException(
                                        "Invalid email or password"
                                )
                        );

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {

            throw new UnauthorizedException(
                    "Invalid email or password"
            );
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Organization not found"));

        User user = new User();

        user.setName(request.getName());

        user.setEmail(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(request.getRole());

        user.setOrganization(organization);

        userRepository.save(user);
    }


}