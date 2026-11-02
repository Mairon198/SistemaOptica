package dao.impl;

import Conexion.ConexionDB;
import dao.RegistroDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import modelo.Cliente;
import modelo.Usuario;
import modelo.Vendedor;

public class RegistroDAOImpl implements RegistroDAO {

    @Override
    public void registrarCliente(Cliente c, Usuario u) throws SQLException {
        Connection con = conexion();
        boolean autoCommitPrevio = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            String sql = "INSERT INTO cliente (dni, nombres, apellidos, telefono, email, direccion) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            int idCliente;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setTexto(ps, 1, c.getDni());
                ps.setString(2, c.getNombres());
                ps.setString(3, c.getApellidos());
                setTexto(ps, 4, c.getTelefono());
                setTexto(ps, 5, c.getEmail());
                setTexto(ps, 6, c.getDireccion());
                ps.executeUpdate();
                idCliente = idGenerado(ps);
            }
            c.setIdCliente(idCliente);
            u.setIdCliente(idCliente);
            insertarUsuario(con, u);
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitPrevio);
        }
    }

    @Override
    public void registrarVendedor(Vendedor v, Usuario u) throws SQLException {
        Connection con = conexion();
        boolean autoCommitPrevio = con.getAutoCommit();
        try {
            con.setAutoCommit(false);
            String sql = "INSERT INTO vendedor (nombres, apellidos, usuario, telefono) VALUES (?, ?, ?, ?)";
            int idVendedor;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, v.getNombres());
                ps.setString(2, v.getApellidos());
                ps.setString(3, v.getUsuario());
                setTexto(ps, 4, v.getTelefono());
                ps.executeUpdate();
                idVendedor = idGenerado(ps);
            }
            v.setIdVendedor(idVendedor);
            u.setIdVendedor(idVendedor);
            insertarUsuario(con, u);
            con.commit();
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitPrevio);
        }
    }

    private void insertarUsuario(Connection con, Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (usuario, email, passwordHash, rol, idCliente, idVendedor) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getUsuario());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRol().name());
            if (u.getIdCliente() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, u.getIdCliente());
            }
            if (u.getIdVendedor() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, u.getIdVendedor());
            }
            ps.executeUpdate();
        }
    }

    private static Connection conexion() throws SQLException {
        Connection con = ConexionDB.getConexion();
        if (con == null) {
            throw new SQLException("Sin conexión a la base de datos");
        }
        return con;
    }

    private static int idGenerado(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (!rs.next()) {
                throw new SQLException("No se obtuvo el id generado");
            }
            return rs.getInt(1);
        }
    }

    private static void setTexto(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null || valor.isBlank()) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, valor.trim());
        }
    }
}
