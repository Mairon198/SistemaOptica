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
    public int insertarYObtenerId(Cliente cliente) {
        int idGenerado = -1;
        String sql = "INSERT INTO cliente (dni, nombres, apellidos, telefono, email, direccion, fechaRegistro, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, cliente.getDni());
            ps.setString(2, cliente.getNombres());
            ps.setString(3, cliente.getApellidos());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getEmail());
            ps.setString(6, cliente.getDireccion());
            ps.setTimestamp(7, cliente.getFechaRegistro() == null ? null : Timestamp.valueOf(cliente.getFechaRegistro()));
            ps.setBoolean(8, cliente.isActivo());
            
            int filasAfectadas = ps.executeUpdate();
            
            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar cliente: " + e.getMessage());
        }
        
        return idGenerado;
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
    
    @Override
public Cliente buscarPorDni(String dni) {
    String sql = SELECT_CLIENTES + "WHERE dni = ?";
    try (Connection con = ConexionDB.getConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, dni);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return crearCliente(rs);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al buscar cliente por DNI: " + e.getMessage());
    }
    return null;
}
    
}

