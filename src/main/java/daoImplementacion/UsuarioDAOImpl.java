package daoImplementacion;

import Conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import dao.UsuarioDAO;
import modelo.Rol;
import modelo.Usuario;

public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String SELECT_BASE
            = "SELECT u.idUsuario, u.usuario, u.email, u.passwordHash, u.rol, u.idCliente, u.idVendedor, "
            + "u.activo, u.intentosFallidos, u.bloqueadoHasta, u.ultimoAcceso, "
            + "COALESCE(c.nombres + ' ' + c.apellidos, v.nombres + ' ' + v.apellidos, u.usuario) AS nombreMostrar "
            + "FROM Usuario u "
            + "LEFT JOIN Cliente c ON c.idCliente = u.idCliente "
            + "LEFT JOIN Vendedor v ON v.idVendedor = u.idVendedor ";

    @Override
    public Usuario buscarPorLogin(String usuarioOCorreo) {
        String sql = SELECT_BASE + "WHERE u.usuario = ? OR u.email = ?";
        Connection con = ConexionDB.getConexion();
        if (con == null) {
            throw new IllegalStateException("Sin conexión a la base de datos");
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuarioOCorreo);
            ps.setString(2, usuarioOCorreo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar el usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existe(String usuario, String email) {
        String sql = "SELECT 1 FROM Usuario WHERE usuario = ? OR email = ?";
        Connection con = ConexionDB.getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al consultar el usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean insertar(Usuario u) {
        String sql = "INSERT INTO Usuario (usuario, email, passwordHash, rol, idCliente, idVendedor) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = ConexionDB.getConexion();
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
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void registrarAccesoExitoso(int idUsuario) {
        String sql = "UPDATE Usuario SET intentosFallidos = 0, bloqueadoHasta = NULL, "
                + "ultimoAcceso = SYSDATETIME() WHERE idUsuario = ?";
        Connection con = ConexionDB.getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al registrar acceso: " + e.getMessage());
        }
    }

    @Override
    public void registrarIntentoFallido(int idUsuario, int intentos, LocalDateTime bloqueadoHasta) {
        String sql = "UPDATE Usuario SET intentosFallidos = ?, bloqueadoHasta = ? WHERE idUsuario = ?";
        Connection con = ConexionDB.getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, intentos);
            if (bloqueadoHasta == null) {
                ps.setNull(2, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(2, Timestamp.valueOf(bloqueadoHasta));
            }
            ps.setInt(3, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al registrar intento fallido: " + e.getMessage());
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("idUsuario"));
        u.setUsuario(rs.getString("usuario"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("passwordHash"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        int idCliente = rs.getInt("idCliente");
        u.setIdCliente(rs.wasNull() ? null : idCliente);
        int idVendedor = rs.getInt("idVendedor");
        u.setIdVendedor(rs.wasNull() ? null : idVendedor);
        u.setActivo(rs.getBoolean("activo"));
        u.setIntentosFallidos(rs.getInt("intentosFallidos"));
        Timestamp bloqueo = rs.getTimestamp("bloqueadoHasta");
        u.setBloqueadoHasta(bloqueo == null ? null : bloqueo.toLocalDateTime());
        Timestamp acceso = rs.getTimestamp("ultimoAcceso");
        u.setUltimoAcceso(acceso == null ? null : acceso.toLocalDateTime());
        u.setNombreMostrar(rs.getString("nombreMostrar"));
        return u;
    }
}
