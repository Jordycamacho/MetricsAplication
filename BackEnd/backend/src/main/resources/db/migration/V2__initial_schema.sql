-- 1. Agregar columna user_id (nullable temporalmente)
ALTER TABLE workout_sessions ADD COLUMN user_id BIGINT;

-- 2. Poblar user_id a partir del dueño de la rutina asociada
UPDATE workout_sessions ws
SET user_id = r.user_id
FROM routines r
WHERE ws.routine_id = r.id;

-- 3. Para sesiones huérfanas (sin routine_id válido), asignar usuario administrador (el primero que tenga rol ADMIN)
UPDATE workout_sessions
SET user_id = (SELECT id FROM users WHERE role = 'ADMIN' ORDER BY id LIMIT 1)
WHERE user_id IS NULL;

-- 4. Ahora hacer la columna obligatoria (NOT NULL)
ALTER TABLE workout_sessions ALTER COLUMN user_id SET NOT NULL;

-- 5. Agregar restricción de clave foránea
ALTER TABLE workout_sessions
    ADD CONSTRAINT fk_workout_sessions_user
    FOREIGN KEY (user_id) REFERENCES users(id);