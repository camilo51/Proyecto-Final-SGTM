# Módulo Android de Inventario y Repuestos

## Estructura

El módulo está separado de la UI y usa el flujo `Composable → ViewModel → Repository → Retrofit`.

- `data/api/inventory/InventoryApiService.kt`: contratos HTTP del inventario.
- `data/model/common`: respuestas genéricas paginadas, errores y resultados de red.
- `data/model/inventory`: DTOs Gson, solicitudes y objetos de integración.
- `data/repository/inventory`: interfaz, implementación Retrofit, selector reutilizable y señal de recarga.
- `ui/viewmodel/inventory`: listado, formulario, detalle y movimientos.
- `ui/screens/inventory`: listado, detalle, alta, edición, historial y diálogos de stock.

## URL y autenticación

El módulo usa el `RetrofitClient` existente del proyecto y conserva su URL original: `https://enginesjds.onrender.com/api/`. Las pantallas no contienen URLs ni llamadas HTTP.

La autenticación se mantiene en el flujo de login ya existente. El repositorio devuelve el código y mensaje del backend para que los ViewModels muestren errores de permisos, sesión o conflicto sin crear una capa adicional de sesión global.

## Endpoints conectados

- `GET /inventory`: listado paginado con `search`, `category`, `brand`, `status`, `sort`, `page` y `limit`.
- `GET /inventory/categories`, `GET /inventory/brands` y `GET /inventory/alerts`.
- `GET /inventory/{id}` y `GET /inventory/{id}/movements`.
- `POST /inventory`, `PUT /inventory/{id}` y `DELETE /inventory/{id}`.
- `POST /inventory/{id}/entry`, `/output` y `/adjustment`.

Las respuestas paginadas siempre se representan con `PaginatedApiResponse<T>` y `PaginationDto`, nunca como una lista directa.

## Reglas de negocio

- El precio de compra y venta usan `BigDecimal`; no se calcula dinero con `Double`.
- El precio de venta debe ser mayor o igual que el precio de compra.
- Código opcional: 3–20 caracteres alfanuméricos o guiones; se envía en mayúsculas.
- Categorías: se obtienen del endpoint; no existe una lista local hardcodeada.
- El estado no se envía en POST/PUT. Solo se muestra el estado calculado por el backend.
- Entradas y salidas requieren cantidad positiva; el ajuste acepta cero y significa cantidad absoluta.
- La salida se bloquea localmente si excede el stock visible, pero el backend sigue siendo la autoridad.
- Borrado: exige motivo y acepta `204 No Content` sin deserializar un cuerpo. Los conflictos, por ejemplo por historial, muestran el mensaje del servidor.
- Todas las mutaciones esperan la respuesta del servidor y luego notifican al listado, detalle y alertas para recargar.

## Alertas

Las alertas se consultan desde el backend y se muestran como resumen y en el detalle. El endpoint real devuelve `401` sin una sesión válida, por lo que el DTO se limita al envoltorio documentado (`inventory_id`, `item`, `status`, `message`) y debe validarse con una sesión de Administrador antes de ampliar campos.

## Permisos y navegación

El rol obtenido en el login habilita Inventario solo para Administrador. El drawer no muestra la opción a otros roles y las rutas `inventory` mantienen la verificación antes de mostrar el contenido. El cierre de sesión existente limpia la pila de navegación.

## Integración con otros módulos

`InventorySelector.searchAvailableParts` devuelve referencias paginadas con `id`, código, nombre, marca, precio de venta, cantidad y estado. Órdenes debe usar ese selector y enviar únicamente `inventory_id` y `quantity` a su endpoint `add-part`; no debe descontar stock desde Android.

Compras y Proveedores pueden reutilizar `InventoryRepository` para registrar entradas confirmadas. Reportes puede consumir `getMovements` y `getAlerts`; los DTOs mantienen las cantidades antes/después y el delta para auditoría. Clientes y Motocicletas no requieren acoplamiento directo con este módulo.
