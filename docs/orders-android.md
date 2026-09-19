# Órdenes de trabajo Android

## Alcance

Este módulo usa únicamente el proyecto Android SGTM, sus modelos existentes y `ApiService`. No depende de la página web de ENGINES JDS.

## Clientes

El módulo de Clientes usa el mismo flujo de datos y navegación completa para alta y edición:

```text
ClientsScreen
      ↓ navegación actual
ClientFormScreen
      ↓
ClientViewModel
      ↓
ClientRepository
      ↓
ApiService
      ↓
RetrofitClient
```

La pantalla usa `GET /clients`, `POST /clients`, `PUT /clients/{id}` y `DELETE /clients/{id}` ya existentes. Todos los contratos se interpretan mediante `ApiResponse<T>` y el repositorio extrae `data` antes de entregarlo al ViewModel. La autenticación se mantiene centralizada en `RetrofitClient`, por lo que el interceptor existente agrega el token Bearer cuando está disponible.

El contrato del backend también soporta `city`, `status` y `notes`, pero Android no los usa porque no son necesarios para este flujo. La aplicación envía únicamente `document_type`, `document`, `name`, `last_name` y `phone`. `document_type` admite exactamente `CC`, `CE`, `NIT` y `Pasaporte`; si se omite, el servicio usa `CC`.

El endpoint efectivo no acepta ni persiste `email` o `address` en el contrato seguro de clientes. Por eso el formulario no los muestra. La búsqueda local usa nombre, apellido, documento, teléfono, ciudad e ID. El campo visible es `Documento` y se serializa como `document`; el campo `Teléfono` se serializa como `phone`.

La pantalla completa `ClientFormScreen` conserva el estilo SGTM y el orden `Tipo de documento`, `Documento`, `Nombre`, `Apellido` y `Teléfono`. Crear y editar reutilizan el mismo formulario, con `Guardar cliente`/`Guardar cambios`, `Cancelar` y `← Volver`. Después de un POST o PUT exitoso se actualiza el listado con `GET /clients` y se regresa a Clientes.

La validación local evita enviar un teléfono colombiano inválido; el backend valida los campos que se diligencien. Los errores 400/422 muestran el detalle de campo cuando está disponible; 401, 403, 404, 409 y 5xx se traducen a mensajes de usuario. La eliminación conserva el borrado lógico del backend y solicita el motivo requerido por `DELETE /clients/{id}`.

La ruta `clients` está disponible desde el Drawer para administradores y conserva las rutas existentes. El módulo contempla carga inicial, refresco,
error con reintento, lista vacía y búsqueda sin resultados. La lista se pagina localmente en grupos de 10 clientes; la búsqueda reinicia la página en curso.
`ClientViewModelTest` verifica carga exitosa, error de conexión, filtrado local, paginación, creación y actualización.

La edición se abre desde el icono de editar de cada tarjeta y precarga los campos soportados por el backend. Guarda mediante `PUT /clients/{id}`, deshabilita el formulario mientras procesa y vuelve a consultar `GET /clients` para mostrar la información confirmada por el servidor. Un HTTP 409 se presenta como duplicado de documento.

## Arquitectura

```text
OrdersListScreen / CreateOrderScreen / OrderDetailScreen / EditOrderScreen
                              ↓
                         OrderViewModel
                              ↓
                       OrderRepository
                              ↓
                           ApiService
                              ↓
                       RetrofitClient
```

`OrderRepository` es la única capa entre `OrderViewModel` y `ApiService`; conserva las operaciones de listado, detalle, creación, actualización y eliminación. Las pruebas usan dobles de los repositorios existentes, sin una capa adicional de datos.

## Contrato actual de Android

`ApiService` respalda únicamente estas operaciones de órdenes:

- `GET /orders`
- `GET /orders/{id}`
- `POST /orders`
- `PUT /orders/{id}`
- `DELETE /orders/{id}`

El modelo Android `Order` sólo contiene `id`, `clientId`, `motorcycleId`, `description`, `status` y `total`. Por eso la UI muestra únicamente esos datos y no inventa número de orden, fechas, técnicos, servicios, repuestos, historial o pagos.

El cambio de estado se realiza desde el chip de la orden mediante el `PUT /orders/{id}` existente, enviando una copia del `Order` con el `status` seleccionado. Como `Motorcycle` también expone `status` y existe `PUT /motorcycles/{id}`, Android actualiza la motocicleta relacionada con su representación vigente para mantener ambos estados alineados. El selector conserva los estados que devuelve la API y muestra también los cuatro estados operativos ya definidos por el módulo de motocicletas.

No existen en el `ApiService` actual endpoints Android para:

- catálogo o detalle de servicios;
- repuestos o `order_items`;
- historial de estados;
- PDF;
- órdenes asignadas a técnicos;
- solicitudes de repuestos;
- asignación de técnico, porque `Order` no tiene ese campo.

Para soportarlos primero deben existir modelos y endpoints Android respaldados por el backend que se esté utilizando.

## Pantallas y navegación

Las rutas se centralizan en `ui/navigation/AppRoutes.kt`:

- `orders`
- `orders/create`
- `orders/{orderId}`
- `orders/{orderId}/edit`

`orders` se agregó al Drawer y está protegido visualmente para administradores, respetando las rutas heredadas `login`, `admin` y `home`.

## Lista y filtros

La lista carga `GET /orders` una vez al entrar. La búsqueda y el filtro por estado son locales y no generan una petición por cada carácter. La lista tiene estados de carga, error, vacío y búsqueda sin resultados, además de refresco manual.

Los nombres de cliente, marca, modelo y placa sólo se muestran cuando se encuentran en `ClientRepository` y `MotorcycleRepository`. Si no están disponibles, se muestran los IDs que sí existen en `Order`.

## Crear y editar

La creación requiere los campos que el modelo actual exige: cliente, motocicleta, descripción, estado y total. La descripción tiene máximo 2000 caracteres y el total debe ser numérico y no negativo.

La edición usa `PUT /orders/{id}` y sólo modifica campos que existen en `Order`. El total se muestra desde el modelo; Android no recalcula valores.

## Autenticación y logging

`RetrofitClient` conserva el access token únicamente en memoria y su interceptor interno lo agrega a las rutas protegidas. El logging es `BASIC` en Debug y `NONE` en Release, con headers sensibles redactados.

No se persisten JWT, contraseñas ni cookies. No se falsifican headers `Origin` o `Referer`.

## Pruebas

`OrderViewModelTest` cubre:

- carga exitosa;
- error de conexión;
- búsqueda local;
- filtro por estado;
- creación correcta;
- rechazo de datos inválidos.

Ejecutar desde la raíz:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```
