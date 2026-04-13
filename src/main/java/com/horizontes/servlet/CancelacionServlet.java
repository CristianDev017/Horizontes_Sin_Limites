
package com.horizontes.servlet;

import com.horizontes.dao.CancelacionDAO;
import com.horizontes.model.Cancelacion;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/cancelaciones/*")
public class CancelacionServlet extends HttpServlet {

    private final CancelacionDAO dao = new CancelacionDAO();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!tieneAcceso(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String inicio = req.getParameter("inicio");
        String fin    = req.getParameter("fin");
        JsonUtil.sendJson(res, 200, dao.listarPorFechas(inicio, fin));
    }

    // POST /api/cancelaciones
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAtencionOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String body = new String(req.getInputStream().readAllBytes());
        com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(body).getAsJsonObject();
        int reservacionId = json.get("reservacionId").getAsInt();

        Cancelacion c = dao.procesarCancelacion(reservacionId);

        if (c == null) {
            JsonUtil.sendError(res, 400,
                    "No se puede cancelar. Verifique el estado y la fecha de viaje (minimo 7 dias)");
        } else {
            JsonUtil.sendJson(res, 201, c);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    private boolean tieneAcceso(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        return session.getAttribute("usuario") != null;
    }

    private boolean esAtencionOAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && (u.getTipo() == 1 || u.getTipo() == 3);
    }
}