package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;

public final class EstiloUI {

    public static final Color PRIMARIO = new Color(0x0F766E);
    public static final Color PRIMARIO_HOVER = new Color(0x0D9488);
    public static final Color PRIMARIO_PRESIONADO = new Color(0x115E59);
    public static final Color AZUL_OSCURO = new Color(0x0B2545);
    public static final Color TEXTO = new Color(0x1F2937);
    public static final Color TEXTO_SUAVE = new Color(0x6B7280);
    public static final Color BORDE = new Color(0xD1D5DB);
    public static final Color FONDO_CLARO = new Color(0xF3F4F6);
    public static final Color ERROR = new Color(0xB91C1C);
    public static final Color ERROR_FONDO = new Color(0xFEE2E2);

    private EstiloUI() {
    }

    public static Font fuente(int estilo, float tamano) {
        return new Font("Segoe UI", estilo, 12).deriveFont(estilo, tamano);
    }

    private static void suavizar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.BOLD, 12f));
        l.setForeground(TEXTO);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
        return l;
    }

    public static JLabel titulo(String texto, float tamano, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.BOLD, tamano));
        l.setForeground(color);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        return l;
    }

    public static JLabel subtitulo(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.PLAIN, 13f));
        l.setForeground(color);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));
        return l;
    }

    public static JLabel mensajeError(int alto) {
        JLabel l = new JLabel(" ");
        l.setFont(fuente(Font.PLAIN, 12f));
        l.setForeground(ERROR);
        l.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        l.setPreferredSize(new Dimension(10, alto));
        l.setMinimumSize(new Dimension(10, alto));
        return l;
    }

    public static void mostrarError(JLabel l, String texto) {
        if (texto == null || texto.isBlank()) {
            l.setOpaque(false);
            l.setText(" ");
        } else {
            l.setOpaque(true);
            l.setBackground(ERROR_FONDO);
            l.setText("<html>" + texto + "</html>");
        }
        l.repaint();
    }

    public static <T extends JTextField> T campo(T c) {
        c.setFont(fuente(Font.PLAIN, 14f));
        c.setForeground(TEXTO);
        c.setBackground(Color.WHITE);
        c.setCaretColor(PRIMARIO);
        c.setBorder(new BordeRedondeado(BORDE, 1));
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                c.setBorder(new BordeRedondeado(PRIMARIO, 2));
                c.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                c.setBorder(new BordeRedondeado(BORDE, 1));
                c.repaint();
            }
        });
        return c;
    }

    public static JCheckBox casilla(String texto) {
        JCheckBox c = new JCheckBox(texto);
        c.setOpaque(false);
        c.setFont(fuente(Font.PLAIN, 12f));
        c.setForeground(TEXTO_SUAVE);
        c.setFocusPainted(false);
        c.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return c;
    }

    public static JPanel campoConEtiqueta(String texto, JComponent campo) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS));
        JLabel l = etiqueta(texto);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 0));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(campo);
        return p;
    }

    public static JButton botonPrimario(String texto) {
        return new BotonRedondeado(texto, PRIMARIO, PRIMARIO_HOVER, PRIMARIO_PRESIONADO, Color.WHITE, null);
    }

    public static JButton botonSecundario(String texto) {
        return new BotonRedondeado(texto, Color.WHITE, FONDO_CLARO, BORDE, TEXTO, BORDE);
    }

    public static JButton botonEnlace(String texto) {
        JButton b = new JButton(texto);
        b.setFont(fuente(Font.BOLD, 13f));
        b.setForeground(PRIMARIO);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setMargin(new Insets(0, 4, 0, 4));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(PRIMARIO_PRESIONADO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(PRIMARIO);
            }
        });
        return b;
    }

    public static JToggleButton botonSegmento(String texto) {
        return new BotonSegmento(texto);
    }

    private static final class BordeRedondeado extends AbstractBorder {

        private final Color color;
        private final int grosor;

        BordeRedondeado(Color color, int grosor) {
            this.color = color;
            this.grosor = grosor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(grosor));
            float d = grosor / 2f;
            g2.draw(new RoundRectangle2D.Float(x + d, y + d, w - grosor, h - grosor, 12, 12));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(10, 14, 10, 14);
        }
    }

    private static final class BotonRedondeado extends JButton {

        private final Color fondo;
        private final Color hover;
        private final Color presionado;
        private final Color borde;

        BotonRedondeado(String texto, Color fondo, Color hover, Color presionado, Color textoColor, Color borde) {
            super(texto);
            this.fondo = fondo;
            this.hover = hover;
            this.presionado = presionado;
            this.borde = borde;
            setForeground(textoColor);
            setFont(fuente(Font.BOLD, 14f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setMargin(new Insets(10, 18, 10, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, Math.max(d.height, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            Color c = !isEnabled() ? new Color(0x9CA3AF)
                    : getModel().isPressed() ? presionado
                    : getModel().isRollover() ? hover : fondo;
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            if (borde != null) {
                g2.setColor(borde);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class BotonSegmento extends JToggleButton {

        BotonSegmento(String texto) {
            super(texto);
            setFont(fuente(Font.BOLD, 13f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(150, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            setForeground(isSelected() ? Color.WHITE : TEXTO_SUAVE);
            if (isSelected()) {
                g2.setColor(PRIMARIO);
                g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 10, 10));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class PanelDegradado extends JPanel {

        public PanelDegradado() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, AZUL_OSCURO, w, h, PRIMARIO));
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(255, 255, 255, 18));
            g2.fill(new Ellipse2D.Float(w * 0.55f, -h * 0.25f, h * 0.7f, h * 0.7f));
            g2.setColor(new Color(255, 255, 255, 14));
            g2.fill(new Ellipse2D.Float(-w * 0.25f, h * 0.65f, w * 0.8f, w * 0.8f));
            g2.dispose();
        }
    }

    public static class LogoGafas extends JComponent {

        private final int ancho;
        private final Color color;

        public LogoGafas(int ancho, Color color) {
            this.ancho = ancho;
            this.color = color;
            setPreferredSize(new Dimension(ancho, ancho / 2));
            setMinimumSize(getPreferredSize());
            setMaximumSize(getPreferredSize());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            float trazo = Math.max(3f, ancho / 22f);
            g2.setStroke(new BasicStroke(trazo, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(color);
            float lente = ancho * 0.38f;
            float y = (getHeight() - lente) / 2f;
            float x1 = trazo;
            float x2 = ancho - lente - trazo;
            g2.draw(new Ellipse2D.Float(x1, y, lente, lente));
            g2.draw(new Ellipse2D.Float(x2, y, lente, lente));
            float cy = y + lente * 0.4f;
            g2.drawArc((int) (x1 + lente), (int) (cy - lente * 0.25f), (int) (x2 - x1 - lente), (int) (lente * 0.5f), 0, 180);
            g2.drawLine((int) x1, (int) (y + lente * 0.3f), 0, (int) (y + lente * 0.05f));
            g2.drawLine((int) (x2 + lente), (int) (y + lente * 0.3f), ancho, (int) (y + lente * 0.05f));
            g2.dispose();
        }
    }

    public static JLabel textoCentrado(String texto, Color color) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(fuente(Font.PLAIN, 13f));
        l.setForeground(color);
        return l;
    }
}
