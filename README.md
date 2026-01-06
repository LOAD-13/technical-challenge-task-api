# Task Manager API

API REST para la gestión de tareas, desarrollada con Java 17 y Spring Boot 3. Incluye validaciones, manejo de errores global, persistencia en H2 y estadísticas calculadas con Java Streams.

## Descripción del Proyecto

Esta API implementa un CRUD completo de tareas con las siguientes características:
* Operaciones CRUD (Crear, Leer, Actualizar, Eliminar)
* Validaciones con Bean Validation
* Manejo global de errores con `@ControllerAdvice`
* Reglas de negocio implementadas en capa Service
* Endpoint de estadísticas que utiliza Java Streams
* Base de datos en memoria H2

## Repositorio y Pull Request

* **Repositorio:** https://github.com/LOAD-13/technical-challenge-task-api
* **Pull Request:** [Enlace al PR desde feature/tasks-crud hacia main]

## Requisitos

* Java 17 (JDK)
* Maven 3.6+

## Cómo ejecutar

1.  **Clonar el repositorio**
    ```bash
    git clone https://github.com/LOAD-13/technical-challenge-task-api.git
    cd technical-challenge-task-api
    ```

2.  **Ejecutar pruebas automatizadas**
    ```bash
    mvn clean test
    ```

3.  **Iniciar la aplicación**
    ```bash
    mvn spring-boot:run
    ```
    La API estará disponible en: `http://localhost:8080`

## Consola H2 (Base de Datos)

Puedes inspeccionar la base de datos en memoria accediendo a:
* **URL:** `http://localhost:8080/h2-console`
* **JDBC URL:** `jdbc:h2:mem:taskdb`
* **User Name:** `sa`
* **Password:** (dejar vacío)

## Ejemplos de Uso (cURL)

### 1. Crear Tarea (POST)
```bash
curl -v -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Terminar Reto Java", "priority": "HIGH", "status": "TODO", "dueDate": "2025-12-31"}'
```

### 2. Listar Tareas (GET)
```bash
curl -v http://localhost:8080/api/tasks
```

### 3. Actualizar Estado (PATCH)
```bash
curl -v -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DONE"}'
```

### 4. Ver Estadísticas (GET)
```bash
curl -v http://localhost:8080/api/tasks/stats
```

## Reglas de Negocio Implementadas

### Regla A: No se puede marcar DONE si la tarea está vencida (409 Conflict)
* No se puede cambiar el estado a `DONE` si la tarea está vencida (`dueDate` anterior a hoy).
* **Código HTTP:** `409 Conflict`
* **Mensaje de error:** "No se puede completar una tarea vencida"

### Regla B: Para prioridad HIGH, dueDate es obligatoria (400 Bad Request)
* Si la prioridad es `HIGH`, la fecha de vencimiento (`dueDate`) es obligatoria.
* **Código HTTP:** `400 Bad Request`
* **Mensaje de error:** "La fecha de vencimiento es obligatoria cuando la prioridad es HIGH"

## Códigos de Estado HTTP

* **200 OK:** Operación exitosa (GET, PUT, PATCH)
* **201 Created:** Tarea creada exitosamente
* **204 No Content:** Tarea eliminada exitosamente
* **400 Bad Request:** Error de validación o Regla B violada
* **404 Not Found:** Tarea no encontrada
* **409 Conflict:** Regla A violada (intentar completar tarea vencida)

## Endpoint de Estadísticas

El endpoint `/api/tasks/stats` retorna estadísticas agregadas calculadas con Java Streams:

```json
{
  "total": 12,
  "byStatus": { "TODO": 5, "IN_PROGRESS": 4, "DONE": 3 },
  "byPriority": { "LOW": 2, "MEDIUM": 7, "HIGH": 3 },
  "overdue": 2,
  "next7Days": [
    {
      "id": 10,
      "title": "Pagar recibos",
      "dueDate": "2026-01-06",
      "priority": "HIGH",
      "status": "TODO"
    }
  ]
}
```

## Pruebas Automatizadas

El proyecto incluye 4 pruebas automatizadas que cubren:
1. **Test 1:** Crear tarea válida (201 Created)
2. **Test 2:** Validar Regla B - priority HIGH requiere dueDate (400 Bad Request)
3. **Test 3:** Validar Regla A - no permitir DONE en tarea vencida (409 Conflict)
4. **Test 4:** Estadísticas con Java Streams (200 OK)

Ejecutar pruebas:
```bash
mvn clean test
```