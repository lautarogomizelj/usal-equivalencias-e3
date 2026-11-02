-- Catálogos precargados: roles (Requisitos v12 §5), estados (RF-ESTADO-001) y carreras (RF-ADMIN-002).

INSERT INTO roles (codigo, nombre, descripcion) VALUES
  ('ASPIRANTE', 'Aspirante', 'Estudiante que solicita equivalencias'),
  ('ACAD', 'Académico', 'Personal del área académica'),
  ('SECRE', 'Secretaría Administrativa', 'Personal de administración y secretaría académica'),
  ('ADMIN', 'Admin General', 'Administrador del sistema'),
  ('REVISOR', 'Revisor Logs', 'Personal de auditoría');

INSERT INTO estados (codigo, nombre, descripcion, es_terminal) VALUES
  ('SOL-INI', 'Solicitud iniciada', 'Aspirante acaba de crear la solicitud, no ha cargado documentación', FALSE),
  ('DOC-REV', 'Documentación pendiente de revisión', 'El aspirante envió la documentación y espera la revisión del Académico', FALSE),
  ('DOC-RECH', 'Documentación rechazada', 'Hay observaciones sobre la documentación; el sistema guarda cuáles documentos debe recargar el aspirante', FALSE),
  ('ANAL-PEND', 'Análisis preliminar pendiente', 'Documentación aceptada; el Académico debe armar la matriz de equivalencias, el posible horario de cursada y las fechas disponibles para la reunión', FALSE),
  ('REU-FEC-ASP-1', 'Reunión pendiente de fechas aspirante (1ª ronda)', 'El aspirante debe elegir una de las fechas propuestas por el Académico o solicitar otras', FALSE),
  ('REU-FEC-ASP-2', 'Reunión pendiente de fechas aspirante (2ª ronda)', 'El aspirante debe elegir una de las fechas propuestas o rechazar la 2ª ronda', FALSE),
  ('REU-FEC-ACAD', 'Reunión pendiente de fechas académico', 'El aspirante solicitó otras fechas o no se presentó a la reunión; el Académico debe sugerir nuevas fechas', FALSE),
  ('REU-CHAT', 'Resolución fecha por chat', 'El aspirante rechazó la 2ª ronda; aspirante y Académico pautan la fecha por chat', FALSE),
  ('REU-PEND', 'Reunión pendiente de realización', 'Fecha de reunión definida; falta realizar la reunión', FALSE),
  ('ANAL-POST', 'Análisis pos reunión', 'La reunión finalizó; espacio previo a la aceptación o rechazo de la inscripción', FALSE),
  ('INSC-ACEP', 'Inscripción aceptada', 'Análisis preliminar y equivalencias aceptados; el aspirante debe indicar si sus documentos están legalizados', FALSE),
  ('LEG-PEND', 'Legalización pendiente', 'El aspirante indicó que sus documentos no están legalizados; debe legalizarlos y avisar', FALSE),
  ('INSC-RECH', 'Inscripción rechazada', 'Análisis preliminar y equivalencias rechazados; se termina el proceso', TRUE),
  ('LEG-REV', 'Análisis de documentos legalizados', 'El aspirante indicó la legalidad de sus documentos; el Académico los analiza', FALSE),
  ('LEG-RECH', 'Documentos legalizados rechazados', 'Hay observaciones sobre los documentos legalizados; el sistema guarda cuáles debe recargar el aspirante', FALSE),
  ('CUPON-SOL', 'Solicitud de cupón de pago', 'Documentos legalizados aceptados; Secretaría debe enviar el cupón de pago', FALSE),
  ('CUPON-PAG', 'Pago de cupón por aspirante', 'Cupón enviado; esperando que el aspirante pague y cargue el comprobante', FALSE),
  ('PAG-CTRL', 'Control de pago', 'Comprobante cargado; Secretaría controla el pago', FALSE),
  ('PAG-REENV', 'Reenvío de comprobante', 'El pago fue rechazado; el aspirante debe reenviar el comprobante', FALSE),
  ('E3-INI', 'Inicio de E3', 'Pago aceptado; inicio de E3', FALSE);

INSERT INTO carreras (codigo, nombre, facultad) VALUES
  ('ING-INF-001', 'Ingeniería en Informática', 'Facultad de Ingeniería'),
  ('ING-IND-001', 'Ingeniería Industrial', 'Facultad de Ingeniería'),
  ('ING-CIV-001', 'Ingeniería Civil', 'Facultad de Ingeniería');
