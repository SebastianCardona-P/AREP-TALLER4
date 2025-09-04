package co.edu.escuelaing.microsptingboot.annotations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Pruebas unitarias para las anotaciones del framework
 *
 * Esta clase prueba:
 * - @RestController: Anotación para marcar controladores REST
 * - @GetMapping: Anotación para mapear endpoints GET
 * - @RequestParam: Anotación para parámetros de request
 */
public class AnnotationsTest {

    @Test
    void testRestControllerAnnotation() {
        // Verificar que la anotación @RestController existe y funciona
        assertTrue(RestController.class.isAnnotation(),
                  "@RestController debe ser una anotación");

        // Verificar que puede ser aplicada a clases
        assertTrue(RestController.class.isAnnotationPresent(java.lang.annotation.Target.class),
                  "@RestController debe tener @Target definido");
    }

    @Test
    void testGetMappingAnnotation() {
        // Verificar que la anotación @GetMapping existe y funciona
        assertTrue(GetMapping.class.isAnnotation(),
                  "@GetMapping debe ser una anotación");

        // Verificar que puede ser aplicada a métodos
        assertTrue(GetMapping.class.isAnnotationPresent(java.lang.annotation.Target.class),
                  "@GetMapping debe tener @Target definido");
    }

    @Test
    void testRequestParamAnnotation() {
        // Verificar que la anotación @RequestParam existe y funciona
        assertTrue(RequestParam.class.isAnnotation(),
                  "@RequestParam debe ser una anotación");

        // Verificar que puede ser aplicada a parámetros
        assertTrue(RequestParam.class.isAnnotationPresent(java.lang.annotation.Target.class),
                  "@RequestParam debe tener @Target definido");
    }

    @Test
    void testGetMappingValue() {
        // Crear una instancia mock de GetMapping para verificar el valor
        try {
            // Verificar que GetMapping tiene el método value()
            Method valueMethod = GetMapping.class.getMethod("value");
            assertNotNull(valueMethod, "@GetMapping debe tener método value()");
            assertEquals(String.class, valueMethod.getReturnType(),
                        "value() debe retornar String");
        } catch (NoSuchMethodException e) {
            fail("@GetMapping debe tener método value(): " + e.getMessage());
        }
    }

    @Test
    void testRequestParamProperties() {
        try {
            // Verificar que RequestParam tiene el método value()
            Method valueMethod = RequestParam.class.getMethod("value");
            assertNotNull(valueMethod, "@RequestParam debe tener método value()");
            assertEquals(String.class, valueMethod.getReturnType(),
                        "value() debe retornar String");

            // Verificar que RequestParam tiene el método defaultValue()
            Method defaultValueMethod = RequestParam.class.getMethod("defaultValue");
            assertNotNull(defaultValueMethod, "@RequestParam debe tener método defaultValue()");
            assertEquals(String.class, defaultValueMethod.getReturnType(),
                        "defaultValue() debe retornar String");
        } catch (NoSuchMethodException e) {
            fail("@RequestParam debe tener métodos value() y defaultValue(): " + e.getMessage());
        }
    }

    @Test
    void testAnnotationsRetentionPolicy() {
        // Verificar que las anotaciones tienen RetentionPolicy.RUNTIME
        assertTrue(RestController.class.isAnnotationPresent(java.lang.annotation.Retention.class),
                  "@RestController debe tener @Retention");

        assertTrue(GetMapping.class.isAnnotationPresent(java.lang.annotation.Retention.class),
                  "@GetMapping debe tener @Retention");

        assertTrue(RequestParam.class.isAnnotationPresent(java.lang.annotation.Retention.class),
                  "@RequestParam debe tener @Retention");
    }

    @Test
    void testAnnotationsPackage() {
        // Verificar que las anotaciones están en el paquete correcto
        assertEquals("co.edu.escuelaing.microsptingboot.annotations.RestController",
                    RestController.class.getName(),
                    "@RestController debe estar en el paquete correcto");

        assertEquals("co.edu.escuelaing.microsptingboot.annotations.GetMapping",
                    GetMapping.class.getName(),
                    "@GetMapping debe estar en el paquete correcto");

        assertEquals("co.edu.escuelaing.microsptingboot.annotations.RequestParam",
                    RequestParam.class.getName(),
                    "@RequestParam debe estar en el paquete correcto");
    }

    // Clase de prueba para verificar el uso de las anotaciones
    @RestController
    static class TestController {

        @GetMapping("/test")
        public static String testMethod(@RequestParam(value = "param", defaultValue = "default") String param) {
            return "test response";
        }

        @GetMapping("/noparams")
        public static String noParamsMethod() {
            return "no params response";
        }
    }

    @Test
    void testAnnotationsUsage() {
        // Verificar que las anotaciones se pueden usar correctamente
        assertTrue(TestController.class.isAnnotationPresent(RestController.class),
                  "TestController debe estar anotado con @RestController");

        try {
            Method testMethod = TestController.class.getMethod("testMethod", String.class);
            assertTrue(testMethod.isAnnotationPresent(GetMapping.class),
                      "testMethod debe estar anotado con @GetMapping");

            GetMapping getMapping = testMethod.getAnnotation(GetMapping.class);
            assertEquals("/test", getMapping.value(),
                        "@GetMapping debe tener el valor correcto");

        } catch (NoSuchMethodException e) {
            fail("No se pudo encontrar el método de prueba: " + e.getMessage());
        }
    }

    @Test
    void testRequestParamUsage() {
        // Verificar que @RequestParam se puede usar correctamente en parámetros
        try {
            Method testMethod = TestController.class.getMethod("testMethod", String.class);
            java.lang.reflect.Parameter[] parameters = testMethod.getParameters();

            assertEquals(1, parameters.length, "testMethod debe tener un parámetro");

            java.lang.reflect.Parameter param = parameters[0];
            assertTrue(param.isAnnotationPresent(RequestParam.class),
                      "El parámetro debe estar anotado con @RequestParam");

            RequestParam requestParam = param.getAnnotation(RequestParam.class);
            assertEquals("param", requestParam.value(),
                        "@RequestParam debe tener el valor correcto");
            assertEquals("default", requestParam.defaultValue(),
                        "@RequestParam debe tener el defaultValue correcto");

        } catch (NoSuchMethodException e) {
            fail("No se pudo encontrar el método de prueba: " + e.getMessage());
        }
    }
}
