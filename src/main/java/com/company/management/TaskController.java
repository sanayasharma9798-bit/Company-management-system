package com.company.management;

import com.company.management.entity.Task;
import com.company.management.entity.User;
import com.company.management.repository.EmployeeRepository;
import com.company.management.repository.TaskRepository;
import com.company.management.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public TaskController(
            TaskRepository taskRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }


    // =========================================================
    // GET ALL TASKS
    // Admin and Manager only
    // =========================================================

    @GetMapping
    public List<Task> getAllTasks() {

        return taskRepository.findAll();
    }


    // =========================================================
    // GET MANAGER'S TASKS
    // Manager can see only tasks of assigned employees
    // =========================================================

    @GetMapping("/my-tasks")
    public List<Task> getMyTasks(
            Authentication authentication) {

        String username =
                authentication.getName();

        User manager =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User not found"
                                ));

        if (!"MANAGER".equals(manager.getRole())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only managers can view their tasks"
            );
        }

        List<Long> employeeIds =
                employeeRepository
                        .findByManagerId(manager.getId())
                        .stream()
                        .map(employee -> employee.getId())
                        .toList();

        return taskRepository.findAll()
                .stream()
                .filter(task ->
                        employeeIds.contains(
                                task.getEmployeeId()
                        ))
                .toList();
    }


    // =========================================================
    // GET TASKS BY EMPLOYEE
    // Employee can see only their own tasks
    // =========================================================

    @GetMapping("/employee/{employeeId}")
    public List<Task> getTasksByEmployee(
            @PathVariable Long employeeId,
            Authentication authentication) {

        String username =
                authentication.getName();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User not found"
                                ));

        // Employee can only see their own tasks
        if ("EMPLOYEE".equals(user.getRole())) {

            if (user.getEmployeeId() == null
                    || !user.getEmployeeId()
                            .equals(employeeId)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only view your own tasks"
                );
            }
        }

        return taskRepository.findAll()
                .stream()
                .filter(task ->
                        employeeId.equals(
                                task.getEmployeeId()
                        ))
                .toList();
    }


    // =========================================================
    // CREATE TASK
    // Manager can assign task only to assigned employees
    // =========================================================

    @PostMapping
    public Task createTask(
            @RequestBody Task task,
            Authentication authentication) {

        String username =
                authentication.getName();

        User manager =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User not found"
                                ));

        if (!"MANAGER".equals(manager.getRole())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only managers can create tasks"
            );
        }

        if (task.getEmployeeId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee ID is required"
            );
        }

        // Check whether employee belongs to this manager
        boolean assignedToManager =
                employeeRepository
                        .findByManagerId(manager.getId())
                        .stream()
                        .anyMatch(employee ->
                                employee.getId()
                                        .equals(
                                                task.getEmployeeId()
                                        ));

        if (!assignedToManager) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only assign tasks to your assigned employees"
            );
        }

        // Default task status
        if (task.getStatus() == null
                || task.getStatus().isBlank()) {

            task.setStatus("PENDING");
        }

        return taskRepository.save(task);
    }


    // =========================================================
    // UPDATE TASK STATUS
    // Employee can update own task
    // Manager/Admin can update tasks
    // =========================================================

    @PutMapping("/{id}/status")
    public Task updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Task updatedTask,
            Authentication authentication) {

        Task task =
                taskRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Task not found"
                                ));

        String username =
                authentication.getName();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User not found"
                                ));


        // Employee can update only their own task
        if ("EMPLOYEE".equals(user.getRole())) {

            if (user.getEmployeeId() == null
                    || !user.getEmployeeId()
                            .equals(task.getEmployeeId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only update your own tasks"
                );
            }
        }


        // Manager can update only tasks of assigned employees
        if ("MANAGER".equals(user.getRole())) {

            boolean assignedToManager =
                    employeeRepository
                            .findByManagerId(user.getId())
                            .stream()
                            .anyMatch(employee ->
                                    employee.getId()
                                            .equals(
                                                    task.getEmployeeId()
                                            ));

            if (!assignedToManager) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only update tasks of your assigned employees"
                );
            }
        }


        // Validate status
        String status =
                updatedTask.getStatus();

        if (status == null
                || !List.of(
                        "PENDING",
                        "IN_PROGRESS",
                        "COMPLETED"
                ).contains(status)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid task status"
            );
        }

        task.setStatus(status);

        return taskRepository.save(task);
    }
}