package servicio;

import dao.ClienteDAO;
import dao.impl.ClienteDAOImpl;
import modelo.Cliente;

//El registro de clientes ahora lo hace RegistroService
public class ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteService() {
        this(new ClienteDAOImpl());
    }

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public Cliente buscarPorId(int idCliente) {
        return clienteDAO.buscarPorId(idCliente);
    }
}
