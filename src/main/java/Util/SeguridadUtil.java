package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public final class SeguridadUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SALT = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    // Hash falso para igualar el tiempo de respuesta cuando el usuario no existe
    private static final String HASH_FALSO = hashear("falso-no-usar".toCharArray());

    private SeguridadUtil() {
    }

    public static String hashear(char[] password) {
        byte[] salt = new byte[BYTES_SALT];
        RANDOM.nextBytes(salt);
        byte[] hash = derivar(password, salt, ITERACIONES);
        return ITERACIONES + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(char[] password, String almacenado) {
        try {
            String[] partes = almacenado.split(":");
            if (partes.length != 3) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] salt = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            byte[] calculado = derivar(password, salt, iteraciones);
            return MessageDigest.isEqual(esperado, calculado); // comparación en tiempo constante
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Gasta el mismo tiempo que una verificación real (evita revelar si el usuario existe). */
    public static void verificacionFalsa(char[] password) {
        verificar(password, HASH_FALSO);
    }

    private static byte[] derivar(char[] password, byte[] salt, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static void limpiar(char[] datos) {
        if (datos != null) {
            Arrays.fill(datos, '\0');
        }
    }
}
