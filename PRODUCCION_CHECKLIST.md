# Checklist de Produccion - FiaoControl MVP

## Antes de deploy
- [ ] Variables de entorno completas en Vercel
- [ ] SQL aplicado en Supabase
- [ ] RLS habilitado
- [ ] Usuario admin del negocio creado
- [ ] Build local pasa
- [ ] No hay errores criticos en consola

## Validacion funcional
- [ ] Crear cliente
- [ ] Registrar fiao RD$450
- [ ] Registrar abono RD$200
- [ ] Balance final RD$250
- [ ] WhatsApp abre con mensaje correcto

## Seguridad minima
- [ ] Solo datos del negocio autenticado
- [ ] Validacion de monto > 0
- [ ] Endpoints bloquean payload invalido

## Post deploy (30 min)
- [ ] Smoke test en movil
- [ ] Smoke test en escritorio
- [ ] Probar con 1 negocio piloto
- [ ] Registrar feedback inmediato
