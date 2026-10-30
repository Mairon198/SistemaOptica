package daoImplementacion;

import Conexion.ConexionDB;
import dao.AroDAO;
import java.sql.*;
import java.util.ArrayList;
import modelo.Aro;

/**
 *
 * @author Mairon
 */
public class AroDAOImpl implements AroDAO {

    private static final String SELECT_AROS =
            "SELECT p.id_producto, p.codigo, p.nombre, p.marca, p.precio, p.stock, p.stock_minimo, p.activo, "
          + "a.material, a.forma, a.color, a.genero, a.medidas, a.imagen_url "
          + "FROM producto p INNER JOIN aro a ON a.id_producto = p.id_producto ";

    @Override
    public ArrayList<Aro> listar() {
        String sql = SELECT_AROS + "WHERE p.activo = 1 ORDER BY p.nombre";
        return consultar(sql, null);
    }

    @Override
    public ArrayList<Aro> listarStockBajo() {
        String sql = SELECT_AROS + "WHERE p.activo = 1 AND p.stock < p.stock_minimo ORDER BY p.stock";
        return consultar(sql, null);
    }

    @Override
    public ArrayList<Aro> buscar(String texto) {
        String sql = SELECT_AROS + "WHERE p.activo = 1 AND (p.codigo LIKE ? OR p.nombre LIKE ? OR p.marca LIKE ?) ORDER BY p.nombre";
        return consultar(sql, "%" + texto.trim() + "%");
    }

    @Override
    public Aro buscarPorId(int idProducto) {
        String sql = SELECT_AROS + "WHERE p.id_producto = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearAro(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insertar(Aro aro) {
        String sql = "INSERT INTO aro(id_producto, material, forma, color, genero, medidas, imagen_url) VALUES (?,?,?,?,?,?,?)";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);   // producto + aro se guardan juntos o no se guarda nada

            int id = ProductoDAOImpl.insertarProducto(con, aro);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, aro.getMaterial());
                ps.setString(3, aro.getForma());
                ps.setString(4, aro.getColor());
                ps.setString(5, aro.getGenero());
                ps.setString(6, aro.getMedidas());
                ps.setString(7, aro.getImagenUrl());
                ps.executeUpdate();
            }

            con.commit();
            aro.setIdProducto(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar aro: " + e.getMessage());
            ProductoDAOImpl.rollback(con);
            return false;
        } finally {
            ProductoDAOImpl.cerrar(con);
        }
    }

    @Override
    public boolean actualizar(Aro aro) {
        String sql = "UPDATE aro SET material=?, forma=?, color=?, genero=?, medidas=?, imagen_url=? WHERE id_producto=?";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            ProductoDAOImpl.actualizarProducto(con, aro);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, aro.getMaterial());
                ps.setString(2, aro.getForma());
                ps.setString(3, aro.getColor());
                ps.setString(4, aro.getGenero());
                ps.setString(5, aro.getMedidas());
                ps.setString(6, aro.getImagenUrl());
                ps.setInt(7, aro.getIdProducto());
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar aro: " + e.getMessage());
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
            System.err.println("Error al eliminar aro: " + e.getMessage());
            return false;
        }
    }

    // ---------- metodos de apoyo ----------

    private ArrayList<Aro> consultar(String sql, String textoBuscar) {
        ArrayList<Aro> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (textoBuscar != null) {
                ps.setString(1, textoBuscar);
                ps.setString(2, textoBuscar);
                ps.setString(3, textoBuscar);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(crearAro(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Aro crearAro(ResultSet rs) throws SQLException {
        Aro aro = new Aro();
        ProductoDAOImpl.llenarProducto(rs, aro);
        aro.setMaterial(rs.getString("material"));
        aro.setForma(rs.getString("forma"));
        aro.setColor(rs.getString("color"));
        aro.setGenero(rs.getString("genero"));
        aro.setMedidas(rs.getString("medidas"));
        aro.setImagenUrl(rs.getString("imagen_url"));
        return aro;
    }
}
