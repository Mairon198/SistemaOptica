package vista;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import modelo.Rol;
import modelo.Usuario;

/** Interfaz del rol CLIENTE. Aquí se irán agregando los módulos del cliente. */
public class ClienteFrame extends JFrame {

    public ClienteFrame(Usuario usuario) {
        super("Sistema Óptica - Cliente");
        if (usuario.getRol() != Rol.CLIENTE) {
            throw new SecurityException("Acceso denegado: se requiere rol CLIENTE");
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 520);

        JLabel bienvenida = new JLabel("Bienvenido, " + usuario.getNombreMostrar(), SwingConstants.CENTER);
        bienvenida.setFont(bienvenida.getFont().deriveFont(Font.BOLD, 20f));
        JLabel rol = new JLabel("Panel de CLIENTE", SwingConstants.CENTER);

        JButton salir = new JButton("Cerrar sesión");
        salir.addActionListener(e -> Navegador.cerrarSesion(this));

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBorder(BorderFactory.createEmptyBorder(80, 20, 20, 20));
        centro.add(bienvenida, BorderLayout.CENTER);
        centro.add(rol, BorderLayout.SOUTH);

        JPanel sur = new JPanel();
        sur.add(salir);

        add(centro, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
    }
}
