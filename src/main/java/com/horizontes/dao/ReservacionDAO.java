
package com.horizontes.dao;

import com.horizontes.model.Cliente;
import com.horizontes.model.Reservacion;
import com.horizontes.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservacionDAO {

    public Reservacion insertar(Reservacion r) {
        String sql = """
                INSERT INTO reservacion (numero, fecha_creacion, fecha_viaje, paquete_id, agente_id, costo_total, estado)
                VALUES (?, ?, ?, ?, ?, ?, 'PENDIENTE')
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, generarNumero());
            ps.setDate(2, new java.sql.Date(System.currentTimeMillis()));
            ps.setDate(3, new java.sql.Date(r.getFechaViaje().getTime()));
            ps.setInt(4, r.getPaqueteId());
            ps.setInt(5, r.getAgenteId());
            ps.setBigDecimal(6, r.getCostoTotal());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                r.setId(keys.getInt(1));
                insertarPasajeros(r.getId(), r.getPasajeros(), con);
                return buscarPorId(r.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error insertar reservacion: " + e);
        }
        return null;
    }

    public Reservacion buscarPorId(int id) {
        String sql = """
                SELECT r.*, p.nombre AS paquete_nombre, u.nombre AS agente_nombre
                FROM reservacion r
                JOIN paquete p ON r.paquete_id = p.id
                JOIN usuario u ON r.agente_id = u.id
                WHERE r.id = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reservacion r = mapear(rs);
                r.setPasajeros(listarPasajeros(id));
                return r;
            }
        } catch (SQLException e) {
            System.out.println("Error buscar reservacion: " + e);
        }
        return null;
    }

    public Reservacion buscarPorNumero(String numero) {
        String sql = """
                SELECT r.*, p.nombre AS paquete_nombre, u.nombre AS agente_nombre
                FROM reservacion r
                JOIN paquete p ON r.paquete_id = p.id
                JOIN usuario u ON r.agente_id = u.id
                WHERE r.numero = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numero);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reservacion r = mapear(rs);
                r.setPasajeros(listarPasajeros(r.getId()));
                return r;
            }
        } catch (SQLException e) {
            System.out.println("Error buscar reservacion por numero: " + e);
        }
        return null;
    }

    public List<Reservacion> listarPorCliente(String dpi) {
        List<Reservacion> lista = new ArrayList<>();
        String sql = """
                SELECT r.*, p.nombre AS paquete_nombre, u.nombre AS agente_nombre
                FROM reservacion r
                JOIN paquete p ON r.paquete_id = p.id
                JOIN usuario u ON r.agente_id = u.id
                JOIN reservacion_pasajero rp ON r.id = rp.reservacion_id
                WHERE rp.cliente_dpi = ?
                ORDER BY r.fecha_creacion DESC
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dpi);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar reservaciones por cliente: " + e);
        }
        return lista;
    }

    public List<Reservacion> listarDelDia() {
        List<Reservacion> lista = new ArrayList<>();
        String sql = """
                SELECT r.*, p.nombre AS paquete_nombre, u.nombre AS agente_nombre
                FROM reservacion r
                JOIN paquete p ON r.paquete_id = p.id
                JOIN usuario u ON r.agente_id = u.id
                WHERE r.fecha_creacion = CURDATE()
                ORDER BY r.id DESC
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar reservaciones del dia: " + e);
        }
        return lista;
    }

    public boolean cambiarEstado(int id, String estado) {
        String sql = "UPDATE reservacion SET estado = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error cambiar estado: " + e);
        }
        return false;
    }

    private void insertarPasajeros(int reservacionId, List<Cliente> pasajeros, Connection con) throws SQLException {
        String sql = "INSERT INTO reservacion_pasajero (reservacion_id, cliente_dpi) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Cliente c : pasajeros) {
                ps.setInt(1, reservacionId);
                ps.setString(2, c.getDpi());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Cliente> listarPasajeros(int reservacionId) {
        List<Cliente> lista = new ArrayList<>();
        String sql = """
                SELECT c.* FROM cliente c
                JOIN reservacion_pasajero rp ON c.dpi = rp.cliente_dpi
                WHERE rp.reservacion_id = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservacionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setDpi(rs.getString("dpi"));
                c.setNombre(rs.getString("nombre"));
                c.setFechaNac(rs.getDate("fecha_nac"));
                c.setTelefono(rs.getString("telefono"));
                c.setEmail(rs.getString("email"));
                c.setNacionalidad(rs.getString("nacionalidad"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Error listar pasajeros: " + e);
        }
        return lista;
    }

    private String generarNumero() {
        String sql = "SELECT COUNT(*) FROM reservacion";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int total = rs.getInt(1) + 1;
                return String.format("RES-%05d", total);
            }
        } catch (SQLException e) {
            System.out.println("Error generar numero: " + e);
        }
        return "RES-00001";
    }

    private Reservacion mapear(ResultSet rs) throws SQLException {
        Reservacion r = new Reservacion();
        r.setId(rs.getInt("id"));
        r.setNumero(rs.getString("numero"));
        r.setFechaCreacion(rs.getDate("fecha_creacion"));
        r.setFechaViaje(rs.getDate("fecha_viaje"));
        r.setPaqueteId(rs.getInt("paquete_id"));
        r.setPaqueteNombre(rs.getString("paquete_nombre"));
        r.setAgenteId(rs.getInt("agente_id"));
        r.setAgenteNombre(rs.getString("agente_nombre"));
        r.setCostoTotal(rs.getBigDecimal("costo_total"));
        r.setEstado(rs.getString("estado"));
        return r;
    }
}