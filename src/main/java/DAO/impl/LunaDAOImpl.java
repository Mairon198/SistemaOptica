package dao.impl;

import Conexion.ConexionDB;
import dao.LunaDAO;
import java.sql.*;
import java.util.ArrayList;
import modelo.Luna;

/**
 *
 * @author Mairon
 */
public class LunaDAOImpl implements LunaDAO {

    private static final String SELECT_LUNAS =
            "SELECT p.id_producto, p.codigo, p.nombre, p.marca, p.precio, p.stock, p.stock_minimo, p.activo, "
          + "l.tipo_vision, l.material, l.indice_refraccion, l.tratamiento "
          + "FROM producto p INNER JOIN luna l ON l.id_producto = p.id_producto ";

    @Override
    public ArrayList<Luna> listar() {
        String sql = SELECT_LUNAS + "WHERE p.activo = 1 ORDER BY p.nombre";
        return consultar(sql, null);
    }

    @Override
    public ArrayList<Luna> buscar(String texto) {
        String sql = SELECT_LUNAS + "WHERE p.activo = 1 AND (p.codigo LIKE ? OR p.nombre LIKE ? OR p.marca LIKE ?) ORDER BY p.nombre";
        return consultar(sql, "%" + texto.trim() + "%");
    }

    @Override
    public Luna buscarPorId(int idProducto) {
        String sql = SELECT_LUNAS + "WHERE p.id_producto = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearLuna(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insertar(Luna luna) {
        String sql = "INSERT INTO luna(id_producto, tipo_vision, material, indice_refraccion, tratamiento) VALUES (?,?,?,?,?)";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            int id = ProductoDAOImpl.insertarProducto(con, luna);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, luna.getTipoVision());
                ps.setString(3, luna.getMaterial());
                ps.setBigDecimal(4, luna.getIndiceRefraccion());
                ps.setString(5, luna.getTratamiento());
                ps.executeUpdate();
            }

            con.commit();
            luna.setIdProducto(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar luna: " + e.getMessage());
            ProductoDAOImpl.rollback(con);
            return false;
        } finally {
            ProductoDAOImpl.cerrar(con);
        }
    }

    @Override
    public boolean actualizar(Luna luna) {
        String sql = "UPDATE luna SET tipo_vision=?, material=?, indice_refraccion=?, tratamiento=? WHERE id_producto=?";
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            con.setAutoCommit(false);

            ProductoDAOImpl.actualizarProducto(con, luna);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, luna.getTipoVision());
                ps.setString(2, luna.getMaterial());
                ps.setBigDecimal(3, luna.getIndiceRefraccion());
                ps.setString(4, luna.getTratamiento());
                ps.setInt(5, luna.getIdProducto());
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar luna: " + e.getMessage());
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
            System.err.println("Error al eliminar luna: " + e.getMessage());
            return false;
        }
    }

    // ---------- metodos de apoyo ----------

    private ArrayList<Luna> consultar(String sql, String textoBuscar) {
        ArrayList<Luna> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (textoBuscar != null) {
                ps.setString(1, textoBuscar);
                ps.setString(2, textoBuscar);
                ps.setString(3, textoBuscar);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(crearLuna(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Luna crearLuna(ResultSet rs) throws SQLException {
        Luna luna = new Luna();
        ProductoDAOImpl.llenarProducto(rs, luna);
        luna.setTipoVision(rs.getString("tipo_vision"));
        luna.setMaterial(rs.getString("material"));
        luna.setIndiceRefraccion(rs.getBigDecimal("indice_refraccion"));
        luna.setTratamiento(rs.getString("tratamiento"));
        return luna;
    }
}