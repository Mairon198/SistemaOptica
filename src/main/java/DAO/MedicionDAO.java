package dao;

import java.util.ArrayList;
import java.util.List;
import modelo.Medicion;

public interface MedicionDAO {

    ArrayList<Medicion> listar();

    Medicion buscarPorId(int idMedicion);
    
    ArrayList<Medicion> listarPorCliente(int idCliente);
    
    List<Medicion> obtenerPorCliente(int idCliente);
}
