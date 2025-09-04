# MicroSpringBoot - Taller 4 AREP: Virtualización y Despliegue en AWS

## Descripción

Este taller demuestra el despliegue de un microframework web en AWS usando Docker y virtualización. MicroSpringBoot es un servidor HTTP concurrente desarrollado en Java que implementa capacidades de Inversión de Control (IoC), capaz de servir páginas HTML, archivos estáticos (CSS, JS, imágenes PNG/JPEG/ICO) y proporcionar un framework para construir aplicaciones web RESTful a partir de POJOs usando anotaciones.

El objetivo principal es aprender sobre **virtualización, contenedorización con Docker y despliegue en la nube (AWS)**.

### Características Principales

- **Servidor HTTP Concurrente**: Maneja múltiples solicitudes simultáneas usando un pool de hilos
- **Framework IoC**: Carga automática de componentes web usando reflexión
- **Soporte para Archivos Estáticos**: Sirve HTML, CSS, JavaScript e imágenes
- **API RESTful**: Endpoints configurables usando anotaciones
- **Configuración por Anotaciones**: @RestController, @GetMapping, @RequestParam
- **Descubrimiento Automático**: Escaneo automático del classpath para encontrar controladores
- **Arquitectura Escalable**: Diseño modular y extensible
- **Cloud-ready**: Fácil despliegue en plataformas de nube
- **Contenerización con Docker**: Empaquetado en contenedores para portabilidad

## Arquitectura

### Componentes Principales

1. **HttpServer**: Servidor HTTP principal con manejo concurrente de peticiones
2. **MicroSptingBoot**: Clase principal que inicializa el framework
3. **Controladores**: Componentes anotados con @RestController que definen endpoints
4. **Sistema de Anotaciones**: Framework de anotaciones para configuración declarativa
5. **Manejo de Recursos**: Sistema para servir archivos estáticos

### Scaffolding del Proyecto

```
microSptingBoot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── co/edu/escuelaing/microsptingboot/
│   │   │   │   ├── MicroSptingBoot.java           # Clase principal
│   │   │   │   ├── annotations/                   # Sistema de anotaciones
│   │   │   │   │   ├── RestController.java
│   │   │   │   │   ├── GetMapping.java
│   │   │   │   │   └── RequestParam.java
│   │   │   │   ├── controller/                    # Controladores de ejemplo
│   │   │   │   │   ├── GreetingController.java
│   │   │   │   │   └── CalcuteController.java
│   │   │   │   └── httpServer/                    # Servidor HTTP
│   │   │   │       ├── HttpServer.java
│   │   │   │       ├── HttpRequest.java
│   │   │   │       └── HttpResponse.java
│   │   │   └── resources/                         # Archivos estáticos
│   │   │       ├── index.html
│   │   │       ├── images/
│   │   │       ├── scripts/
│   │   │       └── styles/
│   │   └── test/                                  # Pruebas unitarias e integración
│   │       └── java/...
├── target/                                        # Archivos compilados
├── Dockerfile                                     # Configuración Docker
├── pom.xml                                        # Configuración Maven
└── README.md
```

## Servicios REST Disponibles

### GreetingController

- **GET /app/greeting?name={nombre}**: Saludo personalizado
  - Parámetro: `name` (default: "World")
  - Ejemplo: `/app/greeting?name=Juan` → "Hola Juan"

- **GET /app/hello?name={nombre}&age={edad}**: Saludo con edad
  - Parámetros: `name` (default: "World"), `age` (default: "0")
  - Ejemplo: `/app/hello?name=Ana&age=25` → "Hola hola Ana, tienes 25 años"

- **GET /app/status**: Estado del servidor
  - Sin parámetros
  - Ejemplo: `/app/status` → "El servidor está funcionando correctamente"

- **GET /app/welcome?name={nombre}&age={edad}&city={ciudad}**: Bienvenida completa
  - Parámetros: `name` (default: "Usuario"), `age` (default: "0"), `city` (default: "Ciudad Desconocida")
  - Ejemplo: `/app/welcome?name=Carlos&age=30&city=Bogotá` → "Bienvenido Carlos, tienes 30 años y vives en Bogotá"

