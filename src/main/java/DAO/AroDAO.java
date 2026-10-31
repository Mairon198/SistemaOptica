package dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import modelo.Aro;

/**
 *
 * @author Mairon
 */
public interface AroDAO {

    ArrayList<Aro> listar();

    ArrayList<Aro> listarStockBajo();

    ArrayList<Aro> buscar(String texto);

    ArrayList<Aro> listarFiltrado(String marca, String material, String forma,
                                  BigDecimal precioMin, BigDecimal precioMax);

    Aro buscarPorId(int idProducto);

    boolean insertar(Aro aro);

    boolean actualizar(Aro aro);

    boolean eliminar(int idProducto);
}
