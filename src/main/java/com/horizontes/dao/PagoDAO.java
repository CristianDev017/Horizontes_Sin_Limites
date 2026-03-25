
package com.horizontes.dao;

import com.horizontes.model.Pago;
import com.horizontes.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    public boolean insertar(Pago p) {
        String sql = "INSERT INTO pago (reservacion_id, monto, metodo, fecha) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getReservacionId());
            ps.setBigDecimal(2, p.getMonto());
            ps.setInt(3, p.getMetodo());
            ps.setDate(4, new java.sql.Date(p.getFecha().getTime()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error insertar pago: " + e);
        }
        return false;
    }

    public List<Pago> listarPorReservacion(int reservacionId) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pago WHERE reservacion_id = ? ORDER BY fecha";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservacionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar pagos: " + e);
        }
        return lista;
    }

    public BigDecimal totalPagado(int reservacionId) {
        String sql = "SELECT COALESCE(SUM(monto), 0) FROM pago WHERE reservacion_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservacionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.out.println("Error total pagado: " + e);
        }
        return BigDecimal.ZERO;
    }

    private Pago mapear(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setId(rs.getInt("id"));
        p.setReservacionId(rs.getInt("reservacion_id"));
        p.setMonto(rs.getBigDecimal("monto"));
        p.setMetodo(rs.getInt("metodo"));
        p.setFecha(rs.getDate("fecha"));
        return p;
    }
}