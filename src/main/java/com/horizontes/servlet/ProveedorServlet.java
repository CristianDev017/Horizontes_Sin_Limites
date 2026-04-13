
package com.horizontes.servlet;

import com.horizontes.dao.ProveedorDAO;
import com.horizontes.model.Proveedor;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/proveedores/*")
public class ProveedorServlet extends HttpServlet {

    private final ProveedorDAO dao = new ProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!tieneAcceso(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendJson(res, 200, dao.listar());
        } else {
            int id = Integer.parseInt(path.substring(1));
            Proveedor p = dao.buscarPorId(id);
            if (p == null) JsonUtil.sendError(res, 404, "Proveedor no encontrado");
            else JsonUtil.sendJson(res, 200, p);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esOperacionesOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String body = new String(req.getInputStream().readAllBytes());
        Proveedor p = JsonUtil.getGson().fromJson(body, Proveedor.class);

        if (p.getNombre() == null || p.getNombre().isBlank()) {
            JsonUtil.sendError(res, 400, "El nombre es requerido"); return;
        }

        if (dao.buscarPorNombre(p.getNombre()) != null) {
            JsonUtil.sendError(res, 409, "Ya existe un proveedor con ese nombre"); return;
        }
        boolean ok = dao.insertar(p);
        if (ok) JsonUtil.sendJson(res, 201, p);
        else JsonUtil.sendError(res, 500, "Error al crear proveedor");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esOperacionesOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendError(res, 400, "ID requerido"); return;
        }

        String body = new String(req.getInputStream().readAllBytes());
        Proveedor p = JsonUtil.getGson().fromJson(body, Proveedor.class);
        p.setId(Integer.parseInt(path.substring(1)));

        boolean ok = dao.actualizar(p);
        if (ok) JsonUtil.sendJson(res, 200, p);
        else JsonUtil.sendError(res, 500, "Error al actualizar proveedor");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esOperacionesOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendError(res, 400, "ID requerido"); return;
        }
        int id = Integer.parseInt(path.substring(1));
        boolean ok = dao.eliminar(id);
        if (ok) JsonUtil.sendJson(res, 200, java.util.Map.of("mensaje", "Proveedor eliminado"));
        else JsonUtil.sendError(res, 500, "Error al eliminar proveedor");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    private boolean tieneAcceso(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        return session.getAttribute("usuario") != null;
    }

    private boolean esOperacionesOAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && (u.getTipo() == 2 || u.getTipo() == 3);
    }


}