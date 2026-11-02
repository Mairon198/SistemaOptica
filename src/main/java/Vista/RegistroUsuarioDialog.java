package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import servicio.RegistroService;
import servicio.ValidacionException;
import util.SeguridadUtil;

public class RegistroUsuarioDialog extends JDialog {

    private final JToggleButton btnCliente = EstiloUI.botonSegmento("Cliente");
    private final JToggleButton btnVendedor = EstiloUI.botonSegmento("Vendedor");
    private final JTextField txtNombres = EstiloUI.campo(new JTextField());
    private final JTextField txtApellidos = EstiloUI.campo(new JTextField());
    private final JTextField txtDni = EstiloUI.campo(new JTextField());
    private final JTextField txtTelefono = EstiloUI.campo(new JTextField());
    private final JTextField txtDireccion = EstiloUI.campo(new JTextField());
    private final JTextField txtUsuario = EstiloUI.campo(new JTextField());
    private final JTextField txtEmail = EstiloUI.campo(new JTextField());
    private final JPasswordField txtPassword = EstiloUI.campo(new JPasswordField());
    private final JPasswordField txtConfirmar = EstiloUI.campo(new JPasswordField());
    private final JPasswordField txtCodigo = EstiloUI.campo(new JPasswordField());
    private final JCheckBox chkMostrar = EstiloUI.casilla("Mostrar contraseñas");
    private final JLabel lblMensaje = EstiloUI.mensajeError(40);
    private final JButton btnRegistrar = EstiloUI.botonPrimario("Crear cuenta");
    private final JButton btnCancelar = EstiloUI.botonSecundario("Cancelar");
    private final char echoOriginal = txtPassword.getEchoChar();

    private JComponent celdaDni;
    private JComponent celdaDireccion;
    private JComponent celdaCodigo;
    private JPanel raiz;

    private final RegistroService service = new RegistroService();
    private final Consumer<String> alRegistrar;

    public RegistroUsuarioDialog(JFrame padre, Consumer<String> alRegistrar) {
        super(padre, "Crear cuenta", Dialog.ModalityType.APPLICATION_MODAL);
        this.alRegistrar = alRegistrar;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirInterfaz();
        actualizarTipo();
        setResizable(false);
        setLocationRelativeTo(padre);
        getRootPane().setDefaultButton(btnRegistrar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void construirInterfaz() {
        raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Color.WHITE);

        JPanel encabezado = new EstiloUI.PanelDegradado();
        encabezado.setLayout(new BorderLayout(16, 0));
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(textos, javax.swing.BoxLayout.Y_AXIS));
        JLabel t = EstiloUI.titulo("Crear cuenta", 24f, Color.WHITE);
        JLabel s = EstiloUI.subtitulo("Completa tus datos para registrarte", new Color(0xCCFBF1));
        textos.add(t);
        textos.add(javax.swing.Box.createVerticalStrut(4));
        textos.add(s);
        encabezado.add(textos, BorderLayout.CENTER);
        encabezado.add(new EstiloUI.LogoGafas(70, Color.WHITE), BorderLayout.EAST);
        raiz.add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(20, 28, 8, 28));

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(btnCliente);
        grupo.add(btnVendedor);
        btnCliente.setSelected(true);
        JPanel segmentos = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        segmentos.setBackground(EstiloUI.FONDO_CLARO);
        segmentos.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        segmentos.add(btnCliente);
        segmentos.add(btnVendedor);
        JPanel contenedorSeg = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contenedorSeg.setOpaque(false);
        contenedorSeg.add(segmentos);

        int fila = 0;
        fila = agregar(cuerpo, fila, EstiloUI.campoConEtiqueta("Tipo de cuenta", contenedorSeg), null);
        fila = agregar(cuerpo, fila, EstiloUI.campoConEtiqueta("Nombres *", txtNombres),
                EstiloUI.campoConEtiqueta("Apellidos *", txtApellidos));
        fila = agregar(cuerpo, fila, EstiloUI.campoConEtiqueta("Usuario *", txtUsuario),
                EstiloUI.campoConEtiqueta("Correo *", txtEmail));
        fila = agregar(cuerpo, fila, EstiloUI.campoConEtiqueta("Contraseña *", txtPassword),
                EstiloUI.campoConEtiqueta("Confirmar contraseña *", txtConfirmar));

        celdaDni = EstiloUI.campoConEtiqueta("DNI", txtDni);
        fila = agregar(cuerpo, fila, EstiloUI.campoConEtiqueta("Teléfono", txtTelefono), celdaDni);

        celdaDireccion = EstiloUI.campoConEtiqueta("Dirección", txtDireccion);
        fila = agregar(cuerpo, fila, celdaDireccion, null);

        celdaCodigo = EstiloUI.campoConEtiqueta("Código de autorización *", txtCodigo);
        fila = agregar(cuerpo, fila, celdaCodigo, null);

        JLabel ayuda = EstiloUI.subtitulo("Contraseña de al menos 8 caracteres, con letras y números.  (*) obligatorio",
                EstiloUI.TEXTO_SUAVE);
        ayuda.setFont(EstiloUI.fuente(java.awt.Font.PLAIN, 11f));
        GridBagConstraints a = new GridBagConstraints();
        a.gridx = 0;
        a.gridy = fila++;
        a.gridwidth = 2;
        a.anchor = GridBagConstraints.WEST;
        a.insets = new Insets(2, 2, 0, 0);
        cuerpo.add(chkMostrar, a);
        a.gridy = fila++;
        a.insets = new Insets(2, 2, 6, 0);
        cuerpo.add(ayuda, a);

        GridBagConstraints m = new GridBagConstraints();
        m.gridx = 0;
        m.gridy = fila;
        m.gridwidth = 2;
        m.fill = GridBagConstraints.HORIZONTAL;
        m.weightx = 1;
        cuerpo.add(lblMensaje, m);
        raiz.add(cuerpo, BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridBagLayout());
        botones.setOpaque(false);
        botones.setBorder(BorderFactory.createEmptyBorder(8, 28, 22, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 0, 6);
        botones.add(btnCancelar, g);
        g.insets = new Insets(0, 6, 0, 0);
        botones.add(btnRegistrar, g);
        raiz.add(botones, BorderLayout.SOUTH);

        btnCliente.addActionListener(e -> actualizarTipo());
        btnVendedor.addActionListener(e -> actualizarTipo());
        chkMostrar.addActionListener(e -> {
            char eco = chkMostrar.isSelected() ? (char) 0 : echoOriginal;
            txtPassword.setEchoChar(eco);
            txtConfirmar.setEchoChar(eco);
        });
        btnRegistrar.addActionListener(e -> registrar());
        btnCancelar.addActionListener(e -> dispose());

        setContentPane(raiz);
    }

    private int agregar(JPanel panel, int fila, JComponent izquierda, JComponent derecha) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        if (derecha == null) {
            c.gridwidth = 2;
            c.insets = new Insets(0, 0, 12, 0);
            panel.add(izquierda, c);
        } else {
            c.insets = new Insets(0, 0, 12, 7);
            c.ipadx = 0;
            panel.add(izquierda, c);
            c.gridx = 1;
            c.insets = new Insets(0, 7, 12, 0);
            panel.add(derecha, c);
        }
        return fila + 1;
    }

    private boolean esVendedor() {
        return btnVendedor.isSelected();
    }

    private void actualizarTipo() {
        boolean vendedor = esVendedor();
        celdaDni.setVisible(!vendedor);
        celdaDireccion.setVisible(!vendedor);
        celdaCodigo.setVisible(vendedor);
        EstiloUI.mostrarError(lblMensaje, vendedor && !RegistroService.registroVendedorHabilitado()
                ? "El registro de vendedores no está habilitado en este equipo." : null);
        raiz.setPreferredSize(null);
        java.awt.Dimension d = raiz.getPreferredSize();
        raiz.setPreferredSize(new java.awt.Dimension(Math.max(640, d.width), d.height));
        pack();
    }

    private void registrar() {
        final boolean vendedor = esVendedor();
        final String nombres = txtNombres.getText();
        final String apellidos = txtApellidos.getText();
        final String dni = txtDni.getText();
        final String telefono = txtTelefono.getText();
        final String direccion = txtDireccion.getText();
        final String usuario = txtUsuario.getText();
        final String email = txtEmail.getText();
        final char[] password = txtPassword.getPassword();
        final char[] confirmar = txtConfirmar.getPassword();
        final char[] codigo = txtCodigo.getPassword();

        EstiloUI.mostrarError(lblMensaje, null);
        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Guardando...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (vendedor) {
                    service.registrarVendedor(nombres, apellidos, telefono, usuario, email,
                            password, confirmar, codigo);
                } else {
                    service.registrarCliente(nombres, apellidos, dni, telefono, direccion, usuario,
                            email, password, confirmar);
                }
                return null;
            }

            @Override
            protected void done() {
                SeguridadUtil.limpiar(password);
                SeguridadUtil.limpiar(confirmar);
                SeguridadUtil.limpiar(codigo);
                btnRegistrar.setEnabled(true);
                btnRegistrar.setText("Crear cuenta");
                try {
                    get();
                    JOptionPane.showMessageDialog(RegistroUsuarioDialog.this,
                            "Cuenta de " + (vendedor ? "vendedor" : "cliente") + " creada correctamente.\n"
                            + "Ya puede iniciar sesión con el usuario \"" + usuario.trim() + "\".",
                            "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                    alRegistrar.accept(usuario.trim());
                    dispose();
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause();
                    EstiloUI.mostrarError(lblMensaje, causa instanceof ValidacionException
                            ? causa.getMessage() : "Error inesperado al registrar la cuenta.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
