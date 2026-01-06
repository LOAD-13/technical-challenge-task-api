package com.joaquin.taskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data // Genera getters, setters, toString, etc. (Lombok)
@Builder // Permite crear objetos con .builder().build()
@NoArgsConstructor // Constructor vacío requerido por JPA
@AllArgsConstructor // Constructor con todo requerido por Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 3, max = 80, message = "El título debe tener entre 3 y 80 caracteres")
    @Column(nullable = false)
    private String title;

    @Size(max = 250, message = "La descripción no puede exceder los 250 caracteres")
    private String description;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING) // Guarda "TODO" en la BD en vez de un número
    private TaskStatus status = TaskStatus.TODO; // Valor por defecto

    @NotNull(message = "La prioridad es obligatoria")
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    private LocalDate dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist // Se ejecuta antes de guardar por primera vez
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TaskStatus.TODO;
        }
    }

    @PreUpdate // Se ejecuta antes de actualizar cualquier cambio
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}