-- Nombre y apellido en columnas separadas para aspirantes y usuarios (CC-012).
-- Lo ya cargado se parte en el primer espacio: la primera palabra queda como nombre.

ALTER TABLE aspirantes ADD COLUMN apellido VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN apellido VARCHAR(255);

UPDATE aspirantes
SET apellido = COALESCE(NULLIF(SUBSTRING(nombre FROM POSITION(' ' IN nombre) + 1), nombre), ''),
    nombre = SPLIT_PART(nombre, ' ', 1);

UPDATE usuarios
SET apellido = COALESCE(NULLIF(SUBSTRING(nombre FROM POSITION(' ' IN nombre) + 1), nombre), ''),
    nombre = SPLIT_PART(nombre, ' ', 1);

ALTER TABLE aspirantes ALTER COLUMN apellido SET NOT NULL;
ALTER TABLE usuarios ALTER COLUMN apellido SET NOT NULL;
