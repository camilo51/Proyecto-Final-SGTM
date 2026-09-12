# Órdenes de trabajo Android

## Alcance

Este módulo usa únicamente el proyecto Android SGTM, sus modelos existentes y `ApiService`. No depende de la página web de ENGINES JDS.

## Clientes

La primera pantalla de Clientes reutiliza el mismo flujo de datos:

```text
ClientsScreen
      ↓
ClientViewModel
      ↓
ClientRepository
      ↓
ApiService
      ↓
RetrofitClient
```

La pantalla usa `GET /clients` y el alta usa el `POST /clients` ya existente. Ambos contratos se interpretan mediante `ApiResponse<T>` y el repositorio
extrae `data` antes de entregarlo al ViewModel. No se agregaron endpoints nuevos. La autenticación se mantiene centralizada en `RetrofitClient`, por lo que
el interceptor existente agrega el token Bearer cuando está disponible.

El modelo Android contiene `id`, `name`, `cedula`, `email` y `phone`. La pantalla muestra los valores disponibles y permite buscar localmente por
nombre, cédula, correo, teléfono o ID. El formulario de alta no marca ningún campo como obligatorio y deshabilita el botón mientras se procesa la
operación. No se agregó filtro por estado porque el modelo actual no contiene un campo de estado, ni se inventó una ciudad.

La UI llama `cedula` al número de identificación, pero Gson lo serializa como `document`, que es el nombre real del campo en el backend. La aplicación muestra el mensaje real del backend si
la API configurada aplica validaciones adicionales para el alta. Después de crear, se vuelve a consultar `GET /clients`; la respuesta del `POST` no se
inserta directamente para evitar agregar un cliente incompleto si la API devuelve solamente un acuse o una representación parcial.

La ruta `clients` está disponible desde el Drawer para administradores y conserva las rutas existentes. El módulo contempla carga inicial, refresco,
error con reintento, lista vacía y búsqueda sin resultados. La lista se pagina localmente en grupos de 10 clientes; la búsqueda reinicia la página en curso.
`ClientViewModelTest` verifica carga exitosa, error de conexión, filtrado local, paginación, creación y actualización.

La edición se abre desde el icono de editar de cada tarjeta y precarga `name`, `cedula`, `phone` y `email`. Guarda mediante `PUT /clients/{id}`,
deshabilita el formulario mientras procesa y vuelve a consultar `GET /clients` para mostrar la información confirmada por el servidor. Un correo inválido se
rechaza localmente y un HTTP 409 se presenta como conflicto de número de documento.

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

El cambio de estado se realiza mediante el `PUT /orders/{id}` existente, enviando una copia del `Order` con el `status` seleccionado. Los estados disponibles para el selector se obtienen de las órdenes cargadas; no se inventa una lista de estados.

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

El login conserva el access token en memoria mediante `AuthTokenStore`. `BearerAuthInterceptor` lo agrega a las rutas protegidas. El logging es `BASIC` en Debug y `NONE` en Release, con headers sensibles redactados.

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
