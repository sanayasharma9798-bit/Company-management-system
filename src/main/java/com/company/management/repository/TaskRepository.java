
package com.company.management.repository;

import com.company.management.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByEmployeeId(Long employeeId);

}
