package daoImplementacion;

import Conexion.ConexionDB;
import dao.AccesorioDAO;
import java.sql.*;
import java.util.ArrayList;
import modelo.Accesorio;

/**
 *
 * @author Mairon
 */
public class AccesorioDAOImpl implements AccesorioDAO {

    private static final String SELECT_ACCESORIOS =
            "SELECT p.id_producto, p.codigo, p.nombre, p.marca, p.precio, p.stock, p.stock_minimo, p.activo, "
          + "ac.categoria, ac.descripcion "
          + "FROM producto p INNER JOIN accesorio ac ON ac.id_producto = p.id_producto ";

    @Override
    public ArrayList<Accesorio> listar() {
        String sql = SELECT_ACCESORIOS + "WHERE p.activo = 1 ORDER BY p.nombre";
        return consultar(sql, null);
    }

    @Override
    public ArrayList<Accesorio> buscar(String texto) {
        String sql = SELECT_ACCESORIOS + "WHERE p.activo = 1 AND (p.codigo LIKE ? OR p.nombre LIKE ? OR p.marca LIKE ?) ORDER BY p.nombre";
        return consultar(sql, "%" + texto.trim() + "%");
    }

    @Override
    public Accesorio buscarPorId(int idProducto) {
        String sql = SELECT_ACCESORIOS + "WHERE p.id_producto = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearAccesorio(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insertar(Accesorio accesorio) {
        String sql = "INSERT INTO accesorio(id_producto, categoria, descripcion) VALUES (?,?,?)";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            int id = ProductoDAOImpl.insertarProducto(con, accesorio);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, accesorio.getCategoria());
                ps.setString(3, accesorio.getDescripcion());
                ps.executeUpdate();
            }

            con.commit();
            accesorio.setIdProducto(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar accesorio: " + e.getMessage());
            ProductoDAOImpl.rollback(con);
            return false;
        } finally {
            ProductoDAOImpl.cerrar(con);
        }
    }

    @Override
    public boolean actualizar(Accesorio accesorio) {
        String sql = "UPDATE accesorio SET categoria=?, descripcion=? WHERE id_producto=?";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            ProductoDAOImpl.actualizarProducto(con, accesorio);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, accesorio.getCategoria());
                ps.setString(2, accesorio.getDescripcion());
                ps.setInt(3, accesorio.getIdProducto());
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar accesorio: " + e.getMessage());
            ProductoDAOImpl.rollback(con);
            return false;
        } finally {
            ProductoDAOImpl.cerrar(con);
        }
    }

    @Override
    public boolean eliminar(int idProducto) {
        try (Connection con = ConexionDB.getConexion()) {
            return ProductoDAOImpl.desactivar(con, idProducto);
        } catch (SQLException e) {
            System.err.println("Error al eliminar accesorio: " + e.getMessage());
            return false;
        }
    }

    // ---------- metodos de apoyo ----------

    private ArrayList<Accesorio> consultar(String sql, String textoBuscar) {
        ArrayList<Accesorio> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (textoBuscar != null) {
                ps.setString(1, textoBuscar);
                ps.setString(2, textoBuscar);
                ps.setString(3, textoBuscar);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(crearAccesorio(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Accesorio crearAccesorio(ResultSet rs) throws SQLException {
        Accesorio ac = new Accesorio();
        ProductoDAOImpl.llenarProducto(rs, ac);
        ac.setCategoria(rs.getString("categoria"));
        ac.setDescripcion(rs.getString("descripcion"));
        return ac;
    }
}
