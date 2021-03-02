/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.editor;

import de.tor.tribes.dssim.Constants;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Charon
 */
public class TechLevelCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JComboBox mEditor = new JComboBox();

    public TechLevelCellEditor(int pTechLevels) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 1; i <= pTechLevels; i++) {
            model.addElement(i);
        }
        mEditor.setModel(model);
        mEditor.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    fireEditingStopped();
                }
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        double index = mEditor.getSelectedIndex() + 1;
        if (index == 0.0) {
            return (Double) 1.0;
        }
        return (Double) index;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected,
            int row,
            int column) {
        mEditor.setSelectedItem(value);














        if (isSelected) {
            mEditor.setBackground(Constants.DS_BACK);
        } else {
            mEditor.setBackground(Constants.DS_BACK_LIGHT);
        }
        return mEditor;
    }
}

