# Caché en Spring Boot: lectura masiva de registros y comparación de tiempos

**Materia:** ISWZ2202 – Diseño y Arquitectura de Software · Ingeniería de Software
**Objetivo:** reforzar Java y Spring Framework leyendo muchos registros de una base de datos, implementando caché y comparando los tiempos de acceso.

## Estructura

```
cache-demo-spring/
├── backend/    # Spring Boot 3 + Spring Data JPA + H2 + Caffeine (Java 17)
└── frontend/   # index.html (HTML + CSS + JS puro, sin build)
```

## Cómo funciona

1. Al arrancar por primera vez, `DataSeeder` inserta **200.000 productos** (10 categorías → ~20.000 registros por categoría) en una base H2 guardada en `backend/data/`.
2. `GET /api/products/no-cache?category=X` lee **todos** los productos de la categoría directamente de la BD.
3. `GET /api/products/cached?category=X` hace lo mismo, pero el método del servicio lleva `@Cacheable`: la primera vez va a la BD (*cache miss*) y las siguientes devuelven el resultado desde memoria (*cache hit*, Caffeine).
4. `GET /api/benchmark?category=X&runs=5` ejecuta N consultas sin caché y N con caché y devuelve todos los tiempos.
5. `DELETE /api/cache` vacía la caché.

El tiempo se mide en el servidor con `System.nanoTime()` alrededor de la llamada al servicio, y el front además muestra el tiempo total de ida y vuelta.

## Requisitos

- JDK 17 o superior
- Maven 3.9+ (o abrir `backend/` en IntelliJ IDEA / Eclipse / VS Code y ejecutar `CacheDemoApplication`)

## Ejecución

**1. Backend** (puerto 8080)

```bash
cd backend
mvn spring-boot:run
```

La primera vez tarda unos segundos extra mientras genera los datos (se ve en el log: *"Listo: 200000 registros insertados…"*).

**2. Frontend** (puerto 5500, un origen distinto al del backend a propósito)

```bash
cd frontend
python -m http.server 5500
```

Abrir <http://localhost:5500>. También sirve la extensión *Live Server* de VS Code (puerto 5500 por defecto).

> No abras `index.html` con doble clic (`file://`): el origen sería `null` y CORS lo rechazaría.

## CORS: el problema y cómo se resuelve

**Problema.** Un origen es la combinación `esquema + host + puerto`. El front en `http://localhost:5500` y el backend en `http://localhost:8080` son orígenes distintos, así que la *same-origin policy* del navegador bloquea la respuesta del backend y en la consola aparece algo como:

```
Access to fetch at 'http://localhost:8080/api/...' from origin 'http://localhost:5500'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present...
```

Con peticiones como `DELETE` el navegador además envía antes una petición **preflight** (`OPTIONS`) para preguntar si el método está permitido.

**Soluciones en desarrollo** (de la más usada a la menos):

| Opción | Cómo | Cuándo usarla |
|---|---|---|
| **Configuración global en Spring** (la usada aquí) | `CorsConfig` con `WebMvcConfigurer.addCorsMappings` y los orígenes en `application.properties` | Front y back separados; lo más claro y mantenible |
| `@CrossOrigin(origins = "...")` | Anotación en un controlador o método | Casos puntuales |
| Proxy del servidor de desarrollo | Vite: `server.proxy = { '/api': 'http://localhost:8080' }`; Angular: `proxy.conf.json`; el navegador ve un solo origen | Frontends con bundler (React, Vue, Angular) |
| Servir el front desde Spring | Copiar el HTML a `src/main/resources/static/` | Mismo origen, no hay CORS |

**Qué evitar:** `allowedOrigins("*")` en producción. Si se usan cookies o `Authorization` con credenciales, `*` ni siquiera es válido: hay que listar orígenes explícitos y `allowCredentials(true)`. En producción se suele poner un API gateway / reverse proxy (Nginx) para que front y API compartan origen.

## Resultados (completar con tus mediciones)

Ejecuta el benchmark en la interfaz (5 repeticiones, categoría `Electronica`) y anota los valores:

| Métrica | Sin caché | Con caché |
|---|---|---|
| Registros leídos | 20.000 | 20.000 |
| Corrida 1 | ___ ms | ___ ms (miss) |
| Corridas 2–5 (promedio) | ___ ms | ___ ms (hits) |
| Mejora (veces más rápido) | – | ___ × |

**Análisis sugerido:**
- El *cache miss* cuesta igual o un poco más que una consulta normal, porque además de leer la BD guarda el resultado.
- Los *hits* devuelven la lista que ya está en memoria, por eso el tiempo cae a una fracción de milisegundo.
- H2 está en el mismo equipo y es muy rápido; con un motor remoto (PostgreSQL/MySQL en otra máquina) la diferencia sería mucho mayor por la latencia de red.
- Riesgo de la caché: **datos obsoletos**. Aquí caducan a los 10 minutos (`expireAfterWrite=10m`) y pueden vaciarse con `DELETE /api/cache`; en un caso real habría que invalidarla (`@CacheEvict`) al insertar o modificar productos.
- Limitación: el proxy de Spring solo aplica `@Cacheable` a llamadas que entran desde otro bean (*self-invocation*).

## Subir a GitHub

```bash
cd cache-demo-spring
git init
git add .
git commit -m "Actividad: caché con Spring Boot y front de comparación"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/cache-demo-spring.git
git push -u origin main
```

(Antes crea el repositorio vacío `cache-demo-spring` en github.com.) Pega el enlace en la consigna.

## Prompt usado para generar el front con IA

> Genera un front en HTML, CSS y JavaScript puro que consuma mi API REST de Spring Boot en `http://localhost:8080`: `GET /api/categories`, `GET /api/products/no-cache?category=`, `GET /api/products/cached?category=`, `GET /api/benchmark?category=&runs=` y `DELETE /api/cache`. Debe permitir elegir categoría, mostrar el tiempo de servidor y el tiempo total de cada consulta, un historial, una vista previa de los registros y un gráfico de barras con el benchmark. Si falla por CORS, muestra un mensaje claro.
