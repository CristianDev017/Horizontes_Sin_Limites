
package com.horizontes.dao;

import com.horizontes.model.Cancelacion;
import com.horizontes.util.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CancelacionDAO {

    private final PagoDAO pagoDAO = new PagoDAO();
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();

    public Cancelacion procesarCancelacion(int reservacionId) {
        com.horizontes.model.Reservacion res = reservacionDAO.buscarPorId(reservacionId);
        if (res == null) return null;

        // Validar estado
        String estado = res.getEstado();
        if (!estado.equals("PENDIENTE") && !estado.equals("CONFIRMADA")) return null;

        // Calcular dias hasta el viaje
        LocalDate hoy = LocalDate.now();
        LocalDate fechaViaje = res.getFechaViaje().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaViaje);

        // Validar minimo 7 dias
        if (diasRestantes < 7) return null;

        // Calcular porcentaje de reembolso
        BigDecimal porcentaje;
        if (diasRestantes > 30) {
            porcentaje = new BigDecimal("1.00");      // 100%
        } else if (diasRestantes >= 15) {
            porcentaje = new BigDecimal("0.70");      // 70%
        } else {
            porcentaje = new BigDecimal("0.40");      // 40%
        }

        // Calcular montos
        BigDecimal totalPagado = pagoDAO.totalPagado(reservacionId);
        BigDecimal montoReembolso = totalPagado.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
        BigDecimal perdidaAgencia = totalPagado.subtract(montoReembolso).setScale(2, RoundingMode.HALF_UP);

        // Insertar cancelacion
        String sql = """
                INSERT INTO cancelacion (reservacion_id, fecha, monto_reembolso, perdida_agencia)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservacionId);
            ps.setDate(2, java.sql.Date.valueOf(hoy));
            ps.setBigDecimal(3, montoReembolso);
            ps.setBigDecimal(4, perdidaAgencia);
            ps.executeUpdate();

            // Cambiar estado de la reservacion
            reservacionDAO.cambiarEstado(reservacionId, "CANCELADA");

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                Cancelacion c = new Cancelacion();
                c.setId(keys.getInt(1));
                c.setReservacionId(reservacionId);
                c.setFecha(java.sql.Date.valueOf(hoy));
                c.setMontoReembolso(montoReembolso);
                c.setPerdidaAgencia(perdidaAgencia);
                return c;
            }
        } catch (SQLException e) {
            System.out.println("Error procesar cancelacion: " + e);
        }
        return null;
    }

    public List<Cancelacion> listarPorFechas(String fechaInicio, String fechaFin) {
        List<Cancelacion> lista = new ArrayList<>();
        String sql;
        boolean filtrar = fechaInicio != null && !fechaInicio.isEmpty()
                && fechaFin   != null && !fechaFin.isEmpty();

        if (filtrar) {
            sql = "SELECT * FROM cancelacion WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC";
        } else {
            sql = "SELECT * FROM cancelacion ORDER BY fecha DESC";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filtrar) {
                ps.setString(1, fechaInicio);
                ps.setString(2, fechaFin);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar cancelaciones: " + e);
        }
        return lista;
    }

    private Cancelacion mapear(ResultSet rs) throws SQLException {
        Cancelacion c = new Cancelacion();
        c.setId(rs.getInt("id"));
        c.setReservacionId(rs.getInt("reservacion_id"));
        c.setFecha(rs.getDate("fecha"));
        c.setMontoReembolso(rs.getBigDecimal("monto_reembolso"));
        c.setPerdidaAgencia(rs.getBigDecimal("perdida_agencia"));
        return c;
    }
}