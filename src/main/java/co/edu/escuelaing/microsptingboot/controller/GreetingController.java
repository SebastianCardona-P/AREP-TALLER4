package co.edu.escuelaing.microsptingboot.controller;

import co.edu.escuelaing.microsptingboot.annotations.GetMapping;
import co.edu.escuelaing.microsptingboot.annotations.RequestParam;
import co.edu.escuelaing.microsptingboot.annotations.RestController;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Sebastian
 */
@RestController
public class GreetingController {

    @GetMapping("/greeting")
    public static String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }

    @GetMapping("/hello")
    public static String helloService(@RequestParam(value = "name", defaultValue = "World") String name,
                                      @RequestParam(value = "age", defaultValue = "0") String age) {
        return "Hola hola " + name + ", tienes " + age + " años";
    }

    @GetMapping("/status")
    public static String status() {
        return "El servidor está funcionando correctamente";
    }


    @GetMapping("/welcome")
    public static String welcome(
            @RequestParam(value = "name", defaultValue = "Usuario") String name,
            @RequestParam(value = "age", defaultValue = "0") String age,
            @RequestParam(value = "city", defaultValue = "Ciudad Desconocida") String city
    ) {
        return "Bienvenido " + name + ", tienes " + age + " años y vives en " + city;
    }
}
