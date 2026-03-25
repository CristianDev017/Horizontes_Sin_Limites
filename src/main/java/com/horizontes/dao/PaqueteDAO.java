
package com.horizontes.dao;

import com.horizontes.model.Paquete;
import com.horizontes.model.ServicioPaquete;
import com.horizontes.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaqueteDAO {

    public List<Paquete> listar() {
        List<Paquete> lista = new ArrayList<>();
        String sql = """
                SELECT p.*, d.nombre AS destino_nombre
                FROM paquete p
                JOIN destino d ON p.destino_id = d.id
                ORDER BY p.nombre
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar paquetes: " + e);
        }
        return lista;
    }

    public Paquete buscarPorId(int id) {
        String sql = """
                SELECT p.*, d.nombre AS destino_nombre
                FROM paquete p
                JOIN destino d ON p.destino_id = d.id
                WHERE p.id = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Paquete p = mapear(rs);
                p.setServicios(listarServicios(id));
                return p;
            }
        } catch (SQLException e) {
            System.out.println("Error buscar paquete: " + e);
        }
        return null;
    }

    public List<Paquete> listarPorDestino(int destinoId) {
        List<Paquete> lista = new ArrayList<>();
        String sql = """
                SELECT p.*, d.nombre AS destino_nombre
                FROM paquete p
                JOIN destino d ON p.destino_id = d.id
                WHERE p.destino_id = ? AND p.activo = 1
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, destinoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar paquetes por destino: " + e);
        }
        return lista;
    }

    public boolean insertar(Paquete p) {
        String sql = """
                INSERT INTO paquete (nombre, destino_id, duracion_dias, descripcion, precio_venta, capacidad, activo)
                VALUES (?, ?, ?, ?, ?, ?, 1)
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getDestinoId());
            ps.setInt(3, p.getDuracionDias());
            ps.setString(4, p.getDescripcion());
            ps.setBigDecimal(5, p.getPrecioVenta());
            ps.setInt(6, p.getCapacidad());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error insertar paquete: " + e);
        }
        return false;
    }

    public boolean actualizar(Paquete p) {
        String sql = """
                UPDATE paquete SET nombre=?, destino_id=?, duracion_dias=?,
                descripcion=?, precio_venta=?, capacidad=? WHERE id=?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getDestinoId());
            ps.setInt(3, p.getDuracionDias());
            ps.setString(4, p.getDescripcion());
            ps.setBigDecimal(5, p.getPrecioVenta());
            ps.setInt(6, p.getCapacidad());
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error actualizar paquete: " + e);
        }
        return false;
    }

    public boolean desactivar(int id) {
        String sql = "UPDATE paquete SET activo = 0 WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error desactivar paquete: " + e);
        }
        return false;
    }

    public boolean insertarServicio(ServicioPaquete s) {
        String sql = "INSERT INTO servicio_paquete (paquete_id, proveedor_id, descripcion, costo) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, s.getPaqueteId());
            ps.setInt(2, s.getProveedorId());
            ps.setString(3, s.getDescripcion());
            ps.setBigDecimal(4, s.getCosto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error insertar servicio: " + e);
        }
        return false;
    }

    public List<ServicioPaquete> listarServicios(int paqueteId) {
        List<ServicioPaquete> lista = new ArrayList<>();
        String sql = """
                SELECT sp.*, pr.nombre AS proveedor_nombre
                FROM servicio_paquete sp
                JOIN proveedor pr ON sp.proveedor_id = pr.id
                WHERE sp.paquete_id = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, paqueteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ServicioPaquete s = new ServicioPaquete();
                s.setId(rs.getInt("id"));
                s.setPaqueteId(rs.getInt("paquete_id"));
                s.setProveedorId(rs.getInt("proveedor_id"));
                s.setProveedorNombre(rs.getString("proveedor_nombre"));
                s.setDescripcion(rs.getString("descripcion"));
                s.setCosto(rs.getBigDecimal("costo"));
                lista.add(s);
            }
        } catch (SQLException e) {
            System.out.println("Error listar servicios: " + e);
        }
        return lista;
    }

    private Paquete mapear(ResultSet rs) throws SQLException {
        Paquete p = new Paquete();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDestinoId(rs.getInt("destino_id"));
        p.setDestinoNombre(rs.getString("destino_nombre"));
        p.setDuracionDias(rs.getInt("duracion_dias"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        p.setCapacidad(rs.getInt("capacidad"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}