package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.Color;
import modelo.Usuario;
import servicio.AuthException;
import servicio.AuthService;
import util.SeguridadUtil;

public class LoginFrame extends JFrame {

    private final JTextField txtUsuario = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JLabel lblMensaje = new JLabel(" ", SwingConstants.CENTER);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        super("Sistema Óptica - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        construirInterfaz();
        pack();
        setMinimumSize(new Dimension(380, getHeight()));
        setResizable(false);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void construirInterfaz() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);

        JLabel titulo = new JLabel("Sistema Óptica", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 16, 0);
        panel.add(titulo, gc);

        gc.insets = new Insets(4, 0, 2, 0);
        gc.gridy = 1;
        panel.add(new JLabel("Usuario o correo"), gc);
        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 8, 0);
        panel.add(txtUsuario, gc);

        gc.insets = new Insets(4, 0, 2, 0);
        gc.gridy = 3;
        panel.add(new JLabel("Contraseña"), gc);
        gc.gridy = 4;
        gc.insets = new Insets(0, 0, 8, 0);
        panel.add(txtPassword, gc);

        lblMensaje.setForeground(new Color(180, 30, 30));
        gc.gridy = 5;
        panel.add(lblMensaje, gc);

        gc.gridy = 6;
        gc.insets = new Insets(8, 0, 0, 0);
        panel.add(btnIngresar, gc);

        btnIngresar.addActionListener(e -> intentarLogin());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
    }

    private void intentarLogin() {
        final String identificador = txtUsuario.getText();
        final char[] password = txtPassword.getPassword();
        lblMensaje.setText(" ");
        btnIngresar.setEnabled(false);
        btnIngresar.setText("Verificando...");

        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                return authService.login(identificador, password);
            }

            @Override
            protected void done() {
                SeguridadUtil.limpiar(password);
                btnIngresar.setEnabled(true);
                btnIngresar.setText("Ingresar");
                try {
                    Usuario usuario = get();
                    txtPassword.setText("");
                    dispose();
                    Navegador.abrirSegunRol(usuario);
                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable causa = ex.getCause();
                    lblMensaje.setText(causa instanceof AuthException
                            ? causa.getMessage() : "Error inesperado al iniciar sesión.");
                    txtPassword.setText("");
                    txtPassword.requestFocusInWindow();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
