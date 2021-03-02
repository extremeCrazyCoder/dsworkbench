/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.renderer;

import de.tor.tribes.dssim.Constants;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Charon
 */
public class TableHeaderRenderer extends DefaultTableCellRenderer {

    public TableHeaderRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
        c.setBackground(Constants.DS_BACK);
        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        return c;
    }
}
