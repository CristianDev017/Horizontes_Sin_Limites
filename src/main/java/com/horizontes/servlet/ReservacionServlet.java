
package com.horizontes.servlet;

import com.horizontes.dao.ClienteDAO;
import com.horizontes.dao.ReservacionDAO;
import com.horizontes.model.Cliente;
import com.horizontes.model.Reservacion;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

@WebServlet("/api/reservaciones/*")
public class ReservacionServlet extends HttpServlet {

    private final ReservacionDAO dao       = new ReservacionDAO();
    private final ClienteDAO clienteDAO    = new ClienteDAO();

    // GET /api/reservaciones
    // GET /api/reservaciones/{id}
    // GET /api/reservaciones/numero/{numero}
    // GET /api/reservaciones/cliente/{dpi}
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAtencionOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            JsonUtil.sendJson(res, 200, dao.listarDelDia());
            return;
        }

        String[] partes = path.split("/");

        if (partes.length == 2) {
            // /api/reservaciones/{id}
            try {
                int id = Integer.parseInt(partes[1]);
                Reservacion r = dao.buscarPorId(id);
                if (r == null) JsonUtil.sendError(res, 404, "Reservacion no encontrada");
                else JsonUtil.sendJson(res, 200, r);
            } catch (NumberFormatException e) {
                JsonUtil.sendError(res, 400, "ID invalido");
            }
        } else if (partes.length == 3 && partes[1].equals("numero")) {
            // /api/reservaciones/numero/{numero}
            Reservacion r = dao.buscarPorNumero(partes[2]);
            if (r == null) JsonUtil.sendError(res, 404, "Reservacion no encontrada");
            else JsonUtil.sendJson(res, 200, r);
        } else if (partes.length == 3 && partes[1].equals("cliente")) {
            // /api/reservaciones/cliente/{dpi}
            JsonUtil.sendJson(res, 200, dao.listarPorCliente(partes[2]));
        } else {
            JsonUtil.sendError(res, 400, "Ruta invalida");
        }
    }

    // POST /api/reservaciones
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAtencionOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String body = new String(req.getInputStream().readAllBytes());
        ReservacionRequest data = JsonUtil.getGson().fromJson(body, ReservacionRequest.class);

        if (data.paqueteId == 0 || data.fechaViaje == null || data.fechaViaje.isBlank()) {
            JsonUtil.sendError(res, 400, "Paquete y fecha de viaje son requeridos"); return;
        }
        if (data.pasajeros == null || data.pasajeros.isEmpty()) {
            JsonUtil.sendError(res, 400, "Debe agregar al menos un pasajero"); return;
        }

        List<Cliente> clientes = new ArrayList<>();
        for (String dpi : data.pasajeros) {
            Cliente c = clienteDAO.buscarPorDpi(dpi);
            if (c == null) {
                JsonUtil.sendError(res, 404, "Pasajero no encontrado: " + dpi); return;
            }
            clientes.add(c);
        }

        try {
            Reservacion r = new Reservacion();
            r.setPaqueteId(data.paqueteId);
            r.setAgenteId(data.agenteId);
            r.setCostoTotal(data.costoTotal);
            r.setPasajeros(clientes);

            // Parsear fecha dd/MM/yyyy
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date parsed = sdf.parse(data.fechaViaje);
            r.setFechaViaje(new java.sql.Date(parsed.getTime()));

            Reservacion creada = dao.insertar(r);
            if (creada == null) {
                JsonUtil.sendError(res, 500, "Error al crear reservacion"); return;
            }
            JsonUtil.sendJson(res, 201, creada);

        } catch (Exception e) {
            JsonUtil.sendError(res, 400, "Fecha invalida, use formato dd/MM/yyyy");
        }
    }

    // PUT /api/reservaciones/{id}
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAtencionOAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            JsonUtil.sendError(res, 400, "ID requerido"); return;
        }

        int id = Integer.parseInt(path.substring(1));
        String body = new String(req.getInputStream().readAllBytes());
        EstadoRequest estado = JsonUtil.getGson().fromJson(body, EstadoRequest.class);

        boolean ok = dao.cambiarEstado(id, estado.estado);
        if (ok) JsonUtil.sendJson(res, 200, java.util.Map.of("mensaje", "Estado actualizado"));
        else JsonUtil.sendError(res, 500, "Error al actualizar estado");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    private boolean esAtencionOAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && (u.getTipo() == 1 || u.getTipo() == 3);
    }

    private static class ReservacionRequest {
        int paqueteId;
        int agenteId;
        String fechaViaje;
        java.math.BigDecimal costoTotal;
        List<String> pasajeros;
    }

    private static class EstadoRequest {
        String estado;
    }
}