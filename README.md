# FiaoControl

FiaoControl es una plataforma para colmados que ayuda a registrar fiados, abonos y balances de clientes de forma simple.

El objetivo del proyecto es digitalizar una operacion que normalmente se lleva en libreta, pero con una experiencia pensada para uso real en mostrador: rapida, clara y amigable para usuarios no tecnicos.

## Que estoy desarrollando

Actualmente el desarrollo esta enfocado en:

- Registro y autenticacion por colmado (Supabase Auth).
- Gestion de clientes y sus balances.
- Registro de movimientos (`fiao` y `abono`) con historial.
- Envio de estado por WhatsApp con detalle de deuda.
- Dashboard y estadisticas para apoyar decisiones de cobro.
- App Android como canal principal de uso diario.

## Estructura del repositorio

- `src/`, `public/`, `supabase/`: aplicacion web y backend/schema para MVP.
- `fiaocontrol-android/`: app Android principal (Kotlin + Jetpack Compose).

## Stack principal

- Frontend web: Next.js + TypeScript
- App movil: Kotlin + Jetpack Compose
- Backend/Auth/DB: Supabase (Postgres + Auth + REST)

## Como ejecutar el proyecto

### Web (Next.js)

```bash
npm install
npm run dev
```

Abre `http://localhost:3000`.

### Android (app principal)

1. Abre Android Studio en la carpeta `fiaocontrol-android/`.
2. Sincroniza Gradle.
3. Ejecuta en emulador o dispositivo.

Opcional por terminal:

```bash
cd fiaocontrol-android
./gradlew :app:assembleDebug
```

## Estado actual

Proyecto en fase MVP, iterando rapidamente sobre experiencia de colmadero, claridad de datos y flujo de cobro.

## Vision

Convertir FiaoControl en una herramienta diaria para colmados que permita:

- Cobrar mejor
- Reducir perdida por fiado desorganizado
- Tener control real del negocio con pocos toques
