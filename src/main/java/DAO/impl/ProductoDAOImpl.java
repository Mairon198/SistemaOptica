package dao.impl;

import java.sql.*;
import modelo.Producto;

/**
 * Metodos de la tabla "producto" que comparten AroDAOImpl, LunaDAOImpl,
 * AccesorioDAOImpl y VentaDAOImpl (no tiene interfaz porque solo es de apoyo)
 *
 * @author Mairon
 */
public class ProductoDAOImpl {

    // Inserta en la tabla producto y devuelve el id generado
    public static int insertarProducto(Connection con, Producto p) throws SQLException {
        String sql = "INSERT INTO producto(codigo, tipo, nombre, marca, precio, stock, stock_minimo) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getTipo());
            ps.setString(3, p.getNombre());
            ps.setString(4, p.getMarca());
            ps.setBigDecimal(5, p.getPrecio());
            ps.setInt(6, p.getStock());
            ps.setInt(7, p.getStockMinimo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se obtuvo el id del producto");
    }

    public static void actualizarProducto(Connection con, Producto p) throws SQLException {
        String sql = "UPDATE producto SET codigo=?, nombre=?, marca=?, precio=?, stock=?, stock_minimo=? WHERE id_producto=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getMarca());
            ps.setBigDecimal(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setInt(6, p.getStockMinimo());
            ps.setInt(7, p.getIdProducto());
            ps.executeUpdate();
        }
    }

    // Borrado logico: no se borra la fila, solo se marca activo = 0
    public static boolean desactivar(Connection con, int idProducto) throws SQLException {
        String sql = "UPDATE producto SET activo = 0 WHERE id_producto = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        }
    }

    // Llena los datos generales del producto desde el ResultSet
    public static void llenarProducto(ResultSet rs, Producto p) throws SQLException {
        p.setIdProducto(rs.getInt("id_producto"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setMarca(rs.getString("marca"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setStockMinimo(rs.getInt("stock_minimo"));
        p.setActivo(rs.getBoolean("activo"));
    }

    public static void rollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void cerrar(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}