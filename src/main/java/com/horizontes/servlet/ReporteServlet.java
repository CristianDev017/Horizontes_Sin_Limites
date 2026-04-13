
package com.horizontes.servlet;

import com.horizontes.model.Usuario;
import com.horizontes.util.DBConnection;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/api/reportes/*")
public class ReporteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (!esAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        String path   = req.getPathInfo();
        String inicio = req.getParameter("inicio");
        String fin    = req.getParameter("fin");

        if (path == null) { JsonUtil.sendError(res, 400, "Ruta invalida"); return; }

        switch (path) {
            case "/ventas"               -> JsonUtil.sendJson(res, 200, reporteVentas(inicio, fin));
            case "/cancelaciones"        -> JsonUtil.sendJson(res, 200, reporteCancelaciones(inicio, fin));
            case "/ganancias"            -> JsonUtil.sendJson(res, 200, reporteGanancias(inicio, fin));
            case "/agente-ventas"        -> JsonUtil.sendJson(res, 200, reporteAgenteVentas(inicio, fin));
            case "/agente-ganancias"     -> JsonUtil.sendJson(res, 200, reporteAgenteGanancias(inicio, fin));
            case "/paquete-mas-vendido"  -> JsonUtil.sendJson(res, 200, reportePaquete(inicio, fin, true));
            case "/paquete-menos-vendido"-> JsonUtil.sendJson(res, 200, reportePaquete(inicio, fin, false));
            case "/ocupacion-destino"    -> JsonUtil.sendJson(res, 200, reporteOcupacion(inicio, fin));
            default -> JsonUtil.sendError(res, 404, "Reporte no encontrado");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    // REPORTE VENTAS
    private List<Map<String, Object>> reporteVentas(String inicio, String fin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        boolean filtrar = tieneFechas(inicio, fin);
        String sql = """
                SELECT r.numero, r.fecha_viaje, r.costo_total, r.estado,
                       p.nombre AS paquete, u.nombre AS agente,
                       GROUP_CONCAT(c.nombre SEPARATOR ', ') AS pasajeros
                FROM reservacion r
                JOIN paquete p ON r.paquete_id = p.id
                JOIN usuario u ON r.agente_id = u.id
                JOIN reservacion_pasajero rp ON r.id = rp.reservacion_id
                JOIN cliente c ON rp.cliente_dpi = c.dpi
                WHERE r.estado = 'CONFIRMADA'
                """ + (filtrar ? "AND r.fecha_creacion BETWEEN ? AND ? " : "")
                + "GROUP BY r.id ORDER BY r.fecha_creacion DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filtrar) { ps.setString(1, inicio); ps.setString(2, fin); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("Numero",      rs.getString("numero"));
                fila.put("Paquete",     rs.getString("paquete"));
                fila.put("Agente",      rs.getString("agente"));
                fila.put("Pasajeros",   rs.getString("pasajeros"));
                fila.put("Fecha Viaje", rs.getString("fecha_viaje"));
                fila.put("Costo Total", rs.getBigDecimal("costo_total"));
                lista.add(fila);
            }
        } catch (SQLException e) { System.out.println("Error reporte ventas: " + e); }
        return lista;
    }

    // REPORTE CANCELACIONES
    private List<Map<String, Object>> reporteCancelaciones(String inicio, String fin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        boolean filtrar = tieneFechas(inicio, fin);
        String sql = """
                SELECT r.numero, c.fecha AS fecha_cancelacion,
                       c.monto_reembolso, c.perdida_agencia,
                       p.nombre AS paquete
                FROM cancelacion c
                JOIN reservacion r ON c.reservacion_id = r.id
                JOIN paquete p ON r.paquete_id = p.id
                """ + (filtrar ? "WHERE c.fecha BETWEEN ? AND ? " : "")
                + "ORDER BY c.fecha DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filtrar) { ps.setString(1, inicio); ps.setString(2, fin); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("Numero",             rs.getString("numero"));
                fila.put("Paquete",            rs.getString("paquete"));
                fila.put("Fecha Cancelacion",  rs.getString("fecha_cancelacion"));
                fila.put("Monto Reembolso",    rs.getBigDecimal("monto_reembolso"));
                fila.put("Perdida Agencia",    rs.getBigDecimal("perdida_agencia"));
                lista.add(fila);
            }
        } catch (SQLException e) { System.out.println("Error reporte cancelaciones: " + e); }
        return lista;
    }

    // REPORTE GANANCIAS
    private Map<String, Object> reporteGanancias(String inicio, String fin) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        boolean filtrar = tieneFechas(inicio, fin);

        String sqlVentas = """
                SELECT COALESCE(SUM(r.costo_total - sp_total.costo_paquete), 0) AS ganancia_bruta
                FROM reservacion r
                JOIN (
                    SELECT paquete_id, SUM(costo) AS costo_paquete
                    FROM servicio_paquete GROUP BY paquete_id
                ) sp_total ON r.paquete_id = sp_total.paquete_id
                WHERE r.estado = 'CONFIRMADA'
                """ + (filtrar ? "AND r.fecha_creacion BETWEEN ? AND ?" : "");

        String sqlReembolsos = "SELECT COALESCE(SUM(monto_reembolso), 0) AS total_reembolsos FROM cancelacion"
                + (filtrar ? " WHERE fecha BETWEEN ? AND ?" : "");

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sqlVentas)) {
                if (filtrar) { ps.setString(1, inicio); ps.setString(2, fin); }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) resultado.put("Ganancia Bruta", rs.getBigDecimal("ganancia_bruta"));
            }
            try (PreparedStatement ps = con.prepareStatement(sqlReembolsos)) {
                if (filtrar) { ps.setString(1, inicio); ps.setString(2, fin); }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    java.math.BigDecimal reembolsos = rs.getBigDecimal("total_reembolsos");
                    resultado.put("Total Reembolsos", reembolsos);
                    java.math.BigDecimal bruta = (java.math.BigDecimal) resultado.get("Ganancia Bruta");
                    resultado.put("Ganancia Neta", bruta.subtract(reembolsos));
                }
            }
        } catch (SQLException e) { System.out.println("Error reporte ganancias: " + e); }
        return resultado;
    }

    // REPORTE AGENTE MAS VENTAS
    private Map<String, Object> reporteAgenteVentas(String inicio, String fin) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        boolean filtrar = tieneFechas(inicio, fin);
        String sql = """
                SELECT u.nombre AS agente, COUNT(r.id) AS total_reservaciones,
                       SUM(r.costo_total) AS monto_total
                FROM reservacion r
                JOIN usuario u ON r.agente_id = u.id
                WHERE r.estado = 'CONFIRMADA'
                """ + (filtrar ? "AND r.fecha_creacion BETWEEN ? AND ? " : "")
                + "GROUP BY u.id ORDER BY total_reservaciones DESC LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filtrar) { ps.setString(1, inicio); ps.setString(2, fin); }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("Agente",               rs.getString("agente"));
                resultado.put("Total Reservaciones",  rs.getInt("total_reservaciones"));
                resultado.put("Monto Total",          rs.getBigDecimal("monto_total"));
            }
        } catch (SQLException e) { System.out.println("Error reporte agente ventas: " + e); }
        return resultado;
    }

    // REPORTE AGENTE MAS GANANCIAS
    private Map<String, Object> reporteAgenteGanancias(String inicio, String fin) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        boolean filtrar = tieneFechas(inicio, fin);
        String sql = """
                SELECT u.nombre AS agente,
                       SUM(r.costo_total - sp_total.costo_paquete) AS ganancia_total
                FROM reservacion r
                JOIN usuario u ON r.agente_id = u.id
                JOIN (
                    SELECT paquete_id, SUM(costo) AS costo_paquete
                    FROM servicio_paquete GROUP BY paquete_id
                ) sp_total ON r.paquete_id = sp_total.paquete_id
                WHERE r.estado = 'CONFIRMADA'
                """ + (filtrar ? "AND r.fecha_creacion BETWEEN ? AND ? " : "")
                + "GROUP BY u.id ORDER BY ganancia_total DESC LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filtrar) { ps.setString(1, inicio); ps.setString(2, fin); }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("Agente",         rs.getString("agente"));
                resultado.put("Ganancia Total", rs.getBigDecimal("ganancia_total"));
            }
        } catch (SQLException e) { System.out.println("Error reporte agente ganancias: " + e); }
        return resultado;
    }

    // REPORTE PAQUETE MAS/MENOS VENDIDO
    private Map<String, Object> reportePaquete(String inicio, String fin, boolean mas) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        boolean filtrar = tieneFechas(inicio, fin);
        String orden = mas ? "DESC" : "ASC";
        String sql = """
                SELECT p.nombre AS paquete, COUNT(r.id) AS total_ventas,
                       SUM(r.costo_total) AS monto_total
                FROM reservacion r
                JOIN paquete p ON r.paquete_id = p.id
                WHERE r.estado = 'CONFIRMADA'
                """ + (filtrar ? "AND r.fecha_creacion BETWEEN ? AND ? " : "")
                + "GROUP BY p.id ORDER BY total_ventas " + orden + " LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filtrar) { ps.setString(1, inicio); ps.setString(2, fin); }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                resultado.put("Paquete",      rs.getString("paquete"));
                resultado.put("Total Ventas", rs.getInt("total_ventas"));
                resultado.put("Monto Total",  rs.getBigDecimal("monto_total"));
            }
        } catch (SQLException e) { System.out.println("Error reporte paquete: " + e); }
        return resultado;
    }

    // REPORTE OCUPACION POR DESTINO
    private List<Map<String, Object>> reporteOcupacion(String inicio, String fin) {
        List<Map<String, Object>> lista = new ArrayList<>();
        boolean filtrar = tieneFechas(inicio, fin);
        String sql = """
                SELECT d.nombre AS destino, COUNT(r.id) AS total_reservaciones
                FROM reservacion r
                JOIN paquete p ON r.paquete_id = p.id
                JOIN destino d ON p.destino_id = d.id
                WHERE r.estado != 'CANCELADA'
                """ + (filtrar ? "AND r.fecha_creacion BETWEEN ? AND ? " : "")
                + "GROUP BY d.id ORDER BY total_reservaciones DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filtrar) { ps.setString(1, inicio); ps.setString(2, fin); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("Destino",              rs.getString("destino"));
                fila.put("Total Reservaciones",  rs.getInt("total_reservaciones"));
                lista.add(fila);
            }
        } catch (SQLException e) { System.out.println("Error reporte ocupacion: " + e); }
        return lista;
    }

    private boolean tieneFechas(String inicio, String fin) {
        return inicio != null && !inicio.isEmpty() && fin != null && !fin.isEmpty();
    }

    private boolean esAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && u.getTipo() == 3;
    }
}