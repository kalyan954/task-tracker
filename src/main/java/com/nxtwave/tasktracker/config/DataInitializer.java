package com.nxtwave.tasktracker.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.nxtwave.tasktracker.organization.entity.Organization;
import com.nxtwave.tasktracker.organization.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;

    @Override
    public void run(String... args) {

        if (!organizationRepository.existsByName("Default Organization")) {

            Organization organization =
                    new Organization();

            organization.setName(
                    "Default Organization"
            );

            organizationRepository.save(
                    organization
            );

            System.out.println(
                    "Default Organization created"
            );
        }
    }
}