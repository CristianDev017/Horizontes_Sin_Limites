
package com.horizontes.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("dd/MM/yyyy")
            .create();

    private JsonUtil() {}

    public static void sendJson(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }

    public static void sendError(HttpServletResponse response, int status, String mensaje) throws IOException {
        sendJson(response, status, new ErrorResponse(mensaje));
    }

    public static Gson getGson() {
        return gson;
    }

    private record ErrorResponse(String error) {}
}