package dao;

import java.util.ArrayList;
import modelo.Medicion;

public interface MedicionDAO {

    ArrayList<Medicion> listarPorCliente(int idCliente);

    Medicion buscarPorId(int idMedicion);
}
