# Control Sprint 48H

## Estado general
- Meta: MVP en produccion en 48 horas.
- Estado actual: En ejecucion (Bloque 1).

## Backlog ejecutable (orden estricto)
- [x] Inicializar Next.js + TypeScript + Tailwind
- [ ] Crear proyecto Supabase
- [x] Definir esquema SQL y migracion inicial
- [ ] Configurar Auth
- [ ] Implementar CRUD de clientes
- [ ] Implementar registro fiao/abono
- [ ] Implementar balance por cliente
- [ ] Implementar historial por cliente
- [ ] Implementar boton WhatsApp prellenado
- [ ] Implementar dashboard deuda total
- [ ] QA de casos minimos
- [ ] Deploy Vercel
- [ ] Verificacion post-deploy

## Bloque actual
- Objetivo del bloque: Dejar base tecnica lista para modelado de datos.
- Inicio: 05/04/2026
- Fin estimado: Hoy
- Resultado: Proyecto Next.js creado en /fiaocontrol con dependencias del MVP instaladas.

## Registro de decisiones
- Decision 001: Crear la app en subcarpeta fiaocontrol.
- Motivo: La carpeta raiz no cumple naming de npm por el espacio en el nombre.
- Impacto: El desarrollo continua sin bloqueo y el deploy sera directo desde esa subcarpeta.

- Decision 002: Usar WhatsApp click-to-chat en fase 1.
- Motivo: Reducir riesgo tecnico para cumplir ventana de 48 horas.
- Impacto: Entrega de valor inmediata; API oficial pasa a fase 2.

## Bloqueos
- Bloqueo:
- Severidad: baja/media/alta
- Accion:

## Corte cada 2 horas
### Corte 1
- Estado: On track
- Completado: Se creo el proyecto base y se instalaron librerias iniciales.
- Bloqueo: La carpeta raiz tiene espacios y no cumple naming de npm.
- Siguiente: Configurar Supabase, SQL base y primer flujo CRUD de clientes.

### Corte 2
- Estado: On track
- Completado: Esquema SQL MVP, utilidades WhatsApp y pantalla base de producto.
- Bloqueo: Ninguno critico.
- Siguiente: Crear proyecto Supabase, aplicar SQL e implementar CRUD de clientes.

### Corte 3
- Estado:
- Completado:
- Bloqueo:
- Siguiente:

### Corte 4
- Estado:
- Completado:
- Bloqueo:
- Siguiente:
