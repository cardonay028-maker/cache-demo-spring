Caché con Java y Spring Boot

Actividad de ISWZ2202 - Diseño y Arquitectura de Software (Ingeniería de Software). Estudiante: Yair Cardona

La aplicación lee muchos registros de una base de datos, usa caché y compara el tiempo de acceso con y sin caché. El front lo generó una IA a partir del backend ya terminado.


Estructura
backend/   -> proyecto Spring Boot
frontend/  -> index.html
docs/      -> capturas de pantalla

Cómo ejecutarlo
Abrir la carpeta backend en IntelliJ y ejecutar CacheDemoApplication (o usar mvn spring-boot:run). La primera vez se generan 200.000 productos.
Comprobar en http://localhost:8080/api/categories que responde.

Abrir frontend/index.html con el servidor de IntelliJ, o desde la carpeta frontend ejecutar python -m http.server 5500 y entrar a http://localhost:5500.

Servicios
Servicio	Qué hace
GET /api/categories	Lista las categorías
GET /api/products/no-cache?category=X	Lee los productos de la categoría directo de la base de datos
GET /api/products/cached?category=X	Lo mismo, pero usando la caché
GET /api/benchmark?category=X&runs=N	Hace N consultas de cada tipo y devuelve los tiempos
DELETE /api/cache	Borra la caché

Cómo funciona la caché

Se activó con @EnableCaching y se puso @Cacheable en el método del servicio que busca por categoría. La primera consulta de una categoría va a la base de datos (miss) y las siguientes devuelven la lista guardada en memoria (hit). La caché expira a los 10 minutos.

CORS

El front y el backend están en puertos distintos, así que el navegador los trata como orígenes diferentes y bloquea la respuesta si el backend no la permite.

<img width="952" height="770" alt="Captura de pantalla 2026-10-14 182624" src="https://github.com/user-attachments/assets/a1208291-6bf6-4d93-9155-f6433d810f1b" />


Se resolvió con la clase CorsConfig, que permite los orígenes escritos en application.properties (app.cors.allowed-origins) para las rutas /api. Hubo que agregar el puerto 63342 (el de IntelliJ) y reiniciar el backend.

<img width="939" height="943" alt="Captura de pantalla 2026-10-14 182323" src="https://github.com/user-attachments/assets/6d02fad7-c0c1-4403-a45b-3db140301df8" />

Resultados

Prueba con la categoría Alimentos (20.000 registros) y 5 repeticiones:

	Promedio
Sin caché	57,4 ms
Con caché, primera consulta (miss)	57,8 ms
Con caché, siguientes consultas (hit)	0,06 ms

La caché fue unas 929 veces más rápida en el servidor.

<img width="939" height="943" alt="Captura de pantalla 2026-10-14 182323" src="https://github.com/user-attachments/assets/5a2fc6a8-1b5f-4814-b8c4-e81acb3ace10" />

<img width="900" height="552" alt="Captura de pantalla 2026-10-14 182338" src="https://github.com/user-attachments/assets/098f1e50-b056-4a3a-a16c-6cf1a6f750bf" />

<img width="898" height="924" alt="Captura de pantalla 2026-10-14 182356" src="https://github.com/user-attachments/assets/fb59f9e2-2201-472e-91ed-b1230a76f6c6" />


Conclusión

La caché reduce mucho el tiempo de lectura, pero usa memoria y los datos pueden quedar desactualizados si cambian en la base de datos. Sirve más para datos que se consultan seguido y casi no cambian.

Uso de IA

El front se generó con IA como pedía la actividad.
