package dao;

import java.util.ArrayList;
import modelo.Cliente;

public interface ClienteDAO {

    ArrayList<Cliente> listar();

    Cliente buscarPorId(int idCliente);

    public int insertarYObtenerId(Cliente cliente);
    
    Cliente buscarPorDni(String dni);
    
}
