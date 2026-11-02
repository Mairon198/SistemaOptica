package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import modelo.Usuario;
import servicio.AuthException;
import servicio.AuthService;
import util.SeguridadUtil;

public class LoginFrame extends JFrame {

    private final JTextField txtUsuario = EstiloUI.campo(new JTextField());
    private final JPasswordField txtPassword = EstiloUI.campo(new JPasswordField());
    private final JCheckBox chkMostrar = EstiloUI.casilla("Mostrar contraseña");
    private final JButton btnIngresar = EstiloUI.botonPrimario("Ingresar");
    private final JButton btnCrearCuenta = EstiloUI.botonEnlace("Crear cuenta nueva");
    private final JLabel lblMensaje = EstiloUI.mensajeError(44);
    private final AuthService authService = new AuthService();
    private final char echoOriginal = txtPassword.getEchoChar();

    public LoginFrame() {
        super("Sistema Óptica - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        construirInterfaz();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(crearPanelMarca(), BorderLayout.WEST);
        raiz.add(crearPanelFormulario(), BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel crearPanelMarca() {
        JPanel marca = new EstiloUI.PanelDegradado();
        marca.setPreferredSize(new Dimension(340, 500));
        marca.setLayout(new GridBagLayout());

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new javax.swing.BoxLayout(contenido, javax.swing.BoxLayout.Y_AXIS));

        EstiloUI.LogoGafas logo = new EstiloUI.LogoGafas(130, Color.WHITE);
        logo.setAlignmentX(0.5f);
        JLabel nombre = EstiloUI.titulo("Sistema Óptica", 28f, Color.WHITE);
        nombre.setAlignmentX(0.5f);
        JLabel lema = EstiloUI.subtitulo("Ventas, inventario y clientes", new Color(0xCCFBF1));
        lema.setAlignmentX(0.5f);

        contenido.add(javax.swing.Box.createHorizontalStrut(290));
        contenido.add(logo);
        contenido.add(javax.swing.Box.createVerticalStrut(22));
        contenido.add(nombre);
        contenido.add(javax.swing.Box.createVerticalStrut(8));
        contenido.add(lema);

        marca.add(contenido);
        return marca;
    }

    private JPanel crearPanelFormulario() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(340, 400));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        int fila = 0;

        gc.gridy = fila++;
        gc.insets = new Insets(0, 0, 4, 0);
        form.add(EstiloUI.titulo("Bienvenido", 26f, EstiloUI.TEXTO), gc);

        gc.gridy = fila++;
        gc.insets = new Insets(0, 0, 22, 0);
        form.add(EstiloUI.subtitulo("Inicia sesión para continuar", EstiloUI.TEXTO_SUAVE), gc);

        gc.gridy = fila++;
        gc.insets = new Insets(0, 0, 12, 0);
        form.add(EstiloUI.campoConEtiqueta("Usuario o correo", txtUsuario), gc);

        gc.gridy = fila++;
        gc.insets = new Insets(0, 0, 4, 0);
        form.add(EstiloUI.campoConEtiqueta("Contraseña", txtPassword), gc);

        gc.gridy = fila++;
        gc.insets = new Insets(0, 0, 8, 0);
        form.add(chkMostrar, gc);

        gc.gridy = fila++;
        gc.insets = new Insets(0, 0, 8, 0);
        form.add(lblMensaje, gc);

        gc.gridy = fila++;
        gc.insets = new Insets(0, 0, 18, 0);
        form.add(btnIngresar, gc);

        JPanel registro = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        registro.setOpaque(false);
        JLabel pregunta = EstiloUI.textoCentrado("¿No tienes cuenta?", EstiloUI.TEXTO_SUAVE);
        pregunta.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        registro.add(pregunta);
        registro.add(btnCrearCuenta);
        gc.gridy = fila;
        gc.insets = new Insets(0, 0, 0, 0);
        form.add(registro, gc);

        fondo.add(form);
        fondo.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        chkMostrar.addActionListener(e
                -> txtPassword.setEchoChar(chkMostrar.isSelected() ? (char) 0 : echoOriginal));
        btnIngresar.addActionListener(e -> intentarLogin());
        btnCrearCuenta.addActionListener(e -> new RegistroUsuarioDialog(this, usuario -> {
            txtUsuario.setText(usuario);
            txtPassword.requestFocusInWindow();
        }).setVisible(true));

        return fondo;
    }

    private void intentarLogin() {
        final String identificador = txtUsuario.getText();
        final char[] password = txtPassword.getPassword();
        EstiloUI.mostrarError(lblMensaje, null);
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
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause();
                    EstiloUI.mostrarError(lblMensaje, causa instanceof AuthException
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
