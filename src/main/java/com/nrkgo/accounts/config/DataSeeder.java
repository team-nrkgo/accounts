package com.nrkgo.accounts.config;

import com.nrkgo.accounts.model.Role;
import com.nrkgo.accounts.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository) {
        return args -> {
            System.out.println("Checking Default Roles...");

            // Cleanup unwanted roles
            roleRepository.findByName("Editor").ifPresent(roleRepository::delete);
            roleRepository.findByName("Viewer").ifPresent(roleRepository::delete);

            // Ensure 'Super Admin' exists
            if (roleRepository.findByName("Super Admin").isEmpty()) {
                Role superAdmin = new Role();
                superAdmin.setName("Super Admin");
                superAdmin.setDescription("Full system access");
                roleRepository.save(superAdmin);
                System.out.println("Created 'Super Admin' role.");
            }

            // Ensure 'Admin' exists
            if (roleRepository.findByName("Admin").isEmpty()) {
                Role admin = new Role();
                admin.setName("Admin");
                admin.setDescription("Administrator with broad access");
                roleRepository.save(admin);
                System.out.println("Created 'Admin' role.");
            }
            
            System.out.println("Role Seeding Complete.");
        };
    }
}
