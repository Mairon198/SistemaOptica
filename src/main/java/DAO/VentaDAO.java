package dao;

import java.util.ArrayList;
import modelo.Venta;

/**
 *
 * @author Mairon
 */
public interface VentaDAO {

    boolean registrar(Venta venta);

    ArrayList<Venta> listar();

    Venta buscarPorId(int idVenta);
}
