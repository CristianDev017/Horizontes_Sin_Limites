
package com.horizontes.servlet;

import com.horizontes.dao.DestinoDAO;
import com.horizontes.model.Destino;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/destinos/*")
public class DestinoServlet extends HttpServlet {

    private final DestinoDAO dao = new DestinoDAO();

    // GET /api/destinos
    // GET /api/destinos/{id}
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!tieneAcceso(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendJson(res, 200, dao.listar());
        } else {
            int id = Integer.parseInt(path.substring(1));
            Destino d = dao.buscarPorId(id);
            if (d == null) JsonUtil.sendError(res, 404, "Destino no encontrado");
            else JsonUtil.sendJson(res, 200, d);
        }
    }

    // POST /api/destinos
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esOperacionesOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String body = new String(req.getInputStream().readAllBytes());
        Destino d = JsonUtil.getGson().fromJson(body, Destino.class);

        if (d.getNombre() == null || d.getNombre().isBlank()) {
            JsonUtil.sendError(res, 400, "El nombre es requerido"); return;
        }

        if (dao.buscarPorNombre(d.getNombre()) != null) {
            JsonUtil.sendError(res, 409, "Ya existe un destino con ese nombre"); return;
        }
        boolean ok = dao.insertar(d);
        if (ok) JsonUtil.sendJson(res, 201, d);
        else JsonUtil.sendError(res, 500, "Error al crear destino");
    }

    // PUT /api/destinos/{id}
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esOperacionesOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendError(res, 400, "ID requerido"); return;
        }

        String body = new String(req.getInputStream().readAllBytes());
        Destino d = JsonUtil.getGson().fromJson(body, Destino.class);
        d.setId(Integer.parseInt(path.substring(1)));

        boolean ok = dao.actualizar(d);
        if (ok) JsonUtil.sendJson(res, 200, d);
        else JsonUtil.sendError(res, 500, "Error al actualizar destino");
    }

    // DELETE /api/destinos/{id}
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esOperacionesOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendError(res, 400, "ID requerido"); return;
        }
        int id = Integer.parseInt(path.substring(1));
        boolean ok = dao.eliminar(id);
        if (ok) JsonUtil.sendJson(res, 200, java.util.Map.of("mensaje", "Destino eliminado"));
        else JsonUtil.sendError(res, 500, "Error al eliminar destino");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    private boolean tieneAcceso(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null;
    }

    private boolean esOperacionesOAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && (u.getTipo() == 2 || u.getTipo() == 3);
    }
}