package util;

import modelo.Usuario;

/** Usuario que inició sesión en la aplicación. */
public final class Sesion {

    private static Usuario usuarioActual;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuario() {
        return usuarioActual;
    }

    public static boolean haySesion() {
        return usuarioActual != null;
    }

    public static void cerrar() {
        usuarioActual = null;
    }
}
