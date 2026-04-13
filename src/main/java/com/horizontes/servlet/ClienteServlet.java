
package com.horizontes.servlet;

import com.horizontes.dao.ClienteDAO;
import com.horizontes.model.Cliente;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/clientes/*")
public class ClienteServlet extends HttpServlet {

    private final ClienteDAO dao = new ClienteDAO();

    // GET /api/clientes
    // GET /api/clientes/{dpi}
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!tieneAcceso(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendJson(res, 200, dao.listar());
        } else {
            String dpi = path.substring(1);
            Cliente c = dao.buscarPorDpi(dpi);
            if (c == null) JsonUtil.sendError(res, 404, "Cliente no encontrado");
            else JsonUtil.sendJson(res, 200, c);
        }
    }

    // POST /api/clientes
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!tieneAcceso(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String body = new String(req.getInputStream().readAllBytes());
        Cliente c = JsonUtil.getGson().fromJson(body, Cliente.class);

        if (c.getDpi() == null || c.getDpi().isBlank()) {
            JsonUtil.sendError(res, 400, "El DPI es requerido"); return;
        }
        if (dao.buscarPorDpi(c.getDpi()) != null) {
            JsonUtil.sendError(res, 409, "Ya existe un cliente con ese DPI"); return;
        }

        boolean ok = dao.insertar(c);
        if (ok) JsonUtil.sendJson(res, 201, c);
        else JsonUtil.sendError(res, 500, "Error al registrar cliente");
    }

    // PUT /api/clientes/{dpi}
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!tieneAcceso(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendError(res, 400, "DPI requerido"); return;
        }

        String body = new String(req.getInputStream().readAllBytes());
        Cliente c = JsonUtil.getGson().fromJson(body, Cliente.class);
        c.setDpi(path.substring(1));

        boolean ok = dao.actualizar(c);
        if (ok) JsonUtil.sendJson(res, 200, c);
        else JsonUtil.sendError(res, 500, "Error al actualizar cliente");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    private boolean tieneAcceso(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        // Tipo 1 = Atencion al Cliente, Tipo 3 = Administrador
        return u != null && (u.getTipo() == 1 || u.getTipo() == 3);
    }
}