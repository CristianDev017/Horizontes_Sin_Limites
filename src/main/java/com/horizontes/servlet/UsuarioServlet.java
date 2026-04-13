
package com.horizontes.servlet;

import com.horizontes.dao.UsuarioDAO;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/usuarios/*")
public class UsuarioServlet extends HttpServlet {

    private final UsuarioDAO dao = new UsuarioDAO();

    // GET /api/usuarios
    // GET /api/usuarios/{id}
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendJson(res, 200, dao.listar());
        } else {
            String nombre = path.substring(1);
            Usuario u = dao.buscarPorNombre(nombre);
            if (u == null) JsonUtil.sendError(res, 404, "Usuario no encontrado");
            else JsonUtil.sendJson(res, 200, u);
        }
    }

    // POST /api/usuarios
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String body = new String(req.getInputStream().readAllBytes());
        Usuario u = JsonUtil.getGson().fromJson(body, Usuario.class);

        if (u.getNombre() == null || u.getNombre().isBlank()) {
            JsonUtil.sendError(res, 400, "El nombre es requerido"); return;
        }
        if (u.getPassword() == null || u.getPassword().length() < 6) {
            JsonUtil.sendError(res, 400, "La contrasena debe tener al menos 6 caracteres"); return;
        }
        if (dao.buscarPorNombre(u.getNombre()) != null) {
            JsonUtil.sendError(res, 409, "Ya existe un usuario con ese nombre"); return;
        }

        boolean ok = dao.insertar(u);
        if (ok) JsonUtil.sendJson(res, 201, u);
        else JsonUtil.sendError(res, 500, "Error al crear usuario");
    }

    // PUT /api/usuarios/{id}
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String body = new String(req.getInputStream().readAllBytes());
        Usuario u = JsonUtil.getGson().fromJson(body, Usuario.class);

        String path = req.getPathInfo();
        if (path != null && !path.equals("/")) {
            u.setId(Integer.parseInt(path.substring(1)));
        }

        boolean ok = dao.actualizar(u);
        if (ok) JsonUtil.sendJson(res, 200, u);
        else JsonUtil.sendError(res, 500, "Error al actualizar usuario");
    }

    // DELETE /api/usuarios/{id}
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendError(res, 400, "ID requerido"); return;
        }
        int id = Integer.parseInt(path.substring(1));
        boolean ok = dao.desactivar(id);
        if (ok) JsonUtil.sendJson(res, 200, java.util.Map.of("mensaje", "Usuario desactivado"));
        else JsonUtil.sendError(res, 500, "Error al desactivar usuario");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    private boolean esAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && u.getTipo() == 3;
    }
}