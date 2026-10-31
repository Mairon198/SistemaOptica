package dao.impl;

import Conexion.ConexionDB;
import dao.MedicionDAO;
import java.sql.*;
import java.util.ArrayList;
import modelo.Cliente;
import modelo.Medicion;


public class MedicionDAOImpl implements MedicionDAO {

    private static final String SELECT_MEDICIONES =
            "SELECT id_medicion, id_cliente, fecha, od_esfera, od_cilindro, od_eje, "
          + "oi_esfera, oi_cilindro, oi_eje, adicion, dip, observaciones "
          + "FROM medicion ";

    @Override
    public ArrayList<Medicion> listarPorCliente(int idCliente) {
        ArrayList<Medicion> lista = new ArrayList<>();
        String sql = SELECT_MEDICIONES + "WHERE id_cliente = ? ORDER BY fecha DESC";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(crearMedicion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar mediciones: " + e.getMessage());
        }
        return lista;
    }
    
    @Override
public java.util.List<Medicion> obtenerPorCliente(int idCliente) {
    ArrayList<Medicion> lista = new ArrayList<>();
    String sql = SELECT_MEDICIONES + "WHERE id_cliente = ? ORDER BY fecha DESC";
    try (Connection con = ConexionDB.getConexion();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, idCliente);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearMedicion(rs));
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al listar mediciones: " + e.getMessage());
    }
    return lista;
}
    

    @Override
    public Medicion buscarPorId(int idMedicion) {
        String sql = SELECT_MEDICIONES + "WHERE id_medicion = ?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMedicion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return crearMedicion(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar medición: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean insertar(Medicion m) {
        String sql = "INSERT INTO medicion(id_cliente, fecha, od_esfera, od_cilindro, od_eje, "
                + "oi_esfera, oi_cilindro, oi_eje, adicion, dip, observaciones) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getCliente().getIdCliente());
            ps.setTimestamp(2, Timestamp.valueOf(m.getFecha()));
            llenarValores(ps, m, 3);
            if (ps.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    m.setIdMedicion(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar medición: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean actualizar(Medicion m) {
        String sql = "UPDATE medicion SET od_esfera=?, od_cilindro=?, od_eje=?, oi_esfera=?, oi_cilindro=?, "
                + "oi_eje=?, adicion=?, dip=?, observaciones=? WHERE id_medicion=?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            llenarValores(ps, m, 1);
            ps.setInt(10, m.getIdMedicion());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar medición: " + e.getMessage());
            return false;
        }
    }

    private void llenarValores(PreparedStatement ps, Medicion m, int i) throws SQLException {
        ps.setBigDecimal(i, m.getOdEsfera());
        ps.setBigDecimal(i + 1, m.getOdCilindro());
        enteroONulo(ps, i + 2, m.getOdEje());
        ps.setBigDecimal(i + 3, m.getOiEsfera());
        ps.setBigDecimal(i + 4, m.getOiCilindro());
        enteroONulo(ps, i + 5, m.getOiEje());
        ps.setBigDecimal(i + 6, m.getAdicion());
        ps.setBigDecimal(i + 7, m.getDip());
        ps.setString(i + 8, m.getObservaciones());
    }

    private void enteroONulo(PreparedStatement ps, int i, Integer valor) throws SQLException {
        if (valor == null) {
            ps.setNull(i, Types.INTEGER);
        } else {
            ps.setInt(i, valor);
        }
    }

    private Medicion crearMedicion(ResultSet rs) throws SQLException {
        Medicion m = new Medicion();
        m.setIdMedicion(rs.getInt("id_medicion"));
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        m.setCliente(c);
        Timestamp fecha = rs.getTimestamp("fecha");
        m.setFecha(fecha == null ? null : fecha.toLocalDateTime());
        m.setOdEsfera(rs.getBigDecimal("od_esfera"));
        m.setOdCilindro(rs.getBigDecimal("od_cilindro"));
        m.setOdEje(enteroONulo(rs, "od_eje"));
        m.setOiEsfera(rs.getBigDecimal("oi_esfera"));
        m.setOiCilindro(rs.getBigDecimal("oi_cilindro"));
        m.setOiEje(enteroONulo(rs, "oi_eje"));
        m.setAdicion(rs.getBigDecimal("adicion"));
        m.setDip(rs.getBigDecimal("dip"));
        m.setObservaciones(rs.getString("observaciones"));
        return m;
    }

    private Integer enteroONulo(ResultSet rs, String columna) throws SQLException {
        int valor = rs.getInt(columna);
        return rs.wasNull() ? null : valor;
    }

    @Override
    public ArrayList<Medicion> listar() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
