/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.editor;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Charon
 */
public class SpreadSheetCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JTextField mEditor = new JTextField();

    public SpreadSheetCellEditor() {
        mEditor.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                mEditor.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                mEditor.select(0, 0);
            }
        });
        mEditor.setMargin(new Insets(-2, -2, -2, -2));
    }

    @Override
    public Object getCellEditorValue() {
        try {
            //checkMultipleInsert(mEditor.getText());
            return Integer.parseInt(mEditor.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    /*
     * private void checkMultipleInsert(String pValue) { int units = UnitManager.getSingleton().getUnits().length; StringTokenizer t = new
     * StringTokenizer(pValue, " \t"); if (t.countTokens() == units) { HashMap<UnitHolder, Integer> unitMap = new HashMap<UnitHolder,
     * Integer>(); for (UnitHolder unit : UnitManager.getSingleton().getUnits()) { try { Integer amount = Integer.parseInt(t.nextToken());
     * unitMap.put(unit, amount); } catch (Exception e) { }
     *
     * if (unitMap.size() == units) { cancelCellEditing(); DSWorkbenchSimulatorFrame.getSingleton().insertMultipleUnits(unitMap); } } }
    }
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        mEditor.setText(Integer.toString((Integer) value));
        mEditor.setHorizontalAlignment(SwingConstants.RIGHT);
        return mEditor;
    }
}
