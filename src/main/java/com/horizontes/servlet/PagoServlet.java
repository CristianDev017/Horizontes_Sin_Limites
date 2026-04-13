package com.horizontes.servlet;

import com.horizontes.dao.PagoDAO;
import com.horizontes.dao.ReservacionDAO;
import com.horizontes.model.Pago;
import com.horizontes.model.Reservacion;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/pagos/*")
public class PagoServlet extends HttpServlet {

    private final PagoDAO pagoDAO         = new PagoDAO();
    private final ReservacionDAO resDAO   = new ReservacionDAO();

    // GET /api/pagos/reservacion/{id}
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAtencionOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null) { JsonUtil.sendError(res, 400, "Ruta invalida"); return; }

        String[] partes = path.split("/");
        if (partes.length >= 3 && partes[1].equals("reservacion")) {
            int reservacionId = Integer.parseInt(partes[2]);
            JsonUtil.sendJson(res, 200, pagoDAO.listarPorReservacion(reservacionId));
        } else {
            JsonUtil.sendError(res, 400, "Ruta invalida");
        }
    }

    // POST /api/pagos
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAtencionOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String body = new String(req.getInputStream().readAllBytes());
        Pago p = JsonUtil.getGson().fromJson(body, Pago.class);

        if (p.getMonto() == null || p.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            JsonUtil.sendError(res, 400, "El monto debe ser mayor a 0"); return;
        }

        Reservacion r = resDAO.buscarPorId(p.getReservacionId());
        if (r == null) {
            JsonUtil.sendError(res, 404, "Reservacion no encontrada"); return;
        }
        if (r.getEstado().equals("CANCELADA") || r.getEstado().equals("COMPLETADA")) {
            JsonUtil.sendError(res, 400, "No se puede pagar una reservacion " + r.getEstado()); return;
        }

        boolean ok = pagoDAO.insertar(p);
        if (!ok) { JsonUtil.sendError(res, 500, "Error al registrar pago"); return; }

        // Verificar si ya se pago el total
        BigDecimal totalPagado = pagoDAO.totalPagado(p.getReservacionId());
        if (totalPagado.compareTo(r.getCostoTotal()) >= 0) {
            resDAO.cambiarEstado(p.getReservacionId(), "CONFIRMADA");
            JsonUtil.sendJson(res, 201, java.util.Map.of(
                    "mensaje", "Pago registrado. Reservacion CONFIRMADA",
                    "confirmada", true,
                    "totalPagado", totalPagado
            ));
        } else {
            BigDecimal pendiente = r.getCostoTotal().subtract(totalPagado);
            JsonUtil.sendJson(res, 201, java.util.Map.of(
                    "mensaje", "Pago parcial registrado",
                    "confirmada", false,
                    "totalPagado", totalPagado,
                    "pendiente", pendiente
            ));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    private boolean esAtencionOAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && (u.getTipo() == 1 || u.getTipo() == 3);
    }
}