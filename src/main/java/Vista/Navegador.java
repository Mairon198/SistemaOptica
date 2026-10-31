package vista;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import modelo.Usuario;
import util.Sesion;

public final class Navegador {

    private Navegador() {
    }

    public static void abrirSegunRol(Usuario usuario) {
        Sesion.iniciar(usuario);
        JFrame ventana;
        switch (usuario.getRol()) {
            case VENDEDOR:
                ventana = new FrmMenuVendedor(usuario);
                break;
            case CLIENTE:
                ventana = new ClienteFrame(usuario);
                break;
            default:
                JOptionPane.showMessageDialog(null, "Rol no reconocido.", "Error", JOptionPane.ERROR_MESSAGE);
                Sesion.cerrar();
                return;
        }
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    public static void cerrarSesion(JFrame ventanaActual) {
        Sesion.cerrar();
        ventanaActual.dispose();
        new LoginFrame().setVisible(true);
    }
}
