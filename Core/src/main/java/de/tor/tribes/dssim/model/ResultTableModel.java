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
package de.tor.tribes.dssim.model;

import de.tor.tribes.dssim.types.SimulatorResult;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
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
    private final List<String> columnNames = new LinkedList<>();
    private final List<Class> columnTypes = new LinkedList<>();
    private List<SimulatorResult> data = null;

    ResultTableModel() {
        super();
        
        columnNames.add("");columnTypes.add(String.class);
        for (UnitHolder unit : DataHolder.getSingleton().getSendableUnits()) {
            columnNames.add(unit.getName());
            columnTypes.add(Integer.class);
        }
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
        return columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return columnTypes.get(columnIndex);
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
                UnitHolder unit = DataHolder.getSingleton().getUnitByName(columnNames.get(pColIndex));
                return res.getOffBefore().getAmountForUnit(unit);
            }
        } else if (pRowId == 1) {
            if (pColIndex == 0) {
                return "Verluste";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = DataHolder.getSingleton().getUnitByName(columnNames.get(pColIndex));
                int before = res.getOffBefore().getAmountForUnit(unit);
                int after = res.getSurvivingOff().getAmountForUnit(unit);
                int losses = before - after;
                return losses;
            }
        } else {
            if (pColIndex == 0) {
                return "Überlebende";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = DataHolder.getSingleton().getUnitByName(columnNames.get(pColIndex));
                return res.getSurvivingOff().getAmountForUnit(unit);
            }
        }
    }

    public Object getDefenderValue(int pRowId, int pColIndex, int pDataset) {
        if (pRowId == 0) {
            if (pColIndex == 0) {
                return "Verteidiger";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = DataHolder.getSingleton().getUnitByName(columnNames.get(pColIndex));
                return res.getDefBefore().getAmountForUnit(unit);
            }
        } else if (pRowId == 1) {
            if (pColIndex == 0) {
                return "Verluste";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = DataHolder.getSingleton().getUnitByName(columnNames.get(pColIndex));
                int before = res.getDefBefore().getAmountForUnit(unit);
                int after = res.getSurvivingDef().getAmountForUnit(unit);
                int losses = before - after;
                return losses;
            }
        } else {
            if (pColIndex == 0) {
                return "Überlebende";
            } else {
                SimulatorResult res = data.get(pDataset);
                UnitHolder unit = DataHolder.getSingleton().getUnitByName(columnNames.get(pColIndex));
                return res.getSurvivingDef().getAmountForUnit(unit);
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
