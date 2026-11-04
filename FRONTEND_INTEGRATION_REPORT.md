# Informe de integración frontend — pedidos y cesta

Este documento describe los cambios del backend realizados para el flujo de
compra y las reglas de producto que debe reflejar el frontend. Está dirigido a
la IA local encargada del desarrollo de la interfaz. No se han realizado
cambios en el frontend.

## Resumen funcional

El checkout crea un pedido `EN_PROCESO`, reserva el stock y vacía la cesta. La
simulación de pago se confirma con una segunda llamada: no hay pasarela ni
cobro real. Al completar ese paso el pedido pasa automáticamente a `PAGADO` y
aparece en la cola del vendedor; el vendedor no tiene que aceptar el pedido.
El vendedor marca sus líneas como enviadas y el cliente confirma la entrega.
Cuando todos los vendedores han enviado sus líneas, el pedido pasa a
`TRAMITADO_ENVIADO`; al confirmar la entrega queda `FINALIZADO`.

El carrito puede incluir productos de varios vendedores. Se conserva un pedido
único para el cliente, con las líneas asignadas a su vendedor. La respuesta
de la API para un vendedor incluye solo las líneas que le corresponden.

## Contratos de API

Todas estas operaciones requieren autenticación. La API obtiene la identidad
del usuario autenticado; no se debe enviar un `clienteId` o `vendedorId` en el
cuerpo de la petición. El rol cliente gestiona `/carrito` y las operaciones
de cliente de `/pedidos`; el rol vendedor accede a su cola y al envío.

### Cesta

| Método y ruta | Rol | Uso | Respuesta |
|---|---|---|---|
| `GET /carrito` | Cliente | Leer la cesta persistida | `200`, lista de artículos |
| `POST /carrito/{productoId}` | Cliente | Añadir una unidad; si ya existe, incrementa la cantidad | `204` |
| `PATCH /carrito/{productoId}` | Cliente | Establecer cantidad | `204`; cuerpo `{"cantidad": 2}` |
| `DELETE /carrito/{productoId}` | Cliente | Quitar un artículo | `204` |
| `DELETE /carrito` | Cliente | Descartar/vaciar la cesta | `204` |

Cada artículo de `GET /carrito` contiene `productoId`, `nombre`, `precioCent`,
`urlImagen` y `cantidad`. La cesta persiste al salir y volver a entrar. En el
checkout se vacía solo cuando la creación del pedido termina correctamente.

### Crear pedido y simular el pago

`POST /pedidos` recibe el método de pago guardado y una copia de la dirección
elegida:

```json
{
  "metodoPagoId": "id-del-metodo",
  "direccionEnvio": {
    "nombreCompleto": "Ana García",
    "direccion": "Calle Mayor 1",
    "ciudad": "Madrid",
    "provincia": "Madrid",
    "codigoPostal": "28001",
    "pais": "España",
    "telefono": "+34600000000"
  }
}
```

La respuesta es `201` con un objeto pedido. En ese momento:

1. Se comprueba disponibilidad y se reserva el stock.
2. Se crea el pedido con estado `EN_PROCESO` y caducidad a 30 minutos.
3. Se vacía la cesta.
4. El frontend debe presentar el paso de confirmación/simulación de pago.

Completar la simulación requiere `POST /pedidos/{pedidoId}/pagar` (sin cuerpo).
Devuelve `200` y estado `PAGADO`. Este paso hace visible el pedido en las
listas de los vendedores; no se integra con proveedor de pago alguno.

### Consulta, envío, cancelación y entrega

| Método y ruta | Rol | Uso |
|---|---|---|
| `GET /pedidos` | Cliente | Compatibilidad: todos los pedidos vinculados al cliente |
| `GET /pedidos/activos` | Cliente | Pedidos `EN_PROCESO`, `PAGADO`, `TRAMITADO_ENVIADO` y antiguos `RECIBIDO` |
| `GET /pedidos/historico` | Cliente | Pedidos `FINALIZADO` y `CANCELADO` |
| `GET /pedidos/{pedidoId}` | Cliente | Detalle de un pedido propio aún vinculado |
| `POST /pedidos/{pedidoId}/cancelar` | Cliente | Cancelar pedido elegible y devolver stock |
| `POST /pedidos/{pedidoId}/confirmar-entrega` | Cliente | Confirmar recepción y finalizar |
| `GET /pedidos/vendedor` | Vendedor | Cola activa del vendedor (`historico=false` por defecto) |
| `GET /pedidos/vendedor?historico=true` | Vendedor | Historial `FINALIZADO`/`CANCELADO` |
| `PATCH /pedidos/{pedidoId}/enviar` | Vendedor | Marcar como enviadas sus propias líneas |

