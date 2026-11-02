package dao;

import java.sql.SQLException;
import modelo.Cliente;
import modelo.Usuario;
import modelo.Vendedor;

public interface RegistroDAO {

    void registrarCliente(Cliente cliente, Usuario usuario) throws SQLException;

    void registrarVendedor(Vendedor vendedor, Usuario usuario) throws SQLException;
}
