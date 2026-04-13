
package com.horizontes.servlet;

import com.horizontes.dao.PaqueteDAO;
import com.horizontes.model.Paquete;
import com.horizontes.model.ServicioPaquete;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/paquetes/*")
public class PaqueteServlet extends HttpServlet {

    private final PaqueteDAO dao = new PaqueteDAO();

    // GET /api/paquetes
    // GET /api/paquetes/{id}
    // GET /api/paquetes?destino={id}
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!tieneAcceso(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        String destino = req.getParameter("destino");

        if (path == null || path.equals("/")) {
            if (destino != null) {
                List<Paquete> lista = dao.listarPorDestino(Integer.parseInt(destino));
                JsonUtil.sendJson(res, 200, lista);
            } else {
                JsonUtil.sendJson(res, 200, dao.listar());
            }
        } else {
            // GET /api/paquetes/{id}/servicios
            String[] partes = path.split("/");
            int id = Integer.parseInt(partes[1]);

            if (partes.length > 2 && partes[2].equals("servicios")) {
                JsonUtil.sendJson(res, 200, dao.listarServicios(id));
            } else {
                Paquete p = dao.buscarPorId(id);
                if (p == null) JsonUtil.sendError(res, 404, "Paquete no encontrado");
                else JsonUtil.sendJson(res, 200, p);
            }
        }
    }

    // POST /api/paquetes
    // POST /api/paquetes/{id}/servicios
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esOperacionesOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        String body = new String(req.getInputStream().readAllBytes());

        if (path != null && path.contains("/servicios")) {
            String[] partes = path.split("/");
            int paqueteId = Integer.parseInt(partes[1]);
            ServicioPaquete s = JsonUtil.getGson().fromJson(body, ServicioPaquete.class);
            s.setPaqueteId(paqueteId);
            boolean ok = dao.insertarServicio(s);
            if (ok) JsonUtil.sendJson(res, 201, s);
            else JsonUtil.sendError(res, 500, "Error al agregar servicio");
        } else {
            Paquete p = JsonUtil.getGson().fromJson(body, Paquete.class);
            if (p.getNombre() == null || p.getNombre().isBlank()) {
                JsonUtil.sendError(res, 400, "El nombre es requerido"); return;
            }
            if (p.getPrecioVenta() == null || p.getPrecioVenta().doubleValue() <= 0) {
                JsonUtil.sendError(res, 400, "El precio debe ser mayor a 0"); return;
            }
            if (dao.buscarPorNombre(p.getNombre()) != null) {
                JsonUtil.sendError(res, 409, "Ya existe un paquete con ese nombre"); return;
            }
            boolean ok = dao.insertar(p);
            if (ok) JsonUtil.sendJson(res, 201, p);
            else JsonUtil.sendError(res, 500, "Error al crear paquete");
        }
    }

    // PUT /api/paquetes/{id}
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esOperacionesOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendError(res, 400, "ID requerido"); return;
        }

        String body = new String(req.getInputStream().readAllBytes());
        Paquete p = JsonUtil.getGson().fromJson(body, Paquete.class);
        p.setId(Integer.parseInt(path.substring(1)));

        boolean ok = dao.actualizar(p);
        if (ok) JsonUtil.sendJson(res, 200, p);
        else JsonUtil.sendError(res, 500, "Error al actualizar paquete");
    }

    // DELETE /api/paquetes/{id}
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esOperacionesOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendError(res, 400, "ID requerido"); return;
        }
        int id = Integer.parseInt(path.substring(1));
        boolean ok = dao.desactivar(id);
        if (ok) JsonUtil.sendJson(res, 200, java.util.Map.of("mensaje", "Paquete desactivado"));
        else JsonUtil.sendError(res, 500, "Error al desactivar paquete");
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