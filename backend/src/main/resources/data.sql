-- Datos de prueba iniciales para AgroControl
-- Se cargan automaticamente cada vez que se levanta la aplicacion (H2 en memoria)

INSERT INTO productores (nombres, apellidos, dni, telefono, email) VALUES
 ('Julio', 'Chunga Nima', '41234567', '969111222', 'julio.chunga@agro.pe'),
 ('Rosa', 'Farfan Silva', '45678912', '969333444', 'rosa.farfan@agro.pe');

INSERT INTO cultivos (nombre, variedad, ciclo_dias_estimado) VALUES
 ('Arroz', 'NIR-1', 130),
 ('Limon', 'Limon Sutil', 365),
 ('Mango', 'Kent', 365);

INSERT INTO parcelas (nombre, ubicacion, area_hectareas, productor_id) VALUES
 ('Parcela El Progreso', 'Valle del Chira, Piura', 3.5, 1),
 ('Parcela San Isidro', 'Tambogrande, Piura', 4.0, 1),
 ('Parcela Santa Rosa', 'Cieneguillo, Piura', 5.0, 2);

INSERT INTO campanas_agricolas (fecha_inicio, fecha_fin_estimada, estado, cultivo_id, parcela_id) VALUES
 ('2026-06-01', '2026-10-10', 'EN_CURSO', 1, 1),
 ('2026-05-15', '2027-05-15', 'PLANIFICADA', 2, 3);



-- ---------------- Datos agregados en APF2 ----------------
-- Campana finalizada (arroz en Parcela San Isidro) con sus cosechas
INSERT INTO campanas_agricolas (fecha_inicio, fecha_fin_estimada, estado, cultivo_id, parcela_id) VALUES
 ('2026-01-10', '2026-05-20', 'FINALIZADA', 1, 2);

INSERT INTO cosechas (fecha, cantidad_kg, observacion, campana_id) VALUES
 ('2026-05-10', 12500.0, 'Primera cosecha parcial', 3),
 ('2026-05-18', 9800.5, 'Cosecha final de la campana', 3);

-- Los roles (ADMIN, USER, GUEST) y los usuarios de prueba (admin, user, guest)
-- se crean con contrasenas cifradas (BCrypt) en config/DataInitializer.java