### CalcuteController

- **GET /app/calculate/suma?a={num1}&b={num2}**: Suma de números
  - Parámetros: `a` (default: "0"), `b` (default: "0")
  - Ejemplo: `/app/calculate/suma?a=5&b=3` → "La suma de 5 + 3 = 8"

- **GET /app/calculate/resta?a={num1}&b={num2}**: Resta de números
  - Parámetros: `a` (default: "0"), `b` (default: "0")
  - Ejemplo: `/app/calculate/resta?a=10&b=3` → "La resta de 10 - 3 = 7"

## Cómo Ejecutar el Programa
###  Clonar el Repositorio

```bash
git clone https://github.com/SebastianCardona-P/AREP-TALLER4.git
cd AREP-TALLER4
```

### Opción 1: Usando Maven (Recomendado)

```bash

# Solo compilar
mvn clean compile

# Ejecutar con clase principal específica
mvn exec:java -Dexec.mainClass="co.edu.escuelaing.microsptingboot.MicroSptingBoot"
```

### Opción 2: Usando Java directamente

```bash
# Compilar primero
mvn clean compile

# Ejecutar desde target/classes
java -cp .\target\classes\ co.edu.escuelaing.microsptingboot.MicroSptingBoot

```

### Opción 3: Con Controlador Específico

```bash
# Cargar solo un controlador específico
java -cp target/classes co.edu.escuelaing.microsptingboot.MicroSptingBoot co.edu.escuelaing.microsptingboot.controller.GreetingController
```

### Opción 4: Docker

```bash
# Construir imagen, este comando leerá el Dockerfile en el directorio actual y creará una imagen llamada microspringboot
docker build -t areptaller4 .

# Ejecutar contenedor
docker run -p 6000:6000 areptaller4

# Acceder al servidor
http://localhost:6000
```
### Despliegue en la Nube

Ya que la aplicación está contenida en un contenedor Docker, puede ser desplegada en cualquier plataforma que soporte Docker, como AWS, Google Cloud, Azure, Heroku, entre otros.
Para este ejemplo se mostrará como desplegarlo en AWS EC2
1. Crear un repositorio en Docker Hub y subir la imagen creada
```bash
docker login
docker tag areptaller4 your_dockerhub_username/areptaller4
docker push your_dockerhub_username/areptaller4
```
![imagesEnLocal.png](img%2FimagesEnLocal.png)

2. Crear una instancia EC2 en AWS
Debes asegurarte de cque cuando creas la instacia, instales docker:
```bash
sudo yum update -y
sudo yum install docker
sudo service docker start
sudo usermod -a -G docker ec2-user
```
3. Configurar el grupo de seguridad para permitir el tráfico en el puerto 6000 o el puerto que hayas configurado o por el que quieras exponer el servicio
![puertosEntrada.png](img%2FpuertosEntrada.png)
4. Conectarse a la instancia vía SSH y correr el contenedor
```bash
docker run -p 35000:6000 your_dockerhub_username/areptaller4
```
5. Acceder a la aplicación vía navegador o Postman usando la IP pública de la instancia y el puerto configurado
```http://your_ec2_public_ip:6000
```
![dns.png](img%2Fdns.png)

![pruebaFuncional1.png](img%2FpruebaFuncional1.png)
![pruebaFuncional2.png](img%2FpruebaFuncional2.png)
![pruebaFuncional3.png](img%2FpruebaFuncional3.png)
![pruebaFuncional4.png](img%2FpruebaFuncional4.png)
![pruebaFuncional5.png](img%2FpruebaFuncional5.png)


