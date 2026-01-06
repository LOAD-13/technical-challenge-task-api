package com.joaquin.taskmanager.service;

import com.joaquin.taskmanager.exception.BusinessException;
import com.joaquin.taskmanager.model.Task;
import com.joaquin.taskmanager.model.TaskPriority;
import com.joaquin.taskmanager.model.TaskStatus;
import com.joaquin.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // GET con filtros
    public List<Task> getTasks(TaskStatus status, TaskPriority priority, String search) {
        return taskRepository.search(status, priority, search);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        // Validación Regla B
        validateRuleB(task);
        return taskRepository.save(task);
    }

    public Optional<Task> updateTask(Long id, Task taskDetails) {
        return taskRepository.findById(id).map(existingTask -> {
            // Actualizamos campos
            existingTask.setTitle(taskDetails.getTitle());
            existingTask.setDescription(taskDetails.getDescription());
            existingTask.setPriority(taskDetails.getPriority());
            existingTask.setDueDate(taskDetails.getDueDate());
            existingTask.setStatus(taskDetails.getStatus());

            // Validamos Reglas
            validateRuleB(existingTask);
            validateRuleA(existingTask, existingTask.getStatus()); // Verificamos si intentan poner DONE vencido

            return taskRepository.save(existingTask);
        });
    }

    // Nuevo método para PATCH (Solo cambia el estado)
    public Optional<Task> updateStatus(Long id, TaskStatus newStatus) {
        return taskRepository.findById(id).map(existingTask -> {
            validateRuleA(existingTask, newStatus); // Validar Regla A antes de cambiar
            existingTask.setStatus(newStatus);
            return taskRepository.save(existingTask);
        });
    }

    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // --- REGLAS DE NEGOCIO ---

    // Regla B: priority HIGH requiere dueDate
    private void validateRuleB(Task task) {
        if (task.getPriority() == TaskPriority.HIGH && task.getDueDate() == null) {
            throw new BusinessException("La fecha de vencimiento es obligatoria cuando la prioridad es HIGH", HttpStatus.BAD_REQUEST);
        }
    }

    // Regla A: No permitir DONE en tarea vencida
    private void validateRuleA(Task task, TaskStatus newStatus) {
        if (newStatus == TaskStatus.DONE) {
            // Si no tiene fecha, no puede vencer, así que solo validamos si tiene fecha
            if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
                throw new BusinessException("No se puede completar una tarea vencida", HttpStatus.CONFLICT);
            }
        }
    }

    public Map<String, Object> getTaskStats() {
        List<Task> allTasks = taskRepository.findAll();
        Map<String, Object> stats = new HashMap<>();

        // 1. Total de tareas
        stats.put("total", allTasks.size());

        // 2. Agrupado por Estado (Map<TaskStatus, Long>)
        stats.put("byStatus", allTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting())));

        // 3. Agrupado por Prioridad (Map<TaskPriority, Long>)
        stats.put("byPriority", allTasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting())));

        // 4. Tareas Vencidas (dueDate < hoy y status != DONE)
        LocalDate today = LocalDate.now();
        stats.put("overdue", allTasks.stream()
                .filter(t -> t.getDueDate() != null)
                .filter(t -> t.getDueDate().isBefore(today))
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .count());

        // 5. Próximos 7 días (ordenadas por fecha, máx 5)
        stats.put("next7Days", allTasks.stream()
                .filter(t -> t.getDueDate() != null)
                .filter(t -> !t.getDueDate().isBefore(today)) // Desde hoy (inclusive)
                .filter(t -> t.getDueDate().isBefore(today.plusDays(8))) // Hasta 7 días después
                .sorted(Comparator.comparing(Task::getDueDate))
                .limit(5)
                .collect(Collectors.toList()));

        return stats;
    }

}