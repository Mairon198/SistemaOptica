package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class Configuracion {

    private static final Properties PROPS = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("config.properties")) {
            PROPS.load(in);
        } catch (IOException e) {
        }
    }

    private Configuracion() {
    }

    public static String get(String clave) {
        String valor = PROPS.getProperty(clave);
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
