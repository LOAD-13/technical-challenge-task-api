package com.joaquin.taskmanager.repository;

import com.joaquin.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // JpaRepository ya nos da métodos como save(), findAll(), findById(), deleteById()
    // No necesitamos escribir SQL manual por ahora.
}