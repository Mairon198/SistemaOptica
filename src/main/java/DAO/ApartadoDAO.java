package dao;

import java.util.ArrayList;
import modelo.Apartado;

public interface ApartadoDAO {

    boolean insertar(Apartado apartado);

    ArrayList<Apartado> listarPorCliente(int idCliente);

    boolean existeActivo(int idCliente, int idProducto);

    boolean cancelar(int idApartado, int idCliente);
}
