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


public class VendedorFrame extends JFrame {

    public VendedorFrame(Usuario usuario) {
        super("Sistema Óptica - Vendedor");
        if (usuario.getRol() != Rol.VENDEDOR) {
            throw new SecurityException("Acceso denegado: se requiere rol VENDEDOR");
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 520);

        JLabel bienvenida = new JLabel("Bienvenido, " + usuario.getNombreMostrar(), SwingConstants.CENTER);
        bienvenida.setFont(bienvenida.getFont().deriveFont(Font.BOLD, 20f));
        JLabel rol = new JLabel("Panel de VENDEDOR", SwingConstants.CENTER);

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
