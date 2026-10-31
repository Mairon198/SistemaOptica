package dao.impl;

import Conexion.ConexionDB;
import dao.VentaDAO;
import java.sql.*;
import java.util.ArrayList;
import modelo.Accesorio;
import modelo.Aro;
import modelo.Cliente;
import modelo.DetalleVenta;
import modelo.Luna;
import modelo.Medicion;
import modelo.Producto;
import modelo.Vendedor;
import modelo.Venta;

public class VentaDAOImpl implements VentaDAO {

    private static final String SELECT_VENTAS =
            "SELECT v.id_venta, v.numero_venta, v.id_cliente, v.id_vendedor, v.id_medicion, v.fecha, "
          + "v.subtotal, v.impuesto, v.total, v.metodo_pago, v.estado, "
          + "c.nombres AS cli_nombres, c.apellidos AS cli_apellidos, c.dni AS cli_dni, "
          + "ve.nombres AS ven_nombres, ve.apellidos AS ven_apellidos "
          + "FROM venta v "
          + "INNER JOIN cliente c ON c.idCliente = v.id_cliente "
          + "INNER JOIN vendedor ve ON ve.idVendedor = v.id_vendedor ";

    private String ultimoError;

    public String getUltimoError() {
        return ultimoError;
    }

    @Override
    public boolean registrar(Venta venta) {
        String sqlVenta = "INSERT INTO venta(numero_venta, id_cliente, id_vendedor, id_medicion, fecha, "
                + "subtotal, impuesto, total, metodo_pago, estado) VALUES (?,?,?,?,?,?,?,?,?,?)";
        String sqlStock = "UPDATE producto SET stock = stock - ? "
                + "WHERE id_producto = ? AND activo = 1 AND stock >= ?";
        String sqlDetalle = "INSERT INTO detalle_venta(id_venta, id_producto, cantidad, precio_unitario) "
                + "VALUES (?,?,?,?)";
        ultimoError = null;
        Connection con = null;
        try {
            con = ConexionDB.getConexion();
            if (con == null) {
                ultimoError = "No hay conexión con la base de datos.";
                return false;
            }
            con.setAutoCommit(false);

            //Guardar la venta
            int idVenta;
            try (PreparedStatement ps = con.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, venta.getNumeroVenta());
                ps.setInt(2, venta.getCliente().getIdCliente());
                ps.setInt(3, venta.getVendedor().getIdVendedor());
                if (venta.getMedicion() == null) {
                    ps.setNull(4, Types.INTEGER);
                } else {
                    ps.setInt(4, venta.getMedicion().getIdMedicion());
                }
                ps.setTimestamp(5, Timestamp.valueOf(venta.getFecha()));
                ps.setBigDecimal(6, venta.getSubtotal());
                ps.setBigDecimal(7, venta.getImpuesto());
                ps.setBigDecimal(8, venta.getTotal());
                ps.setString(9, venta.getMetodoPago());
                ps.setString(10, venta.getEstado());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new SQLException("No se obtuvo el id de la venta");
                    }
                    idVenta = rs.getInt(1);
                }
            }

            //Descontar stock y guardar los detalles
            try (PreparedStatement psStock = con.prepareStatement(sqlStock);
                 PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                for (DetalleVenta d : venta.getDetalles()) {
                    Producto p = d.getProducto();

                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, p.getIdProducto());
                    psStock.setInt(3, d.getCantidad());
                    if (psStock.executeUpdate() == 0) {
                        throw new SQLException("Stock insuficiente para " + p.getCodigo() + " - " + p.getNombre());
                    }

                    psDetalle.setInt(1, idVenta);
                    psDetalle.setInt(2, p.getIdProducto());
                    psDetalle.setInt(3, d.getCantidad());
                    psDetalle.setBigDecimal(4, d.getPrecioUnitario());
                    psDetalle.executeUpdate();
                    d.setIdVenta(idVenta);
                }
            }

            con.commit();
            venta.setIdVenta(idVenta);
            return true;
        } catch (SQLException e) {
            ultimoError = e.getMessage();
            System.err.println("Error al registrar venta: " + e.getMessage());
            ProductoDAOImpl.rollback(con);
            return false;
        } finally {
            ProductoDAOImpl.cerrar(con);
        }
    }

    @Override
    public ArrayList<Venta> listar() {
        ArrayList<Venta> lista = new ArrayList<>();
        String sql = SELECT_VENTAS + "ORDER BY v.fecha DESC";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearVenta(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ventas: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public Venta buscarPorId(int idVenta) {
        String sql = SELECT_VENTAS + "WHERE v.id_venta = ?";
        String sqlDetalle = "SELECT d.id_detalle, d.id_venta, d.cantidad, d.precio_unitario, "
                + "p.id_producto, p.codigo, p.tipo, p.nombre, p.marca, p.precio, p.stock, p.stock_minimo, p.activo "
                + "FROM detalle_venta d INNER JOIN producto p ON p.id_producto = d.id_producto "
                + "WHERE d.id_venta = ? ORDER BY d.id_detalle";
        try (Connection con = ConexionDB.getConexion()) {
            Venta venta = null;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        venta = crearVenta(rs);
                    }
                }
            }
            if (venta == null) {
                return null;
            }
            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Producto p = crearProductoSegunTipo(rs.getString("tipo"));
                        ProductoDAOImpl.llenarProducto(rs, p);
                        venta.agregarDetalle(new DetalleVenta(rs.getInt("id_detalle"), rs.getInt("id_venta"),
                                p, rs.getInt("cantidad"), rs.getBigDecimal("precio_unitario")));
                    }
                }
            }
            return venta;
        } catch (SQLException e) {
            System.err.println("Error al buscar venta: " + e.getMessage());
        }
        return null;
    }

    // ---------- metodos de apoyo ----------

    private Venta crearVenta(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        v.setNumeroVenta(rs.getString("numero_venta"));

        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombres(rs.getString("cli_nombres"));
        c.setApellidos(rs.getString("cli_apellidos"));
        c.setDni(rs.getString("cli_dni"));
        v.setCliente(c);

        Vendedor ve = new Vendedor();
        ve.setIdVendedor(rs.getInt("id_vendedor"));
        ve.setNombres(rs.getString("ven_nombres"));
        ve.setApellidos(rs.getString("ven_apellidos"));
        v.setVendedor(ve);

        int idMedicion = rs.getInt("id_medicion");
        if (!rs.wasNull()) {
            Medicion m = new Medicion();
            m.setIdMedicion(idMedicion);
            v.setMedicion(m);
        }

        Timestamp fecha = rs.getTimestamp("fecha");
        v.setFecha(fecha == null ? null : fecha.toLocalDateTime());
        v.setSubtotal(rs.getBigDecimal("subtotal"));
        v.setImpuesto(rs.getBigDecimal("impuesto"));
        v.setTotal(rs.getBigDecimal("total"));
        v.setMetodoPago(rs.getString("metodo_pago"));
        v.setEstado(rs.getString("estado"));
        return v;
    }

    private Producto crearProductoSegunTipo(String tipo) {
        if (Producto.TIPO_LUNA.equals(tipo)) {
            return new Luna();
        }
        if (Producto.TIPO_ACCESORIO.equals(tipo)) {
            return new Accesorio();
        }
        return new Aro();
    }
}