La cola del vendedor contiene pedidos `PAGADO` y `TRAMITADO_ENVIADO`. Cada
respuesta se filtra para mostrar solo las líneas de ese vendedor. En una
compra con varios vendedores, el estado global solo pasa a
`TRAMITADO_ENVIADO` cuando todos han enviado sus líneas. Mientras tanto,
usar el booleano `enviado` de cada línea para mostrar el avance. Solo el
cliente puede confirmar entrega, y solo cuando todas las líneas estén
enviadas; entonces el estado pasa a `FINALIZADO`.

El cliente no puede cancelar cuando ya se ha enviado alguna línea. Se puede
cancelar un pedido `EN_PROCESO` o `PAGADO` antes del envío. Una cancelación
repone las unidades reservadas. Los pedidos `FINALIZADO` y `CANCELADO` son
históricos y no tienen acciones de edición.

## Estados y representación en la interfaz

El enum enviado por la API puede ser:

- `EN_PROCESO`: checkout creado; pago simulado todavía pendiente. No aparece
  en la cola ordinaria del vendedor.
- `PAGADO`: compra confirmada; pendiente de que los vendedores tramiten/envíen
  sus líneas. Aparece en la cola automáticamente.
- `TRAMITADO_ENVIADO`: todas las líneas de todos los vendedores se marcaron
  enviadas; el cliente puede confirmar entrega.
- `FINALIZADO`: entrega confirmada por el cliente; estado de solo consulta.
- `CANCELADO`: cancelado por el cliente o por expiración; estado de solo
  consulta.
- `RECIBIDO`: valor legado de pedidos creados por la versión anterior. Los
  nuevos pedidos no lo usan. Al consultar la cola de vendedor se intenta
  migrar esos pedidos a `PAGADO` y completar el vínculo de vendedor desde los
  productos existentes. El frontend debería tolerarlo como dato histórico
  inesperado.

## Campos de respuesta

`PedidoResponseDTO`:

| Campo | Significado |
|---|---|
| `id` | Identificador del pedido |
| `lineas` | Líneas compradas; en la vista vendedor, solo las suyas |
| `direccionEnvio` | Copia de la dirección de entrega |
| `metodoPagoId`, `tipoMetodoPago`, `aliasMetodoPago`, `referenciaMetodoPago` | Datos visibles del método; nunca el número completo de tarjeta |
| `totalCent` | Total en céntimos; en vista vendedor, subtotal de sus líneas |
| `estado` | Uno de los estados indicados arriba |
| `fechaPedido` | Fecha de creación |
| `nombreCliente` | Nombre conservado para identificar el pedido en la vista vendedor |

Cada elemento de `lineas` (`LineaPedidoResponseDTO`) expone `productoId`,
`nombre`, `referencia`, `urlImagen`, `enviado`, `precioUnitarioCent`,
`cantidad` y `subtotalCent`. Son datos guardados como copia al comprar. La
interfaz de pedidos debe renderizar esa copia y no depender de que el producto
siga publicado o exista en el catálogo.

## Reglas de borrado y conservación

- Al eliminarse un cliente, sus pedidos se conservan, `clienteId` se
  desvincula y `nombreCliente` queda guardado. Así los vendedores pueden
  seguir encontrándolos en su cola o histórico. El cliente eliminado ya no
  puede consultarlos desde sus endpoints.
- No se permite eliminar a un vendedor que tenga pedidos en curso (`EN_PROCESO`,
  `PAGADO` o `TRAMITADO_ENVIADO`; también se protege el legado `RECIBIDO`).
- Al eliminar a un vendedor sin pedidos activos, se eliminan sus productos
  del catálogo; las copias de las líneas de pedido permanecen.
