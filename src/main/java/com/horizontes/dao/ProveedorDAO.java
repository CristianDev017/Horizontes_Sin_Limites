
package com.horizontes.dao;

import com.horizontes.model.Proveedor;
import com.horizontes.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public List<Proveedor> listar() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedor ORDER BY nombre";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar proveedores: " + e);
        }
        return lista;
    }

    public Proveedor buscarPorId(int id) {
        String sql = "SELECT * FROM proveedor WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Error buscar proveedor: " + e);
        }
        return null;
    }

    public boolean insertar(Proveedor p) {
        String sql = "INSERT INTO proveedor (nombre, tipo, pais, contacto) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getTipo());
            ps.setString(3, p.getPais());
            ps.setString(4, p.getContacto());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error insertar proveedor: " + e);
        }
        return false;
    }

    public boolean actualizar(Proveedor p) {
        String sql = "UPDATE proveedor SET nombre=?, tipo=?, pais=?, contacto=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getTipo());
            ps.setString(3, p.getPais());
            ps.setString(4, p.getContacto());
            ps.setInt(5, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error actualizar proveedor: " + e);
        }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM proveedor WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error eliminar proveedor: " + e);
        }
        return false;
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setTipo(rs.getInt("tipo"));
        p.setPais(rs.getString("pais"));
        p.setContacto(rs.getString("contacto"));
        return p;
    }
}