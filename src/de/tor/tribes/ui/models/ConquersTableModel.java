/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.ui.models;

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.types.Ally;
import de.tor.tribes.types.BarbarianAlly;
import de.tor.tribes.types.Barbarians;
import de.tor.tribes.types.Conquer;
import de.tor.tribes.types.NoAlly;
import de.tor.tribes.types.Tribe;
import de.tor.tribes.types.Village;
import de.tor.tribes.ui.DSWorkbenchConquersFrame;
import de.tor.tribes.ui.DSWorkbenchMainFrame;
import de.tor.tribes.util.DSCalculator;
import de.tor.tribes.util.conquer.ConquerManager;
import de.tor.tribes.util.conquer.ConquerManagerListener;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Charon
 */
public class ConquersTableModel extends AbstractDSWorkbenchTableModel {

    private final String PROPERTY_BASE_ID = "conquers.table.model";
    protected static Class[] types;
    protected static String[] colNames;
    protected static List<String> internalNames;
    private static ConquersTableModel SINGLETON = null;

    public static synchronized ConquersTableModel getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ConquersTableModel();
        }
        return SINGLETON;
    }

    static {
        types = new Class[]{Village.class, String.class, String.class, Tribe.class, Ally.class, Tribe.class, Ally.class, Integer.class, Double.class};
        colNames = new String[]{"Dorf", "Kontinent", "Geadelt am", "Verlierer", "Stamm", "Gewinner", "Stamm", "Zustimmung", "Entfernung"};
        internalNames = Arrays.asList(new String[]{"Dorf", "Kontinent", "Geadelt am", "Verlierer", "Stamm (Verlierer)", "Gewinner", "Stamm (Gewinner)", "Zustimmung", "Entfernung"});
    }

    ConquersTableModel() {
        ConquerManager.getSingleton().addConquerManagerListener(new ConquerManagerListener() {

            @Override
            public void fireConquersChangedEvent() {
                fireTableDataChanged();
            }
        });
    }

    @Override
    public int getRowCount() {
        int cnt = ConquerManager.getSingleton().getConquerCount();
        return cnt;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Conquer c = ConquerManager.getSingleton().getConquer(rowIndex);
        columnIndex = getRealColumnId(columnIndex);
        switch (columnIndex) {
            case 0:
                return DataHolder.getSingleton().getVillagesById().get(c.getVillageID());
            case 1: {
                Village v = DataHolder.getSingleton().getVillagesById().get(c.getVillageID());
                return "K" + DSCalculator.getContinent(v.getX(), v.getY());
            }
            case 2: {
                SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                return f.format(new Date((long) c.getTimestamp() * 1000));
            }
            case 3: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getLoser());
                if (t == null) {
                    return Barbarians.getSingleton();
                } else {
                    return t;
                }
            }
            case 4: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getLoser());
                if (t == null) {
                    return BarbarianAlly.getSingleton();
                } else if (t.getAlly() == null) {
                    return NoAlly.getSingleton();
                } else {
                    return t.getAlly();
                }
            }
            case 5: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getWinner());
                if (t == null) {
                    return Barbarians.getSingleton();
                } else {
                    return t;
                }
            }
            case 6: {
                Tribe t = DataHolder.getSingleton().getTribes().get(c.getWinner());
                if (t == null) {
                    return BarbarianAlly.getSingleton();
                } else if (t.getAlly() == null) {
                    return NoAlly.getSingleton();
                } else {
                    return t.getAlly();
                }
            }
            case 7: {
                return c.getCurrentAcceptance();
            }
            default: {
                Village v = DataHolder.getSingleton().getVillagesById().get(c.getVillageID());
                Village vUser = DSWorkbenchMainFrame.getSingleton().getCurrentUserVillage();
                if (vUser != null) {
                    return DSCalculator.calculateDistance(v, vUser);
                } else {
                    return 0;
                }


            }
        }
    }

    @Override
    public String getPropertyBaseID() {
        return PROPERTY_BASE_ID;
    }

    @Override
    public Class[] getColumnClasses() {
        return types;
    }

    @Override
    public String[] getColumnNames() {
        return colNames;
    }

    @Override
    public List<String> getInternalColumnNames() {
        return internalNames;
    }

    @Override
    public void doNotifyOnColumnChange() {
        DSWorkbenchConquersFrame.getSingleton().fireConquersChangedEvent();
    }
}
