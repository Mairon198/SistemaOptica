
package com.mycompany.sistemaoptica;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import vista.LoginFrame;

/**
 *
 * @author Mairon
 */
public class SistemaOptica {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // se usa el aspecto por defecto
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
