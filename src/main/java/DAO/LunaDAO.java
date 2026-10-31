package dao;

import java.util.ArrayList;
import modelo.Luna;

/**
 *
 * @author Mairon
 */
public interface LunaDAO {

    ArrayList<Luna> listar();

    ArrayList<Luna> buscar(String texto);

    Luna buscarPorId(int idProducto);

    boolean insertar(Luna luna);

    boolean actualizar(Luna luna);

    boolean eliminar(int idProducto);
}