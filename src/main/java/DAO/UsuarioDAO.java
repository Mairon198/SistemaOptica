package dao;

import java.time.LocalDateTime;
import modelo.Usuario;

public interface UsuarioDAO {

    Usuario buscarPorLogin(String usuarioOCorreo);

    boolean existe(String usuario, String email);

    boolean insertar(Usuario usuario);

    void registrarAccesoExitoso(int idUsuario);

    void registrarIntentoFallido(int idUsuario, int intentos, LocalDateTime bloqueadoHasta);
}
