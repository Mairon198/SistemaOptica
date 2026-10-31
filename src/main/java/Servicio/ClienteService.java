package servicio;

import dao.ClienteDAO;
import dao.UsuarioDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.UsuarioDAOImpl;
import java.time.LocalDateTime;
import modelo.Cliente;
import modelo.Rol;
import modelo.Usuario;
import util.SeguridadUtil;

public class ClienteService {

    private final ClienteDAO clienteDAO;
    private final UsuarioDAO usuarioDAO;

    public ClienteService() {
        this(new ClienteDAOImpl(), new UsuarioDAOImpl());
    }

    public ClienteService(ClienteDAO clienteDAO, UsuarioDAO usuarioDAO) {
        this.clienteDAO = clienteDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public Cliente buscarPorId(int idCliente) {
        return clienteDAO.buscarPorId(idCliente);
    }

    //Registra el cliente y le crea su cuenta (el usuario es el DNI)
    public void registrar(Cliente c, char[] password) throws ValidacionException {
        try {
            c.setNombres(requerido(c.getNombres(), "Nombres"));
            c.setApellidos(requerido(c.getApellidos(), "Apellidos"));
            c.setDni(requerido(c.getDni(), "DNI"));
            c.setEmail(requerido(c.getEmail(), "Correo"));
            c.setTelefono(vacioANulo(c.getTelefono()));
            c.setDireccion(vacioANulo(c.getDireccion()));

            if (!c.getEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                throw new ValidacionException("El correo no es válido.");
            }
            if (password == null || password.length < 6) {
                throw new ValidacionException("La contraseña debe tener al menos 6 caracteres.");
            }
            if (clienteDAO.buscarPorDni(c.getDni()) != null) {
                throw new ValidacionException("Ya existe un cliente registrado con ese DNI.");
            }
            if (usuarioDAO.existe(c.getDni(), c.getEmail())) {
                throw new ValidacionException("Ya existe una cuenta con ese DNI o correo.");
            }

            c.setFechaRegistro(LocalDateTime.now());
            c.setActivo(true);
            int idCliente = clienteDAO.insertarYObtenerId(c);
            if (idCliente <= 0) {
                throw new ValidacionException("No se pudo registrar el cliente. Verifique la conexión.");
            }

            Usuario u = new Usuario();
            u.setUsuario(c.getDni());
            u.setEmail(c.getEmail());
            u.setPasswordHash(SeguridadUtil.hashear(password));
            u.setRol(Rol.CLIENTE);
            u.setIdCliente(idCliente);
            if (!usuarioDAO.insertar(u)) {
                throw new ValidacionException("El cliente se registró, pero no se pudo crear su cuenta.");
            }
        } finally {
            SeguridadUtil.limpiar(password);
        }
    }

    private String requerido(String valor, String campo) throws ValidacionException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo \"" + campo + "\" es obligatorio.");
        }
        return valor.trim();
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }
}
