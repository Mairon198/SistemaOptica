package dao;

import java.util.ArrayList;
import modelo.Cliente;

/**
 * Contrato de acceso a datos de la tabla cliente.
 */
public interface ClienteDAO {

    ArrayList<Cliente> listar();

    Cliente buscarPorId(int idCliente);
}
