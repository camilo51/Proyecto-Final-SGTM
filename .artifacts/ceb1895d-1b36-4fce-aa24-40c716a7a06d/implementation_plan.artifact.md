# Plan de estandarización de formularios para Empleados y Usuarios

Este plan detalla la reestructuración de las pantallas de creación/edición de empleados y usuarios para que coincidan con la estructura visual solicitada (basada en el formulario de inventario).

## Cambios Propuestos

### Componentes Comunes

#### [NEW] [CommonComponents.kt](file:///C:/Users/Alexi/AndroidStudioProjects/MyApplication/Proyecto-Final-SGTM/app/src/main/java/com/example/myapplication/ui/screens/CommonComponents.kt)
- Crear un archivo para componentes UI reutilizables.
- Mover/Crear `BackLink` (basado en `InventoryBackLink`) para que pueda ser usado en todo el proyecto.

#### [MODIFY] [InventoryComponents.kt](file:///C:/Users/Alexi/AndroidStudioProjects/MyApplication/Proyecto-Final-SGTM/app/src/main/java/com/example/myapplication/ui/screens/inventory/InventoryComponents.kt)
- Hacer `InventoryBackLink` público o referenciar el nuevo `BackLink` común para evitar duplicación.

---

### Módulo de Empleados

#### [MODIFY] [EmployeeFormScreen.kt](file:///C:/Users/Alexi/AndroidStudioProjects/MyApplication/Proyecto-Final-SGTM/app/src/main/java/com/example/myapplication/ui/screens/employees/EmployeeFormScreen.kt)
- Adoptar `AppScaffold` para la cabecera estandarizada.
- Implementar la estructura:
  1. `BackLink` ("← Volver").
  2. Título principal: "Registrar empleado".
  3. Texto explicativo: "Complete los datos básicos del nuevo integrante del equipo.".
  4. Campos de formulario corregidos (Código/Documento, Nombre, Apellido, Correo, Teléfono, Especialidad).
- Corregir el error actual donde los campos tenían etiquetas de inventario (Marca, Categoría, etc.).

---

### Módulo de Usuarios

#### [MODIFY] [UsersFormScreen.kt](file:///C:/Users/Alexi/AndroidStudioProjects/MyApplication/Proyecto-Final-SGTM/app/src/main/java/com/example/myapplication/ui/screens/users/UsersFormScreen.kt)
- Adoptar `AppScaffold`.
- Implementar la estructura:
  1. `BackLink` ("← Volver").
  2. Título principal: "Registrar usuario".
  3. Texto explicativo: "Defina las credenciales y el rol de acceso al sistema.".
  4. Campos de formulario (Usuario, Correo, Contraseña, Confirmar contraseña, Rol, Estado).

## Plan de Verificación

### Verificación Manual
- Desplegar la aplicación y navegar a "Empleados" -> "+ Nuevo empleado". Verificar que la estructura coincida con la imagen.
- Navegar a "Usuarios" -> "+ Nuevo usuario". Verificar la estructura.
- Probar el enlace "← Volver" en ambos formularios.
- Asegurarse de que el tema oscuro y los acentos naranjas se mantengan.
