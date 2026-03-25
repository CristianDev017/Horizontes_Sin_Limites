
package com.horizontes.dao;

import com.horizontes.model.Destino;
import com.horizontes.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinoDAO {

    public List<Destino> listar() {
        List<Destino> lista = new ArrayList<>();
        String sql = "SELECT * FROM destino ORDER BY nombre";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listar destinos: " + e);
        }
        return lista;
    }

    public Destino buscarPorId(int id) {
        String sql = "SELECT * FROM destino WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Error buscar destino: " + e);
        }
        return null;
    }

    public boolean insertar(Destino d) {
        String sql = "INSERT INTO destino (nombre, pais, descripcion, clima, imagen_url) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getNombre());
            ps.setString(2, d.getPais());
            ps.setString(3, d.getDescripcion());
            ps.setString(4, d.getClima());
            ps.setString(5, d.getImagenUrl());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error insertar destino: " + e);
        }
        return false;
    }

    public boolean actualizar(Destino d) {
        String sql = "UPDATE destino SET nombre=?, pais=?, descripcion=?, clima=?, imagen_url=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getNombre());
            ps.setString(2, d.getPais());
            ps.setString(3, d.getDescripcion());
            ps.setString(4, d.getClima());
            ps.setString(5, d.getImagenUrl());
            ps.setInt(6, d.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error actualizar destino: " + e);
        }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM destino WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error eliminar destino: " + e);
        }
        return false;
    }

    private Destino mapear(ResultSet rs) throws SQLException {
        Destino d = new Destino();
        d.setId(rs.getInt("id"));
        d.setNombre(rs.getString("nombre"));
        d.setPais(rs.getString("pais"));
        d.setDescripcion(rs.getString("descripcion"));
        d.setClima(rs.getString("clima"));
        d.setImagenUrl(rs.getString("imagen_url"));
        return d;
    }
}