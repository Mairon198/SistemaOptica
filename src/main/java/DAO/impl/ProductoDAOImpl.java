package dao.impl;

import Conexion.ConexionDB;
import dao.ProductoDAO;
import java.sql.*;
import modelo.Producto;


public class ProductoDAOImpl implements ProductoDAO {

    @Override
    public int buscarIdInactivoPorCodigo(String codigo, String tipo) {
        String sql = "SELECT id_producto FROM producto WHERE codigo = ? AND tipo = ? AND activo = 0";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto eliminado: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean activar(int idProducto) {
        String sql = "UPDATE producto SET activo = 1 WHERE id_producto = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al reactivar producto: " + e.getMessage());
            return false;
        }
    }

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