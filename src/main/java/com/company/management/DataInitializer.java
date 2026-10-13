
package com.company.management;

import com.company.management.entity.User;
import com.company.management.entity.Employee;
import com.company.management.repository.UserRepository;
import com.company.management.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createUsers(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String adminUsername = System.getenv("APP_ADMIN_USERNAME");
            String adminPassword = System.getenv("APP_ADMIN_PASSWORD");

            if (adminUsername != null && !adminUsername.isBlank()
                    && adminPassword != null && !adminPassword.isBlank()) {
                User admin = userRepository.findByUsername(adminUsername)
                        .orElseGet(User::new);
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole("ADMIN");
                userRepository.save(admin);
                System.out.println("Admin account created or updated.");
            }

            createIfMissing(userRepository, employeeRepository,
                    passwordEncoder, "APP_MANAGER_USERNAME",
                    "APP_MANAGER_PASSWORD", "MANAGER");

            createIfMissing(userRepository, employeeRepository,
                    passwordEncoder, "APP_EMPLOYEE_USERNAME",
                    "APP_EMPLOYEE_PASSWORD", "EMPLOYEE");
        };
    }

    private void createIfMissing(
            UserRepository repo,
            EmployeeRepository employeeRepository,
            PasswordEncoder encoder,
            String usernameKey,
            String passwordKey,
            String role) {

        String username = System.getenv(usernameKey);
        String password = System.getenv(passwordKey);

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            System.out.println(role + " account not created: set environment variables.");
            return;
        }

        if (repo.findByUsername(username).isPresent()) {
            System.out.println(role + " account already exists.");
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setRole(role);

        if (role.equals("EMPLOYEE")) {
            Employee employee = new Employee();
            employee.setName("Demo Employee");
            employee.setEmail("employee@example.com");
            employee.setDepartment("ECE");
            employee.setDesignation("Software Developer");

            Employee savedEmployee = employeeRepository.save(employee);
            user.setEmployeeId(savedEmployee.getId());
        }

        repo.save(user);
        System.out.println(role + " account created successfully.");
    }
}