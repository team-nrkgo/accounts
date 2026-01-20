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
            if (roleRepository.count() == 0) {
                System.out.println("Seeding Default Roles...");
                
                Role admin = new Role();
                admin.setId(1L);
                admin.setName("Admin");
                admin.setDescription("Administrator with full access");
                roleRepository.save(admin);

                Role editor = new Role();
                editor.setId(2L);
                editor.setName("Editor");
                editor.setDescription("Can edit content but cannot manage users");
                roleRepository.save(editor);

                Role viewer = new Role();
                viewer.setId(3L);
                viewer.setName("Viewer");
                viewer.setDescription("Read-only access");
                roleRepository.save(viewer);
                
                System.out.println("Roles Seeded Successfully.");
            }
        };
    }
}
