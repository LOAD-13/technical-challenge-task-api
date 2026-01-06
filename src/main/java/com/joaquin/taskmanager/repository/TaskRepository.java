package com.joaquin.taskmanager.repository;

import com.joaquin.taskmanager.model.Task;
import com.joaquin.taskmanager.model.TaskPriority;
import com.joaquin.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Consulta personalizada para filtrar por todo a la vez (si el parámetro es null, lo ignora)
    @Query("SELECT t FROM Task t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Task> search(TaskStatus status, TaskPriority priority, String search);
}