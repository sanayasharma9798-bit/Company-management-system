# Company Management System

## Project Overview
The Company Management System is a web-based application developed to manage employees, managers, and tasks within a company.

## Technologies Used
- Java
- Spring Boot
- Spring Security
- MySQL
- HTML
- CSS
- JavaScript
- Maven

## Features

### Admin
- View total employees and tasks.
- Manage users and their roles.
- Activate and deactivate user accounts.
- View employee and task details.

### Manager
- View assigned employees.
- Assign tasks to employees.
- View tasks assigned to their employees.
- Update task status.

### Employee
- View personal profile.
- View assigned tasks.
- Update task status.

## Security
- Login authentication.
- Role-based access control.
- Password encryption using BCrypt.
- Inactive users cannot log in.

## Database
MySQL is used to store employee, user, and task information.

## How to Run
1. Install Java, Maven, and MySQL.
2. Create the `company_management` database.
3. Configure database credentials in `application.properties`.
4. Run the Spring Boot application.
5. Open `http://localhost:8080` in your browser.

## Project Purpose
This project demonstrates company employee management, task assignment, and role-based access control.