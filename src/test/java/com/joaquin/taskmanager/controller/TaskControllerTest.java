package com.joaquin.taskmanager.controller;

import com.joaquin.taskmanager.model.Task;
import com.joaquin.taskmanager.model.TaskPriority;
import com.joaquin.taskmanager.model.TaskStatus;
import com.joaquin.taskmanager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(TaskController.class) // Probamos solo el Controller
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simula peticiones HTTP sin abrir servidor real

    @MockBean
    private TaskService taskService; // Simulamos el servicio (no usa la BD real)

    @Test
    public void createTask_ShouldReturnCreated_WhenDataIsValid() throws Exception {
        // 1. Preparar datos de prueba (Given)
        Task newTask = Task.builder()
                .title("Aprender JUnit")
                .description("Prueba unitaria")
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .build();

        // Simulamos que el servicio guarda y devuelve la tarea con ID 1
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
                .andExpect(jsonPath("$.status").value("TODO")); // Default si no se envía
    }
}