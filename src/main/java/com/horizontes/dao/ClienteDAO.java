
package com.horizontes.dao;

import com.horizontes.model.Cliente;
import com.horizontes.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public Cliente buscarPorDpi(String dpi) {
        String sql = "SELECT * FROM cliente WHERE dpi = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dpi);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Error buscar cliente: " + e);
        }
        return null;
    }

    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM cliente ORDER BY nombre";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar clientes: " + e);
        }
        return lista;
    }

    public boolean insertar(Cliente c) {
        String sql = "INSERT INTO cliente (dpi, nombre, fecha_nac, telefono, email, nacionalidad) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getDpi());
            ps.setString(2, c.getNombre());
            ps.setDate(3, new java.sql.Date(c.getFechaNac().getTime()));
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getNacionalidad());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error insertar cliente: " + e);
        }
        return false;
    }

    public boolean actualizar(Cliente c) {
        String sql = "UPDATE cliente SET nombre=?, fecha_nac=?, telefono=?, email=?, nacionalidad=? WHERE dpi=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setDate(2, new java.sql.Date(c.getFechaNac().getTime()));
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getNacionalidad());
            ps.setString(6, c.getDpi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error actualizar cliente: " + e);
        }
        return false;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setDpi(rs.getString("dpi"));
        c.setNombre(rs.getString("nombre"));
        c.setFechaNac(rs.getDate("fecha_nac"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setNacionalidad(rs.getString("nacionalidad"));
        return c;
    }
}