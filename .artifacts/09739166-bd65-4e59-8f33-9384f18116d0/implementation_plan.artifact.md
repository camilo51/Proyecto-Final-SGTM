# Estructuración y Refinamiento del Módulo de Login

Este plan detalla las mejoras para dejar el sistema de autenticación (Login) con una arquitectura sólida siguiendo las mejores prácticas de Android (MVVM, Clean Architecture principles, y Robust Error Handling).

## User Review Required

> [!IMPORTANT]
> Se implementará una gestión de errores más robusta en el ViewModel y se estandarizará el acceso al Repositorio.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [Login.kt](file:///C:/Users/Alexi/AndroidStudioProjects/MyApplication/Proyecto-Final-SGTM/app/src/main/java/com/example/myapplication/data/model/Login.kt)
- Limpieza de modelos para asegurar que todos tengan `@SerializedName`.
- Asegurar que `UserDto` contenga la información necesaria para identificar roles si fuera necesario (por ahora basado en nombre/email).

#### [MODIFY] [LoginRepository.kt](file:///C:/Users/Alexi/AndroidStudioProjects/MyApplication/Proyecto-Final-SGTM/app/src/main/java/com/example/myapplication/data/repository/LoginRepository.kt)
- Refactorización para manejar excepciones comunes de red y mapearlas a resultados legibles.

### [UI Layer]

#### [MODIFY] [LoginViewModel.kt](file:///C:/Users/Alexi/AndroidStudioProjects/MyApplication/Proyecto-Final-SGTM/app/src/main/java/com/example/myapplication/ui/viewmodel/LoginViewModel.kt)
- Mejora en la gestión del estado `LoginUiState`.
- Implementación de una función para limpiar mensajes de error.
- Mejora en el parseo de errores de Retrofit (HttpException).

## Verification Plan

### Automated Tests
- No se requieren pruebas automatizadas nuevas en este paso, se verificará mediante compilación.

### Manual Verification
- Ejecutar la aplicación.
- Probar login con credenciales inválidas (verificar mensaje de error).
- Probar login con campos vacíos (verificar validación local).
- Probar login exitoso (verificar redirección a la pantalla de Admin).
