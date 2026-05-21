# Backend Test Checklist

## Authentication

- [ ] Login correcto devuelve 200.
- [ ] Login correcto devuelve cookie `access_token`.
- [ ] Login correcto no devuelve token en JSON.
- [ ] Login incorrecto devuelve 401.
- [ ] `/api/auth/me` sin cookie devuelve 401.
- [ ] `/api/auth/me` con cookie válida devuelve usuario autenticado.
- [ ] Logout limpia cookie.

## Projects

- [ ] ADMIN puede crear proyecto.
- [ ] ADMIN puede listar proyectos de su organización.
- [ ] ADMIN puede ver proyecto por ID de su organización.
- [ ] ADMIN puede editar proyecto.
- [ ] ADMIN puede eliminar proyecto.
- [ ] ADMIN puede asignar auditor a proyecto.
- [ ] AUDITOR solo lista proyectos asignados.
- [ ] AUDITOR puede ver proyecto asignado.
- [ ] AUDITOR no puede crear proyecto.
- [ ] AUDITOR no puede editar proyecto.
- [ ] AUDITOR no puede ver proyecto no asignado.

## Findings

- [ ] ADMIN puede crear finding en proyecto de su organización.
- [ ] ADMIN puede listar findings de su organización.
- [ ] ADMIN puede filtrar findings por severidad.
- [ ] ADMIN puede filtrar findings por estado.
- [ ] ADMIN puede filtrar findings por proyecto.
- [ ] ADMIN puede ver finding por ID.
- [ ] ADMIN puede editar finding.
- [ ] ADMIN puede eliminar finding.
- [ ] AUDITOR puede crear finding en proyecto asignado.
- [ ] AUDITOR no puede crear finding en proyecto no asignado.
- [ ] AUDITOR solo ve findings de proyectos asignados.

## Security

- [ ] Endpoint protegido sin sesión devuelve 401.
- [ ] Usuario autenticado sin permiso devuelve 403.
- [ ] Recurso inexistente devuelve 404.
- [ ] Recurso de otra organización no debe revelar existencia.
- [ ] No se guarda JWT en frontend.
- [ ] JWT viaja solo en cookie HTTPOnly.