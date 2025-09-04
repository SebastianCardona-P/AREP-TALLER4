package co.edu.escuelaing.microsptingboot.controller;

import co.edu.escuelaing.microsptingboot.annotations.GetMapping;
import co.edu.escuelaing.microsptingboot.annotations.RequestParam;
import co.edu.escuelaing.microsptingboot.annotations.RestController;

@RestController
public class CalcuteController {

    @GetMapping("/calculate/suma")
    public static String calculate(
            @RequestParam(value = "a", defaultValue = "0") String a,
            @RequestParam(value = "b", defaultValue = "0") String b
    ) {
        try {
            int numA = Integer.parseInt(a);
            int numB = Integer.parseInt(b);
            int sum = numA + numB;
            return "La suma de " + a + " + " + b + " = " + sum;
        } catch (NumberFormatException e) {
            return "Error: Los parámetros deben ser números válidos";
        }
    }

    @GetMapping("/calculate/resta")
    public static String resta(
            @RequestParam(value = "a", defaultValue = "0") String a,
            @RequestParam(value = "b", defaultValue = "0") String b
    ) {
        try {
            int numA = Integer.parseInt(a);
            int numB = Integer.parseInt(b);
            int res = numA - numB;
            return "La resta de " + a + " - " + b + " = " + res;
        } catch (NumberFormatException e) {
            return "Error: Los parámetros deben ser números válidos";
        }
    }

}
