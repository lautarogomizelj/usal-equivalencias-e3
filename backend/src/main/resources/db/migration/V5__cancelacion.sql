-- CC-010: el aspirante puede cancelar su solicitud antes de enviar la documentación (RF-ASP-010, transición 27).

INSERT INTO estados (codigo, nombre, descripcion, es_terminal) VALUES
  ('SOL-CANC', 'Solicitud cancelada', 'El aspirante canceló la solicitud antes de enviar la documentación; se termina el proceso', TRUE);
