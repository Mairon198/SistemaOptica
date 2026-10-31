package dao;

import java.util.ArrayList;
import modelo.Accesorio;

/**
 *
 * @author Mairon
 */
public interface AccesorioDAO {

    ArrayList<Accesorio> listar();

    ArrayList<Accesorio> buscar(String texto);

    Accesorio buscarPorId(int idProducto);

    boolean insertar(Accesorio accesorio);

    boolean actualizar(Accesorio accesorio);

    boolean eliminar(int idProducto);
}
