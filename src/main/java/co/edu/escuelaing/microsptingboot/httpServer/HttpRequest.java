/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.escuelaing.microsptingboot.httpServer;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sebastian.cardona-p
 */
public class HttpRequest {

    URI reuestUri = null;
    HttpRequest(URI requestUri) {
        reuestUri = requestUri;
    }

    
    /**
     * Obtiene el valor de un parámetro de consulta por su nombre.
     * Si el parámetro no existe, retorna una cadena vacía.
     *
     * @param paramName Nombre del parámetro a buscar
     * @return Valor del parámetro o cadena vacía si no existe
     */
    public String getValue(String paramName) {
        String query = reuestUri.getQuery();
        if (query == null) {
            return "";
        }
        String[] queryParams = query.split("&");
        Map<String, String> queryMap = new HashMap<>();
        for (String param : queryParams) {
            String[] nameValue = param.split("=");
            if (nameValue.length == 2) {
                queryMap.put(nameValue[0], nameValue[1]);
            } else if (nameValue.length == 1) {
                queryMap.put(nameValue[0], "");
            }
        }

        return queryMap.get(paramName) != null ? queryMap.get(paramName) : "";
    }
}
