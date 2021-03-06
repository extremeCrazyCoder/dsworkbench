/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.dssim.renderer;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Charon
 */
public class MultiFunctionCellRenderer extends DefaultTableCellRenderer {

    private ImageIcon loadIcon = null;
    private ImageIcon saveIcon = null;

    public MultiFunctionCellRenderer() {
        try {
            loadIcon = new ImageIcon(this.getClass().getResource("/res/ui/export2.png"));
            saveIcon = new ImageIcon(this.getClass().getResource("/res/ui/import1.png"));
        } catch (Exception e) {
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //if (row == 0 && (column == 0 || column > 4)) {
        if (row == 0 && (column == 0 || column == table.getColumnCount() - 1)) {
            JButton b = new JButton("");
            b.setIcon(loadIcon);
            if (column == 0) {
                b.setToolTipText("Off laden");
            } else {
                b.setToolTipText("Deff laden");
            }
            return b;
        //} else if (row == 1 && (column == 0 || column > 4)) {
        } else if (row == 1 && (column == 0 || column == table.getColumnCount() - 1)) {
            JButton b = new JButton("");
            if (column == 0) {
                b.setToolTipText("Off speichern");
            } else {
                b.setToolTipText("Deff speichern");
            }
            b.setIcon(saveIcon);
            return b;
        }
        return new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
