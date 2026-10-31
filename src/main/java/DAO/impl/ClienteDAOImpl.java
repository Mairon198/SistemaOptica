package dao.impl;

import Conexion.ConexionDB;
import dao.ClienteDAO;
import java.sql.*;
import java.util.ArrayList;
import modelo.Cliente;


public class ClienteDAOImpl implements ClienteDAO {

    private static final String SELECT_CLIENTES =
            "SELECT idCliente, dni, nombres, apellidos, telefono, email, direccion, fechaRegistro, activo "
          + "FROM cliente ";

    @Override
    public ArrayList<Cliente> listar() {
        ArrayList<Cliente> lista = new ArrayList<>();
        String sql = SELECT_CLIENTES + "WHERE activo = 1 ORDER BY nombres, apellidos";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Cliente buscarPorId(int idCliente) {
        String sql = SELECT_CLIENTES + "WHERE idCliente = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearCliente(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente: " + e.getMessage());
        }
        return null;
    }

    static Cliente crearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("idCliente"));
        c.setDni(rs.getString("dni"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setDireccion(rs.getString("direccion"));
        Timestamp fecha = rs.getTimestamp("fechaRegistro");
        c.setFechaRegistro(fecha == null ? null : fecha.toLocalDateTime());
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}
