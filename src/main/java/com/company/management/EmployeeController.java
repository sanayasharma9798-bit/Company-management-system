package com.company.management;

import com.company.management.entity.Employee;
import com.company.management.entity.User;
import com.company.management.repository.EmployeeRepository;
import com.company.management.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeController(
            EmployeeRepository employeeRepository,
            UserRepository userRepository) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    // Get all employees
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Get employees assigned to the logged-in manager
    @GetMapping("/my-employees")
    public List<Employee> getMyEmployees(
            Authentication authentication) {

        String username = authentication.getName();

        User manager = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        ));

        if (!"MANAGER".equals(manager.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only managers can view assigned employees"
            );
        }

        return employeeRepository.findByManagerId(manager.getId());
    }
    // Get profile of logged-in employee

@GetMapping("/my-profile")
public Employee getMyProfile(
        Authentication authentication) {

    String username = authentication.getName();

    User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "User not found"
                    ));

    if (!"EMPLOYEE".equals(user.getRole())) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only employees can view this profile"
        );
    }

    if (user.getEmployeeId() == null) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No employee is linked to this user"
        );
    }

    return employeeRepository.findById(user.getEmployeeId())
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Employee profile not found"
                    ));
}
@GetMapping("/my-manager")
public ManagerResponse getMyManager(
        Authentication authentication) {

    String username = authentication.getName();

    User employeeUser = userRepository.findByUsername(username)
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "User not found"
                    ));

    if (!"EMPLOYEE".equals(employeeUser.getRole())) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only employees can view manager details"
        );
    }

    if (employeeUser.getEmployeeId() == null) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No employee is linked to this user"
        );
    }

    Employee employee = employeeRepository.findById(
            employeeUser.getEmployeeId()
    ).orElseThrow(() ->
            new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Employee profile not found"
            ));

    if (employee.getManagerId() == null) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No manager assigned"
        );
    }

    User manager = userRepository.findById(employee.getManagerId())
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Manager not found"
                    ));

    return new ManagerResponse(
            manager.getId(),
            manager.getUsername(),
            manager.getRole()
    );
}

public record ManagerResponse(
        Long id,
        String username,
        String role
) {
}

    // Add employee
    @PostMapping
    public Employee addEmployee(@RequestBody Employee employee) {
        return employeeRepository.save(employee);
    }

    // Assign manager to employee
    @PutMapping("/{id}/manager")
    public Employee assignManager(
            @PathVariable Long id,
            @RequestParam Long managerId) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));

        employee.setManagerId(managerId);

        return employeeRepository.save(employee);
    }

    // Remove manager from employee
    @PutMapping("/{id}/remove-manager")
    public Employee removeManager(@PathVariable Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));

        employee.setManagerId(null);

        return employeeRepository.save(employee);
    }

    // Delete employee
    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
    }
}