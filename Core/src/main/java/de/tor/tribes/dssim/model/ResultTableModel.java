/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.model;

import de.tor.tribes.dssim.types.SimulatorResult;
import de.tor.tribes.dssim.types.UnitHolder;
import de.tor.tribes.dssim.util.UnitManager;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Charon
 */
public class ResultTableModel extends AbstractTableModel {

    private static ResultTableModel SINGLETON = null;
    private Class[] columnClasses;
    private String[] columnNames;
    private List<SimulatorResult> data = null;

    ResultTableModel() {
        super();
        String[] ordered = new String[]{"", "spear", "sword", "axe", "archer", "spy", "light", "marcher", "heavy", "ram", "catapult", "knight", "snob", "militia"};
        List<String> columns = new LinkedList<>();
        List<Class> classes = new LinkedList<>();
        columns.add("");
        classes.add(String.class);
        for (String col : ordered) {
            if (UnitManager.getSingleton().getUnitByPlainName(col) != null) {
                columns.add(col);
                classes.add(Integer.class);
            }
        }
        columnNames = columns.toArray(new String[]{});
        columnClasses = classes.toArray(new Class[]{});
        data = new LinkedList<>();
    }

    public static synchronized ResultTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ResultTableModel();
        }
        return SINGLETON;
    }

    public void reset() {
        SINGLETON = null;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void clear() {
        data.clear();
        fireTableDataChanged();
    }

    public void addResult(SimulatorResult pResult) {
        data.add(0, pResult);
    }

    public int getRowCount() {
        return data.size() * 9;// + (data.size() - 1);
    }

    public int getDataSetNumberForRow(int pRow) {
        return pRow / 9;
    }

    public SimulatorResult getResult(int pResultId) {
        if (data.size() - 1 < pResultId) {
            return null;
        }
        return data.get(pResultId);
    }

    public void removeResults(Integer[] pSelection) {
        if (pSelection == null || pSelection.length == 0) {
            return;
        }
        Arrays.sort(pSelection);
        for (int i = pSelection.length - 1; i >= 0; i--) {
            data.remove(pSelection[i].intValue());
        }
        fireTableDataChanged();
    }

    public boolean isAttackerRow(int pRow) {
        int row = 0;
        if (pRow == 0) {
            return false;
        }
        if (pRow > 0) {
            //first row of one result
            row = pRow % 9;
        }
        return (row == 1 || row == 2 || row == 3);
    }

    public boolean isDefenderRow(int pRow) {
        int row = 0;
        if (pRow == 0) {
            return false;
        }
        if (pRow > 0) {
            //first row of one result
            row = pRow % 9;
        }
        return (row == 5 || row == 6 || row == 7);
    }

    public boolean isMiscRow(int pRow) {
        return (!isDefenderRow(pRow) && !isAttackerRow(pRow));
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int row = 0;
        if (rowIndex > 0) {
            //first row of one result
            row = rowIndex % 9;
        }
        int dataSet = rowIndex / 9;

        switch (row) {
            case 0: {
                if (columnIndex == 0) {
                    return "Ergebnis " + (data.size() - dataSet);
                } else {
                    return "";
                }
            }
            case 1:
                return getAttackValue(row - 1, columnIndex, dataSet);
            case 2:
                return getAttackValue(row - 1, columnIndex, dataSet);
            case 3:
                return getAttackValue(row - 1, columnIndex, dataSet);
            case 4:
                return "";
            case 5:
                return getDefenderValue(row - 5, columnIndex, dataSet);
            case 6:
                return getDefenderValue(row - 5, columnIndex, dataSet);
            case 7:
                return getDefenderValue(row - 5, columnIndex, dataSet);
            case 8:
                return getMiscValue(columnIndex, dataSet);
        }

        return "";
    }

    public Object getAttackValue(int pRowId, int pColIndex, int pDataset) {
        if (pRowId == 0) {
            if (pColIndex == 0) {
                return "Angreifer";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = UnitManager.getSingleton().getUnitByPlainName(columnNames[pColIndex]);
                return res.getOffBefore().get(unit).getCount();
            }
        } else if (pRowId == 1) {
            if (pColIndex == 0) {
                return "Verluste";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = UnitManager.getSingleton().getUnitByPlainName(columnNames[pColIndex]);
                int before = res.getOffBefore().get(unit).getCount();
                int after = res.getSurvivingOff().get(unit).getCount();
                int losses = before - after;
                return losses;
            }
        } else {
            if (pColIndex == 0) {
                return "Überlebende";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = UnitManager.getSingleton().getUnitByPlainName(columnNames[pColIndex]);
                return res.getSurvivingOff().get(unit).getCount();
            }
        }
    }

    public Object getDefenderValue(int pRowId, int pColIndex, int pDataset) {
        if (pRowId == 0) {
            if (pColIndex == 0) {
                return "Verteidiger";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = UnitManager.getSingleton().getUnitByPlainName(columnNames[pColIndex]);
                return res.getDefBefore().get(unit).getCount();
            }
        } else if (pRowId == 1) {
            if (pColIndex == 0) {
                return "Verluste";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = UnitManager.getSingleton().getUnitByPlainName(columnNames[pColIndex]);
                int before = res.getDefBefore().get(unit).getCount();
                int after = res.getSurvivingDef().get(unit).getCount();
                int losses = before - after;
                return losses;
            }
        } else {
            if (pColIndex == 0) {
                return "Überlebende";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = UnitManager.getSingleton().getUnitByPlainName(columnNames[pColIndex]);
                return res.getSurvivingDef().get(unit).getCount();
            }
        }
    }

    public Object getMiscValue(int pColIndex, int pDataset) {
        SimulatorResult res = data.get(pDataset);
        switch (pColIndex) {
            case 1: {
                return "Wall";
            }
            case 2: {
                int diff = res.getWallBefore() - res.getWallLevel();
                return ((diff > 0) ? "- " : "+/- ") + diff;
            }
            case 4: {
                return "Gebäude";
            }
            case 5: {

                int diff = res.getBuildingBefore() - res.getBuildingLevel();
                return ((diff > 0) ? "- " : "+/- ") + diff;
            }
        }
        return "";
    }
}
