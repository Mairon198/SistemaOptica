package servicio;

import dao.UsuarioDAO;
import dao.UsuarioDAOImpl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import modelo.Usuario;
import util.SeguridadUtil;

/**
 * Autenticación: valida usuario/correo + contraseña, con bloqueo temporal
 * tras varios intentos fallidos.
 */
public class AuthService {

    public static final int MAX_INTENTOS = 5;
    public static final int MINUTOS_BLOQUEO = 5;
    private static final String MSG_CREDENCIALES = "Usuario o contraseña incorrectos.";

    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this(new UsuarioDAOImpl());
    }

    public AuthService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * @return el usuario autenticado
     * @throws AuthException con el mensaje a mostrar si el acceso es denegado
     */
    public Usuario login(String identificador, char[] password) throws AuthException {
        if (identificador == null || identificador.isBlank() || password == null || password.length == 0) {
            throw new AuthException("Ingrese su usuario o correo y su contraseña.");
        }
        Usuario u;
        try {
            u = usuarioDAO.buscarPorLogin(identificador.trim());
        } catch (IllegalStateException e) {
            throw new AuthException("No se pudo conectar con la base de datos.");
        }

        if (u == null) {
            SeguridadUtil.verificacionFalsa(password); // mismo tiempo que un usuario real
            throw new AuthException(MSG_CREDENCIALES);
        }
        if (!u.isActivo()) {
            throw new AuthException("La cuenta está desactivada. Contacte al administrador.");
        }
        if (u.estaBloqueado()) {
            throw new AuthException("Cuenta bloqueada temporalmente. Intente después de las "
                    + u.getBloqueadoHasta().format(DateTimeFormatter.ofPattern("HH:mm")) + ".");
        }

        if (!SeguridadUtil.verificar(password, u.getPasswordHash())) {
            int intentos = u.getIntentosFallidos() + 1;
            LocalDateTime bloqueo = null;
            if (intentos >= MAX_INTENTOS) {
                bloqueo = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO);
                intentos = 0;
            }
            usuarioDAO.registrarIntentoFallido(u.getIdUsuario(), intentos, bloqueo);
            if (bloqueo != null) {
                throw new AuthException("Demasiados intentos. Cuenta bloqueada por "
                        + MINUTOS_BLOQUEO + " minutos.");
            }
            throw new AuthException(MSG_CREDENCIALES);
        }

        usuarioDAO.registrarAccesoExitoso(u.getIdUsuario());
        u.setPasswordHash(null); // no se conserva el hash en memoria
        return u;
    }
}
