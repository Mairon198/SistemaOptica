package vista;

import java.awt.Component;
import java.math.BigDecimal;
import java.util.function.Function;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import servicio.ValidacionException;


final class UtilVista {

    private UtilVista() {
    }


    interface Accion {
        void ejecutar() throws ValidacionException;
    }


    static void ejecutar(Component padre, Accion accion) {
        try {
            accion.ejecutar();
        } catch (ValidacionException ex) {
            JOptionPane.showMessageDialog(padre, ex.getMessage(), "Atención", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(padre,
                    "No se pudo completar la operación.\nVerifique la conexión con la base de datos.\n\n"
                    + ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Modelo de tabla de solo lectura. */
    static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

 
    static <T> ListCellRenderer<Object> renderer(Function<T, String> texto) {
        return new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                setText(valor == null ? " " : texto.apply((T) valor));
                return this;
            }
        };
    }

    static String dinero(BigDecimal valor) {
        return String.format("L %,.2f", valor == null ? BigDecimal.ZERO : valor);
    }

    static String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    // ---------- lectura de campos ----------

    static String requerido(String valor, String campo) throws ValidacionException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo \"" + campo + "\" es obligatorio.");
        }
        return valor.trim();
    }

    static String opcional(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    static BigDecimal decimal(String valor, String campo, boolean obligatorio) throws ValidacionException {
        String limpio = valor == null ? "" : valor.trim().replace(",", "").replace("L", "").trim();
        if (limpio.isEmpty()) {
            if (obligatorio) {
                throw new ValidacionException("El campo \"" + campo + "\" es obligatorio.");
            }
            return null;
        }
        try {
            return new BigDecimal(limpio);
        } catch (NumberFormatException ex) {
            throw new ValidacionException("El campo \"" + campo + "\" debe ser un número. Ejemplo: 1500.00");
        }
    }

    static int entero(String valor, String campo) throws ValidacionException {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new ValidacionException("El campo \"" + campo + "\" es obligatorio.");
        }
        try {
            return Integer.parseInt(limpio);
        } catch (NumberFormatException ex) {
            throw new ValidacionException("El campo \"" + campo + "\" debe ser un número entero.");
        }
    }
}
