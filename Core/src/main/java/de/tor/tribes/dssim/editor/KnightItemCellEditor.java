/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.editor;

import de.tor.tribes.dssim.Constants;
import de.tor.tribes.dssim.types.KnightItem;
import de.tor.tribes.dssim.util.ConfigManager;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Jejkal
 */
public class KnightItemCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JComboBox mEditor = new JComboBox();

    public KnightItemCellEditor() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 0; i <= KnightItem.ID_SNOB; i++) {
            model.addElement(KnightItem.factoryKnightItem(i));
        }
        mEditor.setModel(model);
        mEditor.setSelectedIndex(0);
    }

    @Override
    public Object getCellEditorValue() {
        return (KnightItem) mEditor.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        boolean useEditor = false;
        try {
            //use editor only if knight items are used and we are in the knight row
            useEditor = ((ConfigManager.getSingleton().getKnightType() == ConfigManager.ID_KNIGHT_WITH_ITEMS) &&
                    (table.getValueAt(row, 1).equals("knight")));
        } catch (Exception e) {
        }
        if (useEditor) {
            mEditor.setSelectedItem(value);
            if (isSelected) {
                mEditor.setBackground(Constants.DS_BACK);
            } else {
                mEditor.setBackground(Constants.DS_BACK_LIGHT);
            }
            return mEditor;
        }
        return new JLabel("");
    }
}
