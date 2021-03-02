/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.renderer;

import de.tor.tribes.dssim.Constants;
import de.tor.tribes.dssim.util.ImageManager;
import de.tor.tribes.dssim.util.UnitManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Charon
 */
public class UnitTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        JLabel label = (JLabel) c;
        if (row == -1) {
            //set only header background to dark_ds
            label.setBackground(Constants.DS_BACK);
        }
        label.setIcon(ImageManager.getUnitIcon(UnitManager.getSingleton().getUnitByPlainName((String) value)));
        label.setText("");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
