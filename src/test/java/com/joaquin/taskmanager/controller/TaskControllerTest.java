package com.joaquin.taskmanager.controller;

import com.joaquin.taskmanager.exception.BusinessException;
import com.joaquin.taskmanager.model.Task;
import com.joaquin.taskmanager.model.TaskPriority;
import com.joaquin.taskmanager.model.TaskStatus;
import com.joaquin.taskmanager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.Map;

@WebMvcTest(TaskController.class) // Probamos solo el Controller
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula peticiones HTTP sin abrir servidor real

    @MockBean
    private TaskService taskService; // Simulamos el servicio (no usa la BD real)

    // TEST 1: Crear Tarea Válida (201)
    @Test
    public void createTask_ShouldReturnCreated_WhenDataIsValid() throws Exception {
        // 1. Preparar datos de prueba (Given)
        Task savedTask = Task.builder()
                .id(1L)
                .title("Aprender JUnit")
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .build();

        Mockito.when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

        // 2. Ejecutar la petición POST y Verificar (When & Then)
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Aprender JUnit",
                                    "description": "Prueba unitaria",
                                    "priority": "MEDIUM"
                                }
                                """))
                .andExpect(status().isCreated()) // Esperamos 201 Created
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Aprender JUnit"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    // TEST 2: Regla B - Priority HIGH requiere dueDate (400)
    @Test
    public void createTask_ShouldReturnBadRequest_WhenPriorityHighWithoutDate() throws Exception {
        // Simulamos que el servicio lanza la excepción de negocio cuando recibe datos inválidos
        Mockito.when(taskService.createTask(any(Task.class)))
                .thenThrow(new BusinessException("La fecha es obligatoria para prioridad HIGH", HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Tarea Urgente",
                                    "priority": "HIGH"
                                }
                                """)) // Enviamos sin dueDate a propósito
                .andExpect(status().isBadRequest()) // Esperamos 400
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La fecha es obligatoria para prioridad HIGH"));
    }

    // TEST 3: Regla A - No permitir DONE en tarea vencida (409)
    @Test
    public void updateStatus_ShouldReturnConflict_WhenTaskIsOverdue() throws Exception {
        // Simulamos que el servicio lanza conflicto al intentar actualizar status
        Mockito.when(taskService.updateStatus(eq(1L), eq(TaskStatus.DONE)))
                .thenThrow(new BusinessException("No se puede completar una tarea vencida", HttpStatus.CONFLICT));

        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "DONE"
                                }
                                """))
                .andExpect(status().isConflict()) // Esperamos 409
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("No se puede completar una tarea vencida"));
    }

    // TEST 4 (Stats): Validar que el endpoint responda 200 y la estructura correcta
    @Test
    public void getStats_ShouldReturnCorrectMetrics() throws Exception {
        // Simulamos una respuesta del servicio
        Map<String, Object> mockStats = Map.of(
                "total", 5,
                "overdue", 1
        );

        Mockito.when(taskService.getTaskStats()).thenReturn(mockStats);

        mockMvc.perform(get("/api/tasks/stats")) // Asegúrate de importar 'get'
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.overdue").value(1));
    }
}