package com.company.management;

import com.company.management.entity.User;
import com.company.management.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Show all users without passwords
    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getRole(),
                        user.getEmployeeId(),
                        user.isActive()
                ))
                .toList();
    }

    // Create new user
    @PostMapping
    public UserResponse createUser(@RequestBody User user) {

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        user.setActive(true);

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole(),
                savedUser.getEmployeeId(),
                savedUser.isActive()
        );
    }

    // Update user
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updatedUser.getUsername() != null
                && !updatedUser.getUsername().isBlank()) {

            user.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getRole() != null
                && !updatedUser.getRole().isBlank()) {

            user.setRole(updatedUser.getRole());
        }

        user.setEmployeeId(updatedUser.getEmployeeId());

        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(updatedUser.getPassword())
            );
        }

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole(),
                savedUser.getEmployeeId(),
                savedUser.isActive()
        );
    }
    // Activate / Deactivate user
@PutMapping("/{id}/status")
public UserResponse toggleUserStatus(@PathVariable Long id) {

    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

    user.setActive(!user.isActive());

    User savedUser = userRepository.save(user);

    return new UserResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getRole(),
            savedUser.getEmployeeId(),
            savedUser.isActive()
    );
}

    // Response class — password intentionally excluded
    public record UserResponse(
            Long id,
            String username,
            String role,
            Long employeeId,
            boolean active
    ) {
    }
}
     

    

  
