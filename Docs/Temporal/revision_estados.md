		Accion								Estado luego de la accion

(aspirante - crear solicitud)      	  - solicitud iniciada 
(aspirante - enviar documentacion)	  - documentacion pendiente de revision
(academico - rechaza)	              - documentacion rechazada
(aspirante - re-enviar documentacion) - documentacion pendiente de revision
(academico - acepta)	              - analisis preliminar pendiente 	  (puede subir matriz de equivalencia, armar posible horario de cursada)



(academico - sube documentos y sugiere fechas disponibles)		- reunion pendiente de fechas aspirante           (se le manda al aspirnate horarios disponibles del academico para reunion junto con documentos)
(aspirante - solicita otras fechas)								- reunion pendiente de fechas academico   		  (aspirante envia motivo y detalle de cuando puede reunirse)
(aspirante - seleccionar fechas)				  				- reunion pendiente de confirmacion por academico
(academico - acepta reunion)	  								- reunion pendiente de realizacion 				  (el academico incluye un enlace de meet para la reunion)	      	     

-----------
ACADEMICO ACEPTA REUNION -> REALIZAR REUNION -> DESPUES DE LA REUNION
	Primera situacion: Las matrices y horarios esten bien (no requieran modificacion)
	Segunda situacion: Las matrices y horarios no esten bien (requiran modificacion)

a) luego de la reunion, hace falta crear un estado pos-reunion para darle al academico un espacio previo a la aceptacion/rechazo de la inscripcion, en donde si hace falta, pueda re-subir documentos.
-----------


(academico - aceptar  analisis preliminar/equivalencia			- inscripcion aceptada
(academico - rechazar analisis preliminar/equivalencia)         - inscripcion rechazada





Verdades: 
1) Para coordinar reunion se necesita que el academico suba los dos documentos