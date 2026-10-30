package util;

import Conexion.ConexionDB;
import dao.UsuarioDAO;
import daoImplementacion.UsuarioDAOImpl;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import modelo.Rol;
import modelo.Usuario;

/**
 * Crea usuarios de prueba (si no existen) ligados al cliente y al vendedor del script SQL.
 * Ejecutar una vez después de database.sql. CAMBIE estas contraseñas en producción.
 */
public class CrearUsuariosDemo {

    public static void main(String[] args) throws SQLException {
        UsuarioDAO dao = new UsuarioDAOImpl();
        Connection con = ConexionDB.getConexion();
        if (con == null) {
            System.err.println("Sin conexión a la base de datos.");
            return;
        }
        Integer idVendedor = primerId(con, "SELECT TOP 1 idVendedor FROM Vendedor ORDER BY idVendedor");
        Integer idCliente = primerId(con, "SELECT TOP 1 idCliente FROM Cliente ORDER BY idCliente");

        crear(dao, "mairon", "mairon@optica.com", "Vendedor123", Rol.VENDEDOR, null, idVendedor);
        crear(dao, "juan", "juan@mail.com", "Cliente123", Rol.CLIENTE, idCliente, null);
        ConexionDB.cerrarConexion();
    }

    private static void crear(UsuarioDAO dao, String usuario, String email, String clave,
                              Rol rol, Integer idCliente, Integer idVendedor) {
        if (dao.existe(usuario, email)) {
            System.out.println("Ya existe: " + usuario);
            return;
        }
        Usuario u = new Usuario();
        u.setUsuario(usuario);
        u.setEmail(email);
        u.setPasswordHash(SeguridadUtil.hashear(clave.toCharArray()));
        u.setRol(rol);
        u.setIdCliente(idCliente);
        u.setIdVendedor(idVendedor);
        System.out.println(dao.insertar(u) ? "Creado: " + usuario + " (" + rol + ")" : "Error al crear " + usuario);
    }

    private static Integer primerId(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : null;
        }
    }
}
