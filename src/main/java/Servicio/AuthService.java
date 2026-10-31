package servicio;

import dao.UsuarioDAO;
import dao.impl.UsuarioDAOImpl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import modelo.Usuario;
import util.SeguridadUtil;

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
            SeguridadUtil.verificacionFalsa(password);
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
        u.setPasswordHash(null);
        return u;
    }
}