### Video demostrativo de despligue en youtube
[![Despliegue en AWS](http://img.youtube.com/vi/1mX1Hjv2n1o/0.jpg)](http://www.youtube.com/watch?v=1mX1Hjv2n1o "Despliegue en AWS")
## Configuración

### Variables de Entorno

- **PORT**: Puerto del servidor (default: 35000)
- **MAX_THREADS**: Número máximo de hilos concurrentes (default: 10)

```bash
# Configurar puerto personalizado
export PORT=8080
java -cp target/classes co.edu.escuelaing.microsptingboot.MicroSptingBoot

# O usando -D
java -DPORT=8080 -cp target/classes co.edu.escuelaing.microsptingboot.MicroSptingBoot
```

## Pruebas

### Ejecutar Todas las Pruebas

```bash
mvn test
```

### Pruebas Implementadas

#### Pruebas Unitarias
- **HttpServerTest**: Pruebas del servidor HTTP incluyendo:
  - Carga automática y específica de controladores
  - Lectura de archivos estáticos
  - Configuración de directorios de recursos
  - Copia de archivos estáticos
  - Verificación de existencia de archivos
  - Configuración de puertos

- **GreetingControllerTest**: Pruebas de todos los métodos del controlador de saludos
- **CalcuteControllerTest**: Pruebas de calculadora incluyendo casos edge y validación de errores
- **HttpRequestTest**: Pruebas de parsing de parámetros HTTP
- **HttpResponseTest**: Pruebas de la clase de respuesta HTTP
- **AnnotationsTest**: Pruebas del sistema de anotaciones

#### Pruebas de Integración
- **MicroSptingBootIntegrationTest**: Pruebas de integración completa incluyendo:
  - Inicialización del sistema completo
  - Funcionamiento concurrente
  - Thread safety
  - Escalabilidad

### Cobertura de Pruebas

Las pruebas cubren:
- ✅ Lectura de archivos estáticos en HttpServer
- ✅ Todos los métodos de los controladores
- ✅ Sistema de anotaciones
- ✅ Manejo concurrente de peticiones
- ✅ Configuración y inicialización
- ✅ Casos edge y manejo de errores

## Uso para Desarrolladores Externos

### Crear un Nuevo Controlador

```java
@RestController
public class MiControlador {
    
    @GetMapping("/mi-endpoint")
    public static String miMetodo(@RequestParam(value = "param", defaultValue = "default") String param) {
        return "Mi respuesta: " + param;
    }
}
```

### Pasos para Integrar

1. **Crear clase controlador** anotada con `@RestController`
2. **Anotar métodos** con `@GetMapping("/ruta")`
3. **Usar @RequestParam** para parámetros de consulta
4. **Compilar el proyecto** con `mvn compile`
5. **Ejecutar** - el framework descubrirá automáticamente el controlador


### Estructura de Respuesta

- Los métodos deben ser `public static`
- Retornar `String` con la respuesta
- Usar `@RequestParam` para parámetros opcionales con valores por defecto

## Concurrencia

El servidor implementa concurrencia usando:

- **ThreadPoolExecutor**: Pool de hilos fijo configurable
- **ClientHandler**: Clase interna para manejar cada cliente en hilo separado
- **Shutdown Graceful**: Cierre ordenado del pool de hilos
- **Thread Safety**: Acceso seguro a recursos compartidos

### Características de Concurrencia

- Manejo de múltiples peticiones simultáneas
- Pool de hilos configurable (default: 10 hilos)
- Aislamiento de errores por cliente
- Logging por hilo para debugging
- Gestión automática de recursos

## Notas Técnicas

- **Java 21**: Versión mínima requerida
- **Maven**: Sistema de construcción
- **JUnit 5**: Framework de pruebas
- **Reflexión**: Para descubrimiento automático de componentes
- **Socket Programming**: Para comunicación HTTP de bajo nivel

## Limitaciones Actuales

- Solo soporta métodos GET
- Respuestas en formato String únicamente
- No soporta middleware personalizable
- Configuración limitada de CORS
- No incluye sistema de autenticación

---

**Autor**: Sebastian
**Curso**: AREP - Taller 3
**Universidad**: Escuela Colombiana de Ingeniería Julio Garavito

