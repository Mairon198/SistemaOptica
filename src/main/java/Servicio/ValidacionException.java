package servicio;

/** Error de reglas de negocio; su mensaje se muestra tal cual al usuario. */
public class ValidacionException extends Exception {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
