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

import de.tor.tribes.dssim.types.TechState;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.ServerSettings;
import javax.swing.table.DefaultTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Charon
 */
public class SimulatorTableModel extends DefaultTableModel {
    private static Logger logger = LogManager.getLogger("SimulatorTableModel");

    private static SimulatorTableModel SINGLETON = null;
    private Class[] columnClasses;
    private String[] columnNames;

    SimulatorTableModel() {
        super();
        if (ServerSettings.getSingleton().getTechType() == ServerSettings.SIMPLE_TECH) {
            columnNames = new String[]{"", "Einheit", "Angreifer", "", "Verteidiger", ""};
            columnClasses = new Class[]{Object.class, UnitHolder.class, Integer.class, Object.class, Integer.class, Object.class};
        } else {
            columnNames = new String[]{"", "Einheit", "Angreifer", "Tech", "", "Verteidiger", "Tech", ""};
            columnClasses = new Class[]{Object.class, UnitHolder.class, Integer.class, Double.class, Object.class, Integer.class, Double.class, Object.class};
        }
    }

    public static synchronized SimulatorTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new SimulatorTableModel();
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
        if (!(getValueAt(rowIndex, columnIndex) instanceof UnitHolder)) {
            return true;
        }
        return false;
    }

    public TroopAmountFixed getOffTroops() {
        TroopAmountFixed resTroop = new TroopAmountFixed();
        if (ServerSettings.getSingleton().getTechType() == ServerSettings.SIMPLE_TECH) {
            //return new world values
            for (int i = 0; i < getRowCount(); i++) {
                Integer count = (Integer) getValueAt(i, 2);
                //add element if cout larger than 0
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                resTroop.setAmountForUnit(unit, count);
            }//end of all rows
        } else {
            for (int i = 0; i < getRowCount(); i++) {
                int count = (Integer) getValueAt(i, 2);
                //add element if cout larger than 0
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                resTroop.setAmountForUnit(unit, count);
            }//end of all rows
        }//end of getting table
        //return final result
        return resTroop;
    }

    public TechState getOffTech() {
        TechState resTech = new TechState();
        if (ServerSettings.getSingleton().getTechType() == ServerSettings.SIMPLE_TECH) {
            resTech.fill(1);
        } else {
            for (int i = 0; i < getRowCount(); i++) {
                //add element if cout larger than 0
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                int tech = 1;
                Object val = getValueAt(i, 3);

                if (val instanceof Double) {
                    tech = (int) Math.rint((Double) val);
                } else {
                    tech = (Integer) val;
                }
                resTech.setTechLevel(unit, tech);
            }//end of all rows
        }//end of getting table
        //return final result
        return resTech;
    }

    public TroopAmountFixed getDefTroops() {
        TroopAmountFixed resTroop = new TroopAmountFixed();
        if (ServerSettings.getSingleton().getTechType() == ServerSettings.SIMPLE_TECH) {
            //return new world values
            for (int i = 0; i < getRowCount(); i++) {
                Integer count = (Integer) getValueAt(i, 4);
                //add element if cout larger than 0
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                resTroop.setAmountForUnit(unit, count);
            }//end of all rows
        } else {
            for (int i = 0; i < getRowCount(); i++) {
                Integer count = (Integer) getValueAt(i, 5);
                //add element if cout larger than 0
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                resTroop.setAmountForUnit(unit, count);
            }//end of all rows
        }//end of getting table
        //return final result
        return resTroop;
    }

    public TechState getDefTech() {
        TechState resTech = new TechState();
        if (ServerSettings.getSingleton().getTechType() == ServerSettings.SIMPLE_TECH) {
            resTech.fill(1);
        } else {
            for (int i = 0; i < getRowCount(); i++) {
                //add element if cout larger than 0
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                int tech = 1;
                Object val = getValueAt(i, 6);

                if (val instanceof Double) {
                    tech = (int) Math.rint((Double) val);
                } else {
                    tech = (Integer) val;
                }

                resTech.setTechLevel(unit, tech);
            }//end of all rows
        }//end of getting table
        //return final result
        return resTech;
    }

    public void setOffUnitCount(UnitHolder pUnit, int pCount) {
        for (int i = 0; i < getRowCount(); i++) {
            UnitHolder unit = (UnitHolder) getValueAt(i, 1);
            if (unit.equals(pUnit)) {
                setValueAt(pCount, i, 2);
                return;
            }
        }
    }

    public void setDefUnitCount(UnitHolder pUnit, int pCount) {
        if (ServerSettings.getSingleton().getTechType() == ServerSettings.SIMPLE_TECH) {
            for (int i = 0; i < getRowCount(); i++) {
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                if (unit.equals(pUnit)) {
                    setValueAt(pCount, i, 4);
                    return;
                }
            }
        } else {
            for (int i = 0; i < getRowCount(); i++) {
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                if (unit.equals(pUnit)) {
                    setValueAt(pCount, i, 5);
                    return;
                }
            }
        }
    }

    public void setDef(TroopAmountFixed pTroops, TechState pTech) {
        if (ServerSettings.getSingleton().getTechType() == ServerSettings.SIMPLE_TECH) {
            //return new world values
            for (int i = 0; i < getRowCount(); i++) {
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                int count = pTroops.getAmountForUnit(unit);
                if (count >= 0) {
                    setValueAt(count, i, 4);
                } else {
                    setValueAt(0, i, 4);
                }
            }//end of all rows
        } else {
            for (int i = 0; i < getRowCount(); i++) {
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                int count = pTroops.getAmountForUnit(unit);
                if (count >= 0) {
                    setValueAt(pTroops.getAmountForUnit(unit), i, 5);
                } else {
                    setValueAt(0, i, 5);
                }
                if(pTech != null) {
                    setValueAt(pTech.getTechLevel(unit), i, 6);
                }
            }//end of all rows
        }//end of getting table
    }

    public void setOff(TroopAmountFixed pTroops, TechState pTech) {
        if (ServerSettings.getSingleton().getTechType() == ServerSettings.SIMPLE_TECH) {
            //return new world values
            for (int i = 0; i < getRowCount(); i++) {
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                int count = pTroops.getAmountForUnit(unit);
                if (count >= 0) {
                    setValueAt(count, i, 2);
                } else {
                    setValueAt(0, i, 2);
                }
            }//end of all rows
        } else {
            for (int i = 0; i < getRowCount(); i++) {
                UnitHolder unit = (UnitHolder) getValueAt(i, 1);
                int count = pTroops.getAmountForUnit(unit);
                if (count >= 0) {
                    setValueAt(pTroops.getAmountForUnit(unit), i, 2);
                } else {
                    setValueAt(0, i, 2);
                }
                if(pTech != null) {
                    setValueAt(pTech.getTechLevel(unit), i, 3);
                }
            }//end of all rows
        }//end of getting table
    }

    @Override
    public void setValueAt(Object pValue, int pRow, int pCol) {
        try {
            super.setValueAt(pValue, pRow, pCol);
            fireTableDataChanged();
        } catch (Exception e) {
            //setting data failed somehow
        }
    }
}
