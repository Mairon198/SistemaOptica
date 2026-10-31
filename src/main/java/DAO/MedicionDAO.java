package dao;

import java.util.ArrayList;
import modelo.Medicion;

public interface MedicionDAO {

    ArrayList<Medicion> listar();

    Medicion buscarPorId(int idMedicion);
    
    ArrayList<Medicion> listarPorCliente(int idCliente);
    

    boolean insertar(Medicion medicion);

    boolean actualizar(Medicion medicion);
}
