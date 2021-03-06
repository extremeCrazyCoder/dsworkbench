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

import de.tor.tribes.util.Constants;
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

