
package com.horizontes.dao;

import com.horizontes.model.Usuario;
import com.horizontes.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario login(String nombre, String password) {
        String sql = "SELECT * FROM usuario WHERE nombre = ? AND password = ? AND activo = 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Error login: " + e);
        }
        return null;
    }

    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar usuarios: " + e);
        }
        return lista;
    }

    public Usuario buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM usuario WHERE nombre = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Error buscar usuario: " + e);
        }
        return null;
    }

    public boolean insertar(Usuario u) {
        String sql = "INSERT INTO usuario (nombre, password, tipo, activo) VALUES (?, ?, ?, 1)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getPassword());
            ps.setInt(3, u.getTipo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error insertar usuario: " + e);
        }
        return false;
    }

    public boolean actualizar(Usuario u) {
        String sql = "UPDATE usuario SET password = ?, tipo = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getPassword());
            ps.setInt(2, u.getTipo());
            ps.setInt(3, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error actualizar usuario: " + e);
        }
        return false;
    }

    public boolean desactivar(int id) {
        String sql = "UPDATE usuario SET activo = 0 WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error desactivar usuario: " + e);
        }
        return false;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setPassword(rs.getString("password"));
        u.setTipo(rs.getInt("tipo"));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }
}