package dao.impl;

import Conexion.ConexionDB;
import dao.ApartadoDAO;
import java.sql.*;
import java.util.ArrayList;
import modelo.Apartado;
import modelo.Aro;
import modelo.Cliente;

public class ApartadoDAOImpl implements ApartadoDAO {

    @Override
    public boolean insertar(Apartado a) {
        String sql = "INSERT INTO apartado(id_cliente, id_producto, observaciones) VALUES (?,?,?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getCliente().getIdCliente());
            ps.setInt(2, a.getAro().getIdProducto());
            ps.setString(3, a.getObservaciones());
            if (ps.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setIdApartado(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al apartar aro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public ArrayList<Apartado> listarPorCliente(int idCliente) {
        ArrayList<Apartado> lista = new ArrayList<>();
        String sql = "SELECT a.id_apartado, a.id_cliente, a.fecha, a.estado, a.observaciones, "
                + "p.id_producto, p.codigo, p.nombre, p.marca, p.precio, p.stock, p.stock_minimo, p.activo, "
                + "ar.material, ar.forma, ar.color, ar.genero, ar.medidas, ar.imagen_url "
                + "FROM apartado a "
                + "INNER JOIN producto p ON p.id_producto = a.id_producto "
                + "INNER JOIN aro ar ON ar.id_producto = a.id_producto "
                + "WHERE a.id_cliente = ? ORDER BY a.fecha DESC";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(crearApartado(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar apartados: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public boolean existeActivo(int idCliente, int idProducto) {
        String sql = "SELECT 1 FROM apartado WHERE id_cliente = ? AND id_producto = ? AND estado = 'APARTADO'";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setInt(2, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar apartado: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean cancelar(int idApartado, int idCliente) {
        String sql = "UPDATE apartado SET estado = 'CANCELADO' "
                + "WHERE id_apartado = ? AND id_cliente = ? AND estado = 'APARTADO'";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idApartado);
            ps.setInt(2, idCliente);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cancelar apartado: " + e.getMessage());
            return false;
        }
    }

    private Apartado crearApartado(ResultSet rs) throws SQLException {
        Apartado a = new Apartado();
        a.setIdApartado(rs.getInt("id_apartado"));
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        a.setCliente(c);
        Timestamp fecha = rs.getTimestamp("fecha");
        a.setFecha(fecha == null ? null : fecha.toLocalDateTime());
        a.setEstado(rs.getString("estado"));
        a.setObservaciones(rs.getString("observaciones"));

        Aro aro = new Aro();
        ProductoDAOImpl.llenarProducto(rs, aro);
        aro.setMaterial(rs.getString("material"));
        aro.setForma(rs.getString("forma"));
        aro.setColor(rs.getString("color"));
        aro.setGenero(rs.getString("genero"));
        aro.setMedidas(rs.getString("medidas"));
        aro.setImagenUrl(rs.getString("imagen_url"));
        a.setAro(aro);
        return a;
    }
}