- Un administrador no puede eliminarse desde `DELETE /users/me`. Su baja se
  realiza desde `DELETE /admin/{id}`, ejecutado por otro administrador. Esta
  ruta administrativa solo permite eliminar cuentas de administrador.

## Requisitos de producto enviados que afectan al frontend

- Al añadir dos veces el mismo artículo, la cesta muestra una sola línea con
  cantidad incrementada. En ella se puede cambiar cantidad o eliminar el
  artículo; también vaciar la cesta entera.
- El cliente selecciona una dirección y un método de pago guardados al iniciar
  el checkout. `CrearPedidoRequestDTO` requiere `metodoPagoId` y todos los
  campos de `direccionEnvio`.
- No se conecta a pagos reales: la pantalla puede representar el proceso de
  pago, pero la confirmación se refleja llamando al endpoint `/pagar`.
- El cliente siempre confirma la entrega en la experiencia prevista.
- Para productos ya comprados, mostrar en pedidos la información histórica de
  la línea, aunque el vendedor elimine el producto.
- Los pedidos en curso y el historial deben ser vistas separadas, tanto para
  cliente como para vendedor. El historial es solo consulta.
- Los artículos deben mostrarse con criterios prémium/normal según la
  especificación de catálogo; el endpoint de pedidos no cambia esa regla.
- Fuera del alcance de este cambio de pedidos: incidencias y configuración de
  plataforma. Valoraciones, favoritos/listas, gestión de direcciones y métodos
  de pago y reglas de catálogo prémium no se implementaron en esta modificación
  de backend; el frontend debe integrarlas con sus APIs existentes sin
  confundirlas con el ciclo del pedido.

## Comportamiento de errores que debe manejar la interfaz

- Un método de pago inexistente/no perteneciente al cliente o un pedido no
  propio/no encontrado puede producir `404`.
- Carrito vacío, datos incompletos o cantidad inválida producen error de
  validación (`400`).
- Falta de stock, pago fuera de plazo, transición no permitida, doble envío o
  cancelación después del envío producen conflicto (`409`).
- En particular, si el plazo termina durante el checkout, la llamada de pago
  puede responder conflicto. La interfaz debe actualizar el estado/listado y
  pedir al cliente que vuelva a revisar la cesta.

La expiración se ejecuta en el servidor mediante una tarea periódica de un
minuto: los 30 minutos son el plazo de negocio, pero la actualización automática
del listado puede demorarse hasta el siguiente ciclo. El backend también
comprueba el vencimiento cuando se intenta pagar.

## Cambios realizados en el backend

- **Ciclo del pedido:** enum de estados, fecha de expiración, nombre histórico
  de cliente, identidad de vendedor por línea e indicador `enviado`.
- **Checkout y stock:** instantánea de artículos/precios, reserva de stock,
  creación `EN_PROCESO`, vaciado condicionado del carrito y devolución del
  stock en cancelación o expiración.
- **Operaciones del ciclo:** endpoints para simular pago, cancelar, enviar por
  vendedor y confirmar entrega; separación de listas activas e históricas.
- **Carrito:** endpoint para vaciar la cesta completa; se mantiene el
  incremento de unidades cuando se añade un producto ya presente.
- **Borrado de cuentas:** se conservan pedidos de clientes desvinculándolos,
  se prohíbe borrar vendedores con pedidos activos y se borran sus productos
  al dar de baja vendedores sin pedidos activos. Se bloquea la autobaja de
  administrador.
- **Autorización:** roles cliente/vendedor separados para las rutas de pedido.
- **Compatibilidad:** los pedidos antiguos `RECIBIDO` se intentan migrar a
  `PAGADO` desde la cola del vendedor, recuperando el vendedor desde el
  producto existente.
- **Archivos principales:** `PedidoService`, `PedidoController`,
  `PedidoRepository`, `EstadoPedido`, `Pedido`, `LineaPedido`, DTOs de pedido,
  `CarritoService`, `CarritoController`, `UserService`, `SecurityConfig` y
  `EsiBuyApplication` (habilita el vencimiento periódico). Se actualizaron
  pruebas de servicio/controlador/seguridad y `README.md`.

## Pruebas y documentación

La implementación se validó con `mvnw verify`: 349 pruebas pasaron y el build
finalizó correctamente. Las rutas y reglas descritas también están resumidas
en la sección «Pedidos» del `README.md`.
