package dao;

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

    Aro buscarPorId(int idProducto);

    boolean insertar(Aro aro);

    boolean actualizar(Aro aro);

    boolean eliminar(int idProducto);
}
