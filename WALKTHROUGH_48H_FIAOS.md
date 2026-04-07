# Walkthrough 48H - Cuaderno Digital de Fiaos

## 1) Objetivo del sprint (2 dias)
Lanzar un MVP en produccion que permita:
- Registrar fiao en menos de 10 segundos.
- Registrar abono en menos de 10 segundos.
- Ver balance por cliente y balance total.
- Enviar constancia por WhatsApp con mensaje prellenado.

## 2) Definiciones clave
- MVP: Version minima vendible, con lo esencial que resuelve el dolor principal.
- Produccion: App publicada en internet con base de datos real, backups y monitoreo basico.
- Fiao: Movimiento de deuda a favor del colmado.
- Abono: Pago parcial o total que reduce deuda.
- Balance cliente: Suma de fiaos menos abonos de un cliente.
- Definition of Done (DoD): Lista de criterios obligatorios para considerar algo terminado.

## 3) Alcance cerrado (no negociar en este sprint)
Incluido:
- Login simple por negocio.
- CRUD de clientes.
- Registro de movimientos (fiao/abono).
- Balance por cliente.
- Historial por cliente.
- Envio por WhatsApp (click-to-chat con mensaje prellenado).
- Dashboard basico (deuda total + top deudores).

No incluido (fase 2):
- WhatsApp API oficial automatica.
- Multi-sucursal.
- Facturacion fiscal.
- Inventario completo.
- App nativa iOS/Android.

## 4) Stack recomendado (velocidad + estabilidad)
- Frontend: Next.js 14 + TypeScript + Tailwind.
- Backend: Next.js Route Handlers.
- DB/Auth: Supabase (Postgres + Auth).
- Deploy: Vercel.
- Errores y logs: Sentry basico.
- Analitica minima: PostHog o Vercel Analytics.

## 5) Arquitectura minima
- Tabla businesses
- Tabla users
- Tabla customers
- Tabla ledger_entries (fiao/abono)

Regla de balance:
- fiao suma.
- abono resta.
- balance = SUM(CASE WHEN type='fiao' THEN amount ELSE -amount END)

## 6) Modelo de datos inicial
### customers
- id (uuid)
- business_id (uuid)
- name (text)
- phone (text)
- alias (text, nullable)
- created_at (timestamp)

### ledger_entries
- id (uuid)
- business_id (uuid)
- customer_id (uuid)
- type (enum: fiao, abono)
- amount (numeric(10,2))
- note (text, nullable)
- created_by (uuid)
- created_at (timestamp)

## 7) Flujos UX obligatorios
### Flujo A: Registrar fiao
1. Buscar cliente.
2. Clic en "Fiao".
3. Escribir monto.
4. Guardar.
5. Boton "Enviar por WhatsApp".

### Flujo B: Registrar abono
1. Buscar cliente.
2. Clic en "Abono".
3. Escribir monto.
4. Guardar.
5. Mensaje opcional por WhatsApp.

### Flujo C: Cobro rapido
1. Abrir dashboard.
2. Ver top deudores.
3. Entrar a cliente.
4. Registrar abono.

## 8) Plantillas de WhatsApp (fase 1)
### Fiao
Hola {NOMBRE}, se registro un fiao de RD${MONTO} en {COLMADO}. Balance actual: RD${BALANCE}. Fecha: {FECHA}.

### Abono
Hola {NOMBRE}, se registro un abono de RD${MONTO} en {COLMADO}. Balance actual: RD${BALANCE}. Fecha: {FECHA}.

## 9) Seguridad minima
- Row Level Security por business_id.
- Validar monto > 0.
- Sanitizar entradas de texto.
- Rate limit basico en endpoints de escritura.

## 10) Plan operativo por horas
## Dia 1
### Bloque 1 (0-2h)
- Crear proyecto y repositorio.
- Configurar Supabase y tablas.
- Configurar variables de entorno.

Entregable: Login funcional + DB conectada.

### Bloque 2 (2-5h)
- CRUD clientes.
- Pantalla lista/busqueda.
- Validaciones basicas.

Entregable: Clientes listos para uso real.

### Bloque 3 (5-9h)
- Registrar fiao/abono.
- Historial por cliente.
- Calculo de balance.

Entregable: Flujo core completo.

### Bloque 4 (9-12h)
- Boton WhatsApp con mensaje prellenado.
- Formato RD$ consistente.
- Mejoras de UX movil.

Entregable: Flujo demostrable de punta a punta.

## Dia 2
### Bloque 5 (12-16h)
- Dashboard deuda total + top deudores.
- Ajustes de rendimiento en queries.

Entregable: Pantalla de cobro diario.

### Bloque 6 (16-20h)
- QA funcional con casos reales.
- Manejo de errores y vacios.
- Seed de demo.

Entregable: App estable para piloto.

### Bloque 7 (20-24h)
- Deploy Vercel.
- Dominio, HTTPS, monitoreo basico.
- Backups y checklist final.

Entregable: Produccion activa.

## 11) Checklist de Definition of Done
- Se puede crear cliente.
- Se puede registrar fiao.
- Se puede registrar abono.
- El balance cuadra matematicamente.
- WhatsApp abre con mensaje correcto.
- No hay errores criticos en consola.
- Build de produccion pasa.
- Backup configurado.
- 1 colmado piloto puede usarlo sin asistencia tecnica.

## 12) Casos de prueba minimos
- Cliente nuevo + fiao RD$450.
- Mismo cliente + abono RD$200.
- Balance esperado RD$250.
- Fiao con nota vacia.
- Intento de monto 0 (debe fallar).
- Telefono sin formato (se normaliza o alerta).

## 13) Riesgos de 48H y mitigacion
- Riesgo: Scope creep.
  - Mitigacion: No agregar features fuera del alcance cerrado.
- Riesgo: Problemas de WhatsApp API.
  - Mitigacion: usar click-to-chat en fase 1.
- Riesgo: Seguridad incompleta.
  - Mitigacion: RLS obligatorio antes de deploy.

## 14) Comercial inmediato
- Oferta piloto: 1 mes de acompanamiento + setup.
- Precio inicial sugerido: RD$699 a RD$999/mes.
- KPI principal del piloto: reduccion de deudas discutidas.

## 15) Ritmo de revision (para no perder el hilo)
Hacer corte de control cada 2 horas con 4 preguntas:
1. Que completamos?
2. Que bloqueo aparecio?
3. Que recortamos para proteger fecha?
4. Que sigue en el siguiente bloque?

Plantilla de reporte rapido:
- Estado: On track / Riesgo / Bloqueado
- Logro clave:
- Bloqueo:
- Decision:
- Proximo bloque:
