package dao;

import java.util.ArrayList;
import modelo.Medicion;

/**
 * Contrato de acceso a datos de la tabla medicion (recetas oftalmológicas).
 */
public interface MedicionDAO {

    ArrayList<Medicion> listarPorCliente(int idCliente);

    Medicion buscarPorId(int idMedicion);
}
