package servicio;

import dao.RegistroDAO;
import dao.UsuarioDAO;
import dao.impl.RegistroDAOImpl;
import dao.impl.UsuarioDAOImpl;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.regex.Pattern;
import modelo.Cliente;
import modelo.Rol;
import modelo.Usuario;
import modelo.Vendedor;
import util.Configuracion;
import util.SeguridadUtil;

public class RegistroService {

    public static final String CLAVE_CODIGO_VENDEDOR = "registro.codigo.vendedor";

    private static final Pattern USUARIO = Pattern.compile("^[A-Za-z0-9._-]{4,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+() -]{6,20}$");
    private static final Pattern DNI = Pattern.compile("^[0-9A-Za-z-]{6,20}$");

    private final UsuarioDAO usuarioDAO;
    private final RegistroDAO registroDAO;

    public RegistroService() {
        this(new UsuarioDAOImpl(), new RegistroDAOImpl());
    }

    public RegistroService(UsuarioDAO usuarioDAO, RegistroDAO registroDAO) {
        this.usuarioDAO = usuarioDAO;
        this.registroDAO = registroDAO;
    }

    public static boolean registroVendedorHabilitado() {
        return Configuracion.get(CLAVE_CODIGO_VENDEDOR) != null;
    }

    public void registrarCliente(String nombres, String apellidos, String dni, String telefono,
                                 String direccion, String usuario, String email,
                                 char[] password, char[] confirmacion) throws ValidacionException {
        nombres = requerido(nombres, "Nombres", 100);
        apellidos = requerido(apellidos, "Apellidos", 100);
        dni = opcional(dni, "DNI", DNI, 20);
        telefono = opcional(telefono, "Teléfono", TELEFONO, 20);
        direccion = opcionalLibre(direccion, "Dirección", 200);
        usuario = validarUsuario(usuario);
        email = validarEmail(email);
        validarPassword(password, confirmacion, usuario);
        validarDisponible(usuario, email);

        Cliente c = new Cliente();
        c.setNombres(nombres);
        c.setApellidos(apellidos);
        c.setDni(dni);
        c.setTelefono(telefono);
        c.setEmail(email);
        c.setDireccion(direccion);

        Usuario u = nuevoUsuario(usuario, email, password, Rol.CLIENTE);
        guardar(() -> registroDAO.registrarCliente(c, u));
    }

    public void registrarVendedor(String nombres, String apellidos, String telefono,
                                  String usuario, String email, char[] password, char[] confirmacion,
                                  char[] codigoAutorizacion) throws ValidacionException {
        String codigoEsperado = Configuracion.get(CLAVE_CODIGO_VENDEDOR);
        if (codigoEsperado == null) {
            throw new ValidacionException("El registro de vendedores no está habilitado. "
                    + "El administrador debe definir '" + CLAVE_CODIGO_VENDEDOR + "' en config.properties.");
        }
        if (codigoAutorizacion == null || !iguales(codigoAutorizacion, codigoEsperado)) {
            throw new ValidacionException("El código de autorización de vendedor no es válido.");
        }
        nombres = requerido(nombres, "Nombres", 100);
        apellidos = requerido(apellidos, "Apellidos", 100);
        telefono = opcional(telefono, "Teléfono", TELEFONO, 20);
        usuario = validarUsuario(usuario);
        email = validarEmail(email);
        validarPassword(password, confirmacion, usuario);
        validarDisponible(usuario, email);

        Vendedor v = new Vendedor();
        v.setNombres(nombres);
        v.setApellidos(apellidos);
        v.setUsuario(usuario);
        v.setTelefono(telefono);

        Usuario u = nuevoUsuario(usuario, email, password, Rol.VENDEDOR);
        guardar(() -> registroDAO.registrarVendedor(v, u));
    }

    private static String requerido(String valor, String campo, int max) throws ValidacionException {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio.");
        }
        valor = valor.trim();
        if (valor.length() > max) {
            throw new ValidacionException("El campo '" + campo + "' no puede superar " + max + " caracteres.");
        }
        return valor;
    }

    private static String opcional(String valor, String campo, Pattern patron, int max) throws ValidacionException {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        valor = valor.trim();
        if (valor.length() > max || !patron.matcher(valor).matches()) {
            throw new ValidacionException("El campo '" + campo + "' no tiene un formato válido.");
        }
        return valor;
    }

    private static String opcionalLibre(String valor, String campo, int max) throws ValidacionException {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        valor = valor.trim();
        if (valor.length() > max) {
            throw new ValidacionException("El campo '" + campo + "' no puede superar " + max + " caracteres.");
        }
        return valor;
    }

    private static String validarUsuario(String usuario) throws ValidacionException {
        if (usuario == null || !USUARIO.matcher(usuario.trim()).matches()) {
            throw new ValidacionException("El usuario debe tener de 4 a 30 caracteres: "
                    + "letras, números, punto, guion o guion bajo.");
        }
        return usuario.trim();
    }

    private static String validarEmail(String email) throws ValidacionException {
        if (email == null || email.trim().length() > 150 || !EMAIL.matcher(email.trim()).matches()) {
            throw new ValidacionException("Ingrese un correo electrónico válido.");
        }
        return email.trim();
    }

    private static void validarPassword(char[] password, char[] confirmacion, String usuario)
            throws ValidacionException {
        if (password == null || password.length < 8) {
            throw new ValidacionException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (password.length > 72) {
            throw new ValidacionException("La contraseña no puede superar 72 caracteres.");
        }
        boolean letra = false;
        boolean numero = false;
        for (char ch : password) {
            letra |= Character.isLetter(ch);
            numero |= Character.isDigit(ch);
        }
        if (!letra || !numero) {
            throw new ValidacionException("La contraseña debe combinar letras y números.");
        }
        if (usuario != null && new String(password).equalsIgnoreCase(usuario)) {
            throw new ValidacionException("La contraseña no puede ser igual al usuario.");
        }
        if (confirmacion == null || !java.util.Arrays.equals(password, confirmacion)) {
            throw new ValidacionException("Las contraseñas no coinciden.");
        }
    }

    private void validarDisponible(String usuario, String email) throws ValidacionException {
        try {
            if (usuarioDAO.existe(usuario, email)) {
                throw new ValidacionException("El usuario o el correo ya están registrados.");
            }
        } catch (IllegalStateException e) {
            throw new ValidacionException("No se pudo conectar con la base de datos.");
        }
    }

    private static Usuario nuevoUsuario(String usuario, String email, char[] password, Rol rol) {
        Usuario u = new Usuario();
        u.setUsuario(usuario);
        u.setEmail(email);
        u.setPasswordHash(SeguridadUtil.hashear(password));
        u.setRol(rol);
        return u;
    }

    private interface Operacion {
        void ejecutar() throws SQLException;
    }

    private static void guardar(Operacion op) throws ValidacionException {
        try {
            op.ejecutar();
        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                throw new ValidacionException("El usuario o el correo ya están registrados.");
            }
            System.err.println("Error al registrar la cuenta: " + e.getMessage());
            throw new ValidacionException("No se pudo registrar la cuenta. Intente nuevamente.");
        }
    }

    private static boolean iguales(char[] ingresado, String esperado) {
        byte[] a = new String(ingresado).getBytes(StandardCharsets.UTF_8);
        byte[] b = esperado.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
