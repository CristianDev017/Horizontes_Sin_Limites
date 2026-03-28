
package com.horizontes.servlet;

import com.horizontes.dao.UsuarioDAO;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String body = new String(request.getInputStream().readAllBytes());
        com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();

        String nombre   = json.get("nombre").getAsString();
        String password = json.get("password").getAsString();

        Usuario usuario = usuarioDAO.login(nombre, password);

        if (usuario == null) {
            JsonUtil.sendError(response, 401, "Usuario o contraseña incorrectos");
            return;
        }

        // Guardar en sesion
        HttpSession session = request.getSession(true);
        session.setAttribute("usuario", usuario);
        session.setMaxInactiveInterval(60 * 60); // 1 hora

        Map<String, Object> datos = new HashMap<>();
        datos.put("id",     usuario.getId());
        datos.put("nombre", usuario.getNombre());
        datos.put("tipo",   usuario.getTipo());

        JsonUtil.sendJson(response, 200, datos);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(200);
    }
}
