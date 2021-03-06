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
package de.tor.tribes.dssim.editor;

import de.tor.tribes.dssim.ui.TroopsLoadDialog;
import de.tor.tribes.dssim.ui.TroopsSaveDialog;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Charon
 */
public class MultiCellEditor extends AbstractCellEditor implements TableCellEditor {

    private ImageIcon loadIcon = null;
    private ImageIcon saveIcon = null;
    private JButton loadOffButton = null;
    private JButton saveOffButton = null;
    private JButton loadDefButton = null;
    private JButton saveDefButton = null;
    private MouseListener l = null;

    public MultiCellEditor() {
        try {
            loadIcon = new ImageIcon(this.getClass().getResource("/res/icons/export2.png"));
            saveIcon = new ImageIcon(this.getClass().getResource("/res/icons/import1.png"));
        } catch (Exception e) {
        }
        l = new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (e.getSource() == loadOffButton) {
                    TroopsLoadDialog.getSingleton().showLoadOffDialog();
                } else if (e.getSource() == saveOffButton) {
                    TroopsSaveDialog.getSingleton().showSaveOffDialog();
                } else if (e.getSource() == loadDefButton) {
                    TroopsLoadDialog.getSingleton().showLoadDefDialog();
                } else if (e.getSource() == saveDefButton) {
                    TroopsSaveDialog.getSingleton().showSaveDefDialog();
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        };
        loadOffButton = new JButton("");
        loadOffButton.setIcon(loadIcon);
        loadOffButton.setToolTipText("Off laden");
        saveOffButton = new JButton("");
        saveOffButton.setIcon(saveIcon);
        saveOffButton.setToolTipText("Off speichern");
        loadDefButton = new JButton("");
        loadDefButton.setIcon(loadIcon);
        loadDefButton.setToolTipText("Deff laden");
        saveDefButton = new JButton("");
        saveDefButton.setIcon(saveIcon);
        saveDefButton.setToolTipText("Deff speichern");
        loadOffButton.addMouseListener(l);
        saveOffButton.addMouseListener(l);
        loadDefButton.addMouseListener(l);
        saveDefButton.addMouseListener(l);
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (row == 0 && (column == 0 || column > 4)) {
            if (column == 0) {
                l.mouseClicked(new MouseEvent(loadOffButton, 0, 0, 0, 0, 0, 0, false));
                return loadOffButton;
            } else {
                l.mouseClicked(new MouseEvent(loadDefButton, 0, 0, 0, 0, 0, 0, false));
                return loadDefButton;
            }
        } else if (row == 1 && (column == 0 || column > 4)) {
            if (column == 0) {
                l.mouseClicked(new MouseEvent(saveOffButton, 0, 0, 0, 0, 0, 0, false));
                return saveOffButton;
            } else {
                l.mouseClicked(new MouseEvent(saveDefButton, 0, 0, 0, 0, 0, 0, false));
                return saveDefButton;
            }
        }
        return null;

    }
}
