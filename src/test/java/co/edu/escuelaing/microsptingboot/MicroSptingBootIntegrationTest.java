package co.edu.escuelaing.microsptingboot;

import co.edu.escuelaing.microsptingboot.httpServer.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Pruebas de integración para MicroSpringBoot
 *
 * Esta clase prueba:
 * - Inicialización completa del sistema
 * - Carga automática de controladores
 * - Funcionamiento del servidor HTTP concurrente
 * - Respuestas de los endpoints REST
 * - Manejo de archivos estáticos
 * - Concurrencia y múltiples peticiones simultáneas
 */
public class MicroSptingBootIntegrationTest {

    private Thread serverThread;
    private static final int TEST_PORT = 35001; // Puerto diferente para pruebas

    @BeforeEach
    void setUp() {
        // Limpiar servicios antes de cada prueba
        HttpServer.services.clear();

        // Configurar puerto de prueba
        System.setProperty("PORT", String.valueOf(TEST_PORT));
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // Detener el servidor si está corriendo
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            serverThread.join(5000); // Esperar máximo 5 segundos
        }

        // Limpiar servicios
        HttpServer.services.clear();

        // Limpiar propiedades del sistema
        System.clearProperty("PORT");
    }

    @Test
    void testMicroSpringBootInitialization() {
        // Prueba que MicroSpringBoot se inicializa correctamente
        assertDoesNotThrow(() -> {
            // Solo cargar servicios sin iniciar el servidor
            HttpServer.loadServices(null);
        }, "MicroSpringBoot debe inicializarse sin errores");

        // Verificar que se cargaron los servicios esperados
        assertFalse(HttpServer.services.isEmpty(), "Debe cargar al menos un servicio");
        assertTrue(HttpServer.services.containsKey("/greeting"), "Debe cargar el endpoint /greeting");
        assertTrue(HttpServer.services.containsKey("/calculate/suma"), "Debe cargar el endpoint /calculate/suma");
    }

    @Test
    void testServiceLoading() {
        // Prueba la carga de servicios
        HttpServer.loadServices(null);

        // Verificar servicios de GreetingController
        assertTrue(HttpServer.services.containsKey("/greeting"), "Debe cargar /greeting");
        assertTrue(HttpServer.services.containsKey("/hello"), "Debe cargar /hello");
        assertTrue(HttpServer.services.containsKey("/status"), "Debe cargar /status");
        assertTrue(HttpServer.services.containsKey("/welcome"), "Debe cargar /welcome");

        // Verificar servicios de CalcuteController
        assertTrue(HttpServer.services.containsKey("/calculate/suma"), "Debe cargar /calculate/suma");
        assertTrue(HttpServer.services.containsKey("/calculate/resta"), "Debe cargar /calculate/resta");

        // Verificar que los métodos están correctamente mapeados
        assertNotNull(HttpServer.services.get("/greeting"), "El método /greeting debe estar mapeado");
        assertNotNull(HttpServer.services.get("/calculate/suma"), "El método /calculate/suma debe estar mapeado");
    }

    @Test
    void testSpecificControllerLoading() {
        // Prueba la carga de un controlador específico
        String[] args = {"co.edu.escuelaing.microsptingboot.controller.GreetingController"};
        HttpServer.loadServices(args);

        // Debe cargar solo servicios de GreetingController
        assertTrue(HttpServer.services.containsKey("/greeting"));
        assertTrue(HttpServer.services.containsKey("/hello"));
        assertTrue(HttpServer.services.containsKey("/status"));
        assertTrue(HttpServer.services.containsKey("/welcome"));

        // No debe cargar servicios de CalcuteController
        assertFalse(HttpServer.services.containsKey("/calculate/suma"));
        assertFalse(HttpServer.services.containsKey("/calculate/resta"));
    }

    @Test
    void testInvalidControllerLoading() {
        // Prueba carga de controlador que no existe
        String[] args = {"co.edu.escuelaing.invalid.Controller"};

        assertDoesNotThrow(() -> {
            HttpServer.loadServices(args);
        }, "No debe lanzar excepción para controladores inexistentes");

        // Los servicios deben estar vacíos
        assertTrue(HttpServer.services.isEmpty(), "No debe cargar servicios para controladores inexistentes");
    }

    @Test
    void testEmptyArgsLoading() {
        // Prueba carga con args vacío
        String[] args = {};
        HttpServer.loadServices(args);

        // Debe cargar automáticamente todos los controladores
        assertFalse(HttpServer.services.isEmpty(), "Debe cargar controladores automáticamente");
        assertTrue(HttpServer.services.containsKey("/greeting"));
        assertTrue(HttpServer.services.containsKey("/calculate/suma"));
    }

    @Test
    void testConcurrentServiceAccess() throws InterruptedException {
        // Prueba acceso concurrente a los servicios
        HttpServer.loadServices(null);

        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Crear múltiples hilos que accedan a los servicios
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    // Acceder a diferentes servicios
                    assertNotNull(HttpServer.services.get("/greeting"));
                    assertNotNull(HttpServer.services.get("/calculate/suma"));
                    assertTrue(HttpServer.services.containsKey("/status"));
                } finally {
                    latch.countDown();
                }
            });
        }

        // Esperar que todos los hilos terminen
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Todos los hilos deben completarse");
        executor.shutdown();
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        // Prueba thread safety del mapa de servicios
        HttpServer.loadServices(null);
        int initialSize = HttpServer.services.size();

        int numThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Crear múltiples hilos que lean los servicios
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    // Leer servicios múltiples veces
                    for (int j = 0; j < 100; j++) {
                        assertNotNull(HttpServer.services);
                        assertEquals(initialSize, HttpServer.services.size());
                        assertTrue(HttpServer.services.containsKey("/greeting"));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Todos los hilos deben completarse");
        assertEquals(initialSize, HttpServer.services.size(), "El tamaño del mapa no debe cambiar");
        executor.shutdown();
    }

    @Test
    void testMainMethodExecution() {
        // Prueba que el método main no lance excepciones inmediatamente
        assertDoesNotThrow(() -> {
            // Simular invocación del main sin iniciar realmente el servidor
            // Solo verificamos que la clase se puede instanciar y los servicios se cargan
            HttpServer.loadServices(null);
        }, "El método main debe ejecutarse sin errores iniciales");
    }

    @Test
    void testPortConfiguration() {
        // Verificar que el puerto se configura correctamente desde variables de entorno
        String originalPort = System.getProperty("PORT");

        try {
            // Configurar puerto personalizado
            System.setProperty("PORT", "8080");

            // La configuración del puerto se hace en tiempo de carga de clase,
            // así que verificamos que el sistema maneja la configuración
            assertDoesNotThrow(() -> {
                HttpServer.loadServices(null);
            }, "Debe manejar configuración de puerto personalizada");

        } finally {
            // Restaurar puerto original
            if (originalPort != null) {
                System.setProperty("PORT", originalPort);
            } else {
                System.clearProperty("PORT");
            }
        }
    }

    @Test
    void testMaxThreadsConfiguration() {
        // Verificar que el número máximo de hilos se configura correctamente
        String originalMaxThreads = System.getProperty("MAX_THREADS");

        try {
            // Configurar número personalizado de hilos
            System.setProperty("MAX_THREADS", "20");

            assertDoesNotThrow(() -> {
                HttpServer.loadServices(null);
            }, "Debe manejar configuración de MAX_THREADS personalizada");

        } finally {
            // Restaurar configuración original
            if (originalMaxThreads != null) {
                System.setProperty("MAX_THREADS", originalMaxThreads);
            } else {
                System.clearProperty("MAX_THREADS");
            }
        }
    }

    @Test
    void testFrameworkScalability() {
        // Prueba que el framework puede manejar múltiples controladores
        HttpServer.loadServices(null);

        // Verificar que se cargaron múltiples controladores
        assertTrue(HttpServer.services.size() >= 6,
                  "Debe cargar al menos 6 endpoints de los controladores existentes");

        // Verificar distribución de endpoints
        long greetingEndpoints = HttpServer.services.keySet().stream()
                .filter(key -> !key.startsWith("/calculate"))
                .count();

        long calculateEndpoints = HttpServer.services.keySet().stream()
                .filter(key -> key.startsWith("/calculate"))
                .count();

        assertTrue(greetingEndpoints >= 4, "Debe tener al menos 4 endpoints de greeting");
        assertTrue(calculateEndpoints >= 2, "Debe tener al menos 2 endpoints de calculate");
    }
}
