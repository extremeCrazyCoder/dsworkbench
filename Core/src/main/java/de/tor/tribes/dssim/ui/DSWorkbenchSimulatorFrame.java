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
package de.tor.tribes.dssim.ui;

import de.tor.tribes.dssim.algo.AbstractSimulator;
import de.tor.tribes.dssim.algo.NewSimulator;
import de.tor.tribes.dssim.algo.OldSimulator;
import de.tor.tribes.dssim.editor.MultiCellEditor;
import de.tor.tribes.dssim.editor.SpreadSheetCellEditor;
import de.tor.tribes.dssim.editor.TechLevelCellEditor;
import de.tor.tribes.dssim.model.ResultTableModel;
import de.tor.tribes.dssim.model.SimulatorTableModel;
import de.tor.tribes.dssim.renderer.MultiFunctionCellRenderer;
import de.tor.tribes.dssim.renderer.TableHeaderRenderer;
import de.tor.tribes.dssim.types.AStarResultReceiver;
import de.tor.tribes.dssim.types.SimulatorResult;
import de.tor.tribes.dssim.types.TechState;
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.renderer.UnitCellRenderer;
import de.tor.tribes.ui.renderer.UnitTableHeaderRenderer;
import de.tor.tribes.ui.windows.AbstractDSWorkbenchFrame;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.translation.TranslationManager;
import de.tor.tribes.util.translation.Translator;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Charon
 */
public class DSWorkbenchSimulatorFrame extends AbstractDSWorkbenchFrame {
    private static Logger logger = LogManager.getLogger("DSWorkbenchSimulatorFrame");
    Translator tans = TranslationManager.getTranslator("dssim.ui.DSWorkbenchSimulatorFrame");

    private static DSWorkbenchSimulatorFrame SINGLETON = null;
    private AbstractSimulator sim = null;
    private SimulatorResult lastResult = null;
    private Point mCoordinates = null;
    private AStarResultReceiver mReceiver = null;

    public static synchronized DSWorkbenchSimulatorFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSimulatorFrame();
        }
        return SINGLETON;
    }

    /**
     * Creates new form DSWorkbenchSimulatorFrame
     */
    DSWorkbenchSimulatorFrame() {
        initComponents();
        
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        if (!Constants.DEBUG) {
            GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.astar", GlobalOptions.getHelpBroker().getHelpSet());
        }
        // </editor-fold>
    }

    public void insertValuesExternally(Map<String, Double> pValues) {
        insertValuesExternally(null, pValues, null);
    }

    public void insertValuesExternally(Point pCoordinates, Map<String, Double> pValues, AStarResultReceiver pReceiver) {
        if (pCoordinates != null) {
            mCoordinates = new Point(pCoordinates);
        } else {
            mCoordinates = null;
        }
        mReceiver = pReceiver;
        if (mReceiver != null && mCoordinates != null) {
            jTransferButton.setEnabled(true);
        }
        //add units
        for (int i = 0; i < jAttackerTable.getRowCount(); i++) {
            String unit = (String) jAttackerTable.getValueAt(i, 1);
            Double amount = pValues.get("att_" + unit);
            if (amount != null) {
                jAttackerTable.setValueAt((int) Math.round(amount), i, 2);
            }
            amount = pValues.get("def_" + unit);
            if (amount != null) {
                jAttackerTable.setValueAt((int) Math.round(amount), i, 4);
            }
        }
        Double amount = pValues.get("building");
        if (amount != null) {
            jCataTargetSpinner.setValue((int) Math.round(amount));
        }
        amount = pValues.get("wall");
        if (amount != null) {
            jWallSpinner.setValue((int) Math.round(amount));
        }
        amount = pValues.get("moral");
        if (amount != null) {
            jMoralSpinner.setValue((int) Math.round(amount));
        }
        amount = pValues.get("luck");
        if (amount != null) {
            jLuckSpinner.setValue(amount);
        }
    }

    public void insertMultipleUnits(TroopAmountFixed pUnits) {
        int col = jAttackerTable.getSelectedColumn();
        for (int row = 0; row < jAttackerTable.getRowCount(); row++) {
            UnitHolder unit = (UnitHolder) jAttackerTable.getValueAt(row, 1);
            jAttackerTable.setValueAt(pUnits.getAmountForUnit(unit), row, col);
        }
    }

    public void insertAttackers(TroopAmountFixed pUnits) {
        SimulatorTableModel.getSingleton().setOff(pUnits, null);
    }

    public void insertDefenders(TroopAmountFixed pUnits) {
        SimulatorTableModel.getSingleton().setDef(pUnits, null);
    }

    private void buildTables() {
        //build attacker table
        SimulatorTableModel attackerModel = SimulatorTableModel.getSingleton();
        jAttackerTable.setModel(attackerModel);
        jAttackerTable.setRowHeight(20);
        jAttackerTable.setDefaultEditor(Double.class, new TechLevelCellEditor((ServerSettings.getSingleton().getTechType() == ServerSettings.TECH_3) ? 3 : 10));
        jAttackerTable.setDefaultEditor(Integer.class, new SpreadSheetCellEditor());
        jAttackerTable.setDefaultRenderer(UnitHolder.class, new UnitCellRenderer());
        jAttackerTable.setDefaultRenderer(Object.class, new MultiFunctionCellRenderer());
        jAttackerTable.setDefaultEditor(Object.class, new MultiCellEditor());
        if (ServerSettings.getSingleton().getTechType() != ServerSettings.SIMPLE_TECH) {
            //old model (empty, unit, attacker, tech,empty, defender, tech, empty)
            jAttackerTable.getColumnModel().getColumn(1).setMaxWidth(60);
            jAttackerTable.getColumnModel().getColumn(2).setMaxWidth(80);
            jAttackerTable.getColumnModel().getColumn(3).setMaxWidth(40);
            jAttackerTable.getColumnModel().getColumn(4).setMaxWidth(10);
            jAttackerTable.getColumnModel().getColumn(5).setMaxWidth(80);
            jAttackerTable.getColumnModel().getColumn(6).setMaxWidth(40);
            jAttackerTable.invalidate();
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                attackerModel.addRow(new Object[]{null, unit, 0, 1.0, null, 0, 1.0, null});
            }
            jAttackerTable.revalidate();
        } else {
            //new model (empty, unit, attacker, empty, defender, empty)
            jAttackerTable.getColumnModel().getColumn(1).setMaxWidth(50);
            jAttackerTable.getColumnModel().getColumn(2).setMaxWidth(70);
            jAttackerTable.getColumnModel().getColumn(3).setMaxWidth(5);
            jAttackerTable.getColumnModel().getColumn(4).setMaxWidth(70);
            jAttackerTable.invalidate();
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                attackerModel.addRow(new Object[]{null, unit, 0, null, 0, null});
            }
        }
        updatePop();
        jAttackerTable.revalidate();

        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jAttackerTable.setBackground(Constants.DS_BACK_LIGHT);

        //add header renderers
        for (int i = 0; i < jAttackerTable.getColumnCount(); i++) {
            jAttackerTable.getColumnModel().getColumn(i).setHeaderRenderer(new TableHeaderRenderer());
        }
        
        //set result model and re-layout
        jResultTable.setModel(ResultTableModel.getSingleton());
        jResultTable.setCellSelectionEnabled(false);
        jResultTable.setRowSelectionAllowed(true);
        jResultTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateResultSelection();
            }
        });

        Dimension dim = new Dimension(jScrollPane1.getWidth(), DataHolder.getSingleton().getUnits().size() * 20 + 20 + 1);
        jTroopsPanel.setPreferredSize(dim);
        //jTroopsPanel.setMinimumSize(dim);
        jTroopsPanel.setSize(dim);
        jTroopsPanel.doLayout();
        pack();
    }

    public void updatePop() {
        int cnt = SimulatorTableModel.getSingleton().getOffTroops().getTroopPopCount();
        jOffPop.setText(Integer.toString(cnt));
        
        cnt = SimulatorTableModel.getSingleton().getDefTroops().getTroopPopCount();

        if (ServerSettings.getSingleton().getFarmLimit() != 0) {
            int max = (Integer) jFarmLevelSpinner.getValue() * ServerSettings.getSingleton().getFarmLimit();
            if (cnt > max) {
                jDefPop.setForeground(Color.RED);
                double perc = ((double) max / (double) cnt) * 100;
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumFractionDigits(0);
                nf.setMaximumFractionDigits(2);
                String pop = Integer.toString(cnt) + " (" + nf.format(perc) + "%)";
                jDefPop.setText(pop);
            } else {
                jDefPop.setForeground(new Color(34, 139, 34));
                jDefPop.setText(Integer.toString(cnt));
            }
        } else {
            jDefPop.setForeground(Color.BLACK);
            jDefPop.setText(Integer.toString(cnt));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jMenuPanel = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jAlwaysOnTopButton = new javax.swing.JToggleButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jTransferButton = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jTroopsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jAttackerTable = new javax.swing.JTable();
        jMiscInfoPanel = new javax.swing.JPanel();
        jAttackerBelieve = new javax.swing.JCheckBox();
        jDefenderBelieve = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jOffPop = new javax.swing.JTextField();
        jDefPop = new javax.swing.JTextField();
        jAttackSetupPanel = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jWallSpinner = new javax.swing.JSpinner();
        jCataWallPanel = new javax.swing.JPanel();
        jAimWall = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jCataTargetSpinner = new javax.swing.JSpinner();
        jCataChurchPanel = new javax.swing.JPanel();
        jAimChurch = new javax.swing.JCheckBox();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jFarmLabel = new javax.swing.JLabel();
        jFarmLevelSpinner = new javax.swing.JSpinner();
        jLabel29 = new javax.swing.JLabel();
        jMoralSpinner = new javax.swing.JSpinner();
        jLabel30 = new javax.swing.JLabel();
        jLuckSpinner = new javax.swing.JSpinner();
        jNightBonus = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jResultPanel = new javax.swing.JPanel();
        jResultTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jResultTable = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jAttackerWood = new javax.swing.JTextField();
        jAttackerMud = new javax.swing.JTextField();
        jAttackerIron = new javax.swing.JTextField();
        jAttackerPop = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jAttackerBash = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jDefenderWood = new javax.swing.JTextField();
        jDefenderMud = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jDefenderIron = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jDefenderPop = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jDefenderBash = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jWallInfo = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jCataInfo = new javax.swing.JTextField();
        jLabel34 = new javax.swing.JLabel();
        jNukeInfo = new javax.swing.JTextField();

        setTitle("A*Star");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jMenuPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jMenuPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        jMenuPanel.setOpaque(false);
        jMenuPanel.setPreferredSize(new java.awt.Dimension(100, 611));
        jMenuPanel.setLayout(new java.awt.GridBagLayout());

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bomb.png"))); // NOI18N
        jButton2.setToolTipText("Berechnen, nach wievielen Angriffen alle Verteidiger besiegt sind");
        jButton2.setMaximumSize(new java.awt.Dimension(50, 33));
        jButton2.setMinimumSize(new java.awt.Dimension(50, 33));
        jButton2.setPreferredSize(new java.awt.Dimension(50, 33));
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireBombDefEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jButton2, gridBagConstraints);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/refresh.png"))); // NOI18N
        jButton3.setToolTipText("Die überlebenden Verteidiger einfügen und erneut angreifen");
        jButton3.setMaximumSize(new java.awt.Dimension(50, 33));
        jButton3.setMinimumSize(new java.awt.Dimension(50, 33));
        jButton3.setPreferredSize(new java.awt.Dimension(50, 33));
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireAttackAgainEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jButton3, gridBagConstraints);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/exit.png"))); // NOI18N
        jButton4.setToolTipText("Beenden");
        jButton4.setMaximumSize(new java.awt.Dimension(50, 33));
        jButton4.setMinimumSize(new java.awt.Dimension(50, 33));
        jButton4.setPreferredSize(new java.awt.Dimension(50, 33));
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireExitEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jButton4, gridBagConstraints);

        jAlwaysOnTopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/pin_grey.png"))); // NOI18N
        jAlwaysOnTopButton.setToolTipText("A*Star immer im Vordergrund halten");
        jAlwaysOnTopButton.setMaximumSize(new java.awt.Dimension(50, 33));
        jAlwaysOnTopButton.setMinimumSize(new java.awt.Dimension(50, 33));
        jAlwaysOnTopButton.setPreferredSize(new java.awt.Dimension(50, 33));
        jAlwaysOnTopButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/pin_blue.png"))); // NOI18N
        jAlwaysOnTopButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAlwaysOnTopChangeEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jAlwaysOnTopButton, gridBagConstraints);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/attack_axe.png"))); // NOI18N
        jButton6.setToolTipText("Simulation mit den eingestellten Truppen durchführen");
        jButton6.setMaximumSize(new java.awt.Dimension(50, 33));
        jButton6.setMinimumSize(new java.awt.Dimension(50, 33));
        jButton6.setPreferredSize(new java.awt.Dimension(50, 33));
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireDoSimulationEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jButton6, gridBagConstraints);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/att_remove.png"))); // NOI18N
        jButton7.setToolTipText("Inhalt der Ergebnistabelle löschen");
        jButton7.setMaximumSize(new java.awt.Dimension(50, 33));
        jButton7.setMinimumSize(new java.awt.Dimension(50, 33));
        jButton7.setPreferredSize(new java.awt.Dimension(50, 33));
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireRemoveResults(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jButton7, gridBagConstraints);

        jTransferButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/next.png"))); // NOI18N
        jTransferButton.setToolTipText("<html>Ergebnisse in die DS Workbench Angriffsplanung &uuml;bertragen<br/>Dieser Button ist nur verf&uuml;gbar, wenn A*Star über die Bericht&uuml;bersicht aufgerufen wurde.</html>");
        jTransferButton.setEnabled(false);
        jTransferButton.setMaximumSize(new java.awt.Dimension(50, 33));
        jTransferButton.setMinimumSize(new java.awt.Dimension(50, 33));
        jTransferButton.setPreferredSize(new java.awt.Dimension(50, 33));
        jTransferButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireTransferToExternalAppEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jTransferButton, gridBagConstraints);

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/add_attack.png"))); // NOI18N
        jButton8.setToolTipText("Öffnet einen Dialog, über den Truppen in verschiedenen Formaten gelesen und eingefügt werden können");
        jButton8.setMaximumSize(new java.awt.Dimension(50, 33));
        jButton8.setMinimumSize(new java.awt.Dimension(50, 33));
        jButton8.setPreferredSize(new java.awt.Dimension(50, 33));
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireShowParseDialogEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jButton8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jMenuPanel, gridBagConstraints);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jTroopsPanel.setMaximumSize(new java.awt.Dimension(2147483647, 280));
        jTroopsPanel.setPreferredSize(new java.awt.Dimension(516, 200));
        jTroopsPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBackground(new java.awt.Color(225, 213, 190));
        jScrollPane1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(225, 213, 190), 1, true));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setMaximumSize(new java.awt.Dimension(800, 280));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(160, 120));
        jScrollPane1.setOpaque(false);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(160, 120));

        jAttackerTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jAttackerTable.setGridColor(new java.awt.Color(225, 213, 190));
        jAttackerTable.setOpaque(false);
        jAttackerTable.setPreferredSize(new java.awt.Dimension(500, 280));
        jScrollPane1.setViewportView(jAttackerTable);

        jTroopsPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jTroopsPanel, gridBagConstraints);

        jMiscInfoPanel.setOpaque(false);
        jMiscInfoPanel.setLayout(new java.awt.GridBagLayout());

        jAttackerBelieve.setSelected(true);
        jAttackerBelieve.setText("Gläubig");
        jAttackerBelieve.setToolTipText("Angreifer ist gläubig");
        jAttackerBelieve.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireBelieveChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMiscInfoPanel.add(jAttackerBelieve, gridBagConstraints);

        jDefenderBelieve.setSelected(true);
        jDefenderBelieve.setText("Gläubig");
        jDefenderBelieve.setToolTipText("Verteidiger ist gläubig");
        jDefenderBelieve.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireBelieveChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMiscInfoPanel.add(jDefenderBelieve, gridBagConstraints);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/pop.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jMiscInfoPanel.add(jLabel4, gridBagConstraints);

        jLabel35.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/pop.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jMiscInfoPanel.add(jLabel35, gridBagConstraints);

        jOffPop.setEditable(false);
        jOffPop.setText("0");
        jOffPop.setToolTipText("Benötigte Bauernhofplätze");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jMiscInfoPanel.add(jOffPop, gridBagConstraints);

        jDefPop.setEditable(false);
        jDefPop.setText("0");
        jDefPop.setToolTipText("Benötigte Bauernhofplätze");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jMiscInfoPanel.add(jDefPop, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jMiscInfoPanel, gridBagConstraints);

        jAttackSetupPanel.setOpaque(false);
        jAttackSetupPanel.setLayout(new java.awt.GridBagLayout());

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/wall.png"))); // NOI18N
        jLabel28.setToolTipText("Wallstufe");
        jLabel28.setMaximumSize(new java.awt.Dimension(16, 25));
        jLabel28.setMinimumSize(new java.awt.Dimension(16, 25));
        jLabel28.setPreferredSize(new java.awt.Dimension(16, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAttackSetupPanel.add(jLabel28, gridBagConstraints);

        jWallSpinner.setModel(new javax.swing.SpinnerNumberModel(20, 0, 20, 1));
        jWallSpinner.setToolTipText("Wallstufe");
        jWallSpinner.setMaximumSize(new java.awt.Dimension(60, 25));
        jWallSpinner.setMinimumSize(new java.awt.Dimension(60, 25));
        jWallSpinner.setPreferredSize(new java.awt.Dimension(60, 25));
        jWallSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAttackSetupPanel.add(jWallSpinner, gridBagConstraints);

        jCataWallPanel.setOpaque(false);
        jCataWallPanel.setLayout(new java.awt.GridLayout(1, 0));

        jAimWall.setToolTipText("Katapulte auf den Wall ausrichten");
        jAimWall.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jAimWall.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jAimWall.setIconTextGap(2);
        jAimWall.setMargin(new java.awt.Insets(2, 0, 2, 2));
        jAimWall.setMaximumSize(new java.awt.Dimension(18, 18));
        jAimWall.setMinimumSize(new java.awt.Dimension(18, 18));
        jAimWall.setPreferredSize(new java.awt.Dimension(18, 18));
        jAimWall.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAimAtWallEvent(evt);
            }
        });
        jCataWallPanel.add(jAimWall);

        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/cata.png"))); // NOI18N
        jLabel23.setToolTipText("Katapulte auf den Wall ausrichten");
        jCataWallPanel.add(jLabel23);

        jLabel24.setText(">>");
        jLabel24.setToolTipText("Katapulte auf den Wall ausrichten");
        jCataWallPanel.add(jLabel24);

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/wall.png"))); // NOI18N
        jLabel25.setToolTipText("Katapulte auf den Wall ausrichten");
        jCataWallPanel.add(jLabel25);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jAttackSetupPanel.add(jCataWallPanel, gridBagConstraints);

        jLabel31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/main.png"))); // NOI18N
        jLabel31.setToolTipText("Gebäudestufe Katapultziel");
        jLabel31.setMaximumSize(new java.awt.Dimension(16, 25));
        jLabel31.setMinimumSize(new java.awt.Dimension(16, 25));
        jLabel31.setPreferredSize(new java.awt.Dimension(16, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAttackSetupPanel.add(jLabel31, gridBagConstraints);

        jCataTargetSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 30, 1));
        jCataTargetSpinner.setToolTipText("Gebäudestufe Katapultziel");
        jCataTargetSpinner.setMaximumSize(new java.awt.Dimension(60, 25));
        jCataTargetSpinner.setMinimumSize(new java.awt.Dimension(60, 25));
        jCataTargetSpinner.setPreferredSize(new java.awt.Dimension(60, 25));
        jCataTargetSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAttackSetupPanel.add(jCataTargetSpinner, gridBagConstraints);

        jCataChurchPanel.setOpaque(false);
        jCataChurchPanel.setLayout(new java.awt.GridLayout(1, 0));

        jAimChurch.setToolTipText("Katapulte auf die Kirche ausrichten");
        jAimChurch.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jAimChurch.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jAimChurch.setIconTextGap(2);
        jAimChurch.setMargin(new java.awt.Insets(2, 0, 2, 2));
        jAimChurch.setMaximumSize(new java.awt.Dimension(18, 18));
        jAimChurch.setMinimumSize(new java.awt.Dimension(18, 18));
        jAimChurch.setPreferredSize(new java.awt.Dimension(18, 18));
        jAimChurch.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireAimChurchStateChangedEvent(evt);
            }
        });
        jCataChurchPanel.add(jAimChurch);

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/cata.png"))); // NOI18N
        jLabel26.setToolTipText("Katapulte auf die Kirche ausrichten");
        jCataChurchPanel.add(jLabel26);

        jLabel27.setText(">>");
        jLabel27.setToolTipText("Katapulte auf die Kirche ausrichten");
        jCataChurchPanel.add(jLabel27);

        jLabel37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/church.png"))); // NOI18N
        jLabel37.setToolTipText("Katapulte auf die Kirche ausrichten");
        jCataChurchPanel.add(jLabel37);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        jAttackSetupPanel.add(jCataChurchPanel, gridBagConstraints);

        jFarmLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/farm.png"))); // NOI18N
        jFarmLabel.setToolTipText("Gebäudestufe des Bauernhofs");
        jFarmLabel.setMaximumSize(new java.awt.Dimension(16, 25));
        jFarmLabel.setMinimumSize(new java.awt.Dimension(16, 25));
        jFarmLabel.setPreferredSize(new java.awt.Dimension(16, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jAttackSetupPanel.add(jFarmLabel, gridBagConstraints);

        jFarmLevelSpinner.setModel(new javax.swing.SpinnerNumberModel(30, 1, 30, 1));
        jFarmLevelSpinner.setToolTipText("<html>Geb&auml;udestufe des Bauernhofs<br/>\nDieser Button ist nur auf Servern mit der Bauernhofregel aktiv</html>");
        jFarmLevelSpinner.setMaximumSize(new java.awt.Dimension(60, 25));
        jFarmLevelSpinner.setMinimumSize(new java.awt.Dimension(60, 25));
        jFarmLevelSpinner.setPreferredSize(new java.awt.Dimension(60, 25));
        jFarmLevelSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
        jAttackSetupPanel.add(jFarmLevelSpinner, gridBagConstraints);

        jLabel29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/masks.png"))); // NOI18N
        jLabel29.setToolTipText("Moral (Angreifer)");
        jLabel29.setMaximumSize(new java.awt.Dimension(16, 25));
        jLabel29.setMinimumSize(new java.awt.Dimension(16, 25));
        jLabel29.setPreferredSize(new java.awt.Dimension(16, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAttackSetupPanel.add(jLabel29, gridBagConstraints);

        jMoralSpinner.setModel(new javax.swing.SpinnerNumberModel(100, 30, 100, 1));
        jMoralSpinner.setToolTipText("Moral (Angreifer)");
        jMoralSpinner.setMaximumSize(new java.awt.Dimension(60, 25));
        jMoralSpinner.setMinimumSize(new java.awt.Dimension(60, 25));
        jMoralSpinner.setPreferredSize(new java.awt.Dimension(60, 25));
        jMoralSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAttackSetupPanel.add(jMoralSpinner, gridBagConstraints);

        jLabel30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/klee.png"))); // NOI18N
        jLabel30.setToolTipText("Glück (Angreifer)");
        jLabel30.setMaximumSize(new java.awt.Dimension(16, 25));
        jLabel30.setMinimumSize(new java.awt.Dimension(16, 25));
        jLabel30.setPreferredSize(new java.awt.Dimension(16, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAttackSetupPanel.add(jLabel30, gridBagConstraints);

        jLuckSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, -25.0d, 25.0d, 0.1d));
        jLuckSpinner.setToolTipText("Glück (Angreifer)");
        jLuckSpinner.setMaximumSize(new java.awt.Dimension(60, 25));
        jLuckSpinner.setMinimumSize(new java.awt.Dimension(60, 25));
        jLuckSpinner.setPreferredSize(new java.awt.Dimension(60, 25));
        jLuckSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAttackSetupPanel.add(jLuckSpinner, gridBagConstraints);

        jNightBonus.setText("Nachtbonus");
        jNightBonus.setToolTipText("Nachtbonus aktivieren/deaktivieren");
        jNightBonus.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jNightBonus.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jNightBonus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/sun.gif"))); // NOI18N
        jNightBonus.setIconTextGap(2);
        jNightBonus.setMargin(new java.awt.Insets(2, 0, 2, 2));
        jNightBonus.setMaximumSize(new java.awt.Dimension(100, 25));
        jNightBonus.setMinimumSize(new java.awt.Dimension(100, 25));
        jNightBonus.setPreferredSize(new java.awt.Dimension(100, 25));
        jNightBonus.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/moon.png"))); // NOI18N
        jNightBonus.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireNightBonusStateChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jAttackSetupPanel.add(jNightBonus, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jAttackSetupPanel, gridBagConstraints);

        jSplitPane2.setTopComponent(jPanel2);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jResultPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Ergebnisse"));
        jResultPanel.setMinimumSize(new java.awt.Dimension(248, 100));
        jResultPanel.setOpaque(false);
        jResultPanel.setLayout(new java.awt.GridBagLayout());

        jResultTabbedPane.setPreferredSize(new java.awt.Dimension(534, 300));

        jScrollPane3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(225, 213, 190), 1, true));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(452, 300));

        jResultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jResultTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        jResultTable.setShowHorizontalLines(false);
        jResultTable.setShowVerticalLines(false);
        jScrollPane3.setViewportView(jResultTable);

        jResultTabbedPane.addTab("Angriffe", jScrollPane3);

        jPanel5.setPreferredSize(new java.awt.Dimension(529, 100));
        jPanel5.setLayout(new java.awt.GridBagLayout());

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Angreifer"));
        jPanel6.setPreferredSize(new java.awt.Dimension(254, 175));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/holz.png"))); // NOI18N
        jLabel3.setToolTipText("Holz (Verluste)");

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/lehm.png"))); // NOI18N
        jLabel14.setToolTipText("Lehm (Verluste)");

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/eisen.png"))); // NOI18N
        jLabel15.setToolTipText("Eisen (Verluste)");

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/pop.png"))); // NOI18N
        jLabel16.setToolTipText("Bevölkerung (Verluste)");

        jAttackerWood.setEditable(false);
        jAttackerWood.setToolTipText("Holz (Verluste)");

        jAttackerMud.setEditable(false);
        jAttackerMud.setToolTipText("Lehm (Verluste)");

        jAttackerIron.setEditable(false);
        jAttackerIron.setToolTipText("Eisen (Verluste)");

        jAttackerPop.setEditable(false);
        jAttackerPop.setToolTipText("Bevölkerung (Verluste)");

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skull.png"))); // NOI18N
        jLabel21.setToolTipText("Basherpunkte (Gewinn)");

        jAttackerBash.setEditable(false);
        jAttackerBash.setToolTipText("Basherpunkte (Gewinn)");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(jAttackerWood, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(18, 18, 18)
                        .addComponent(jAttackerMud, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(jLabel21))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jAttackerBash, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                            .addComponent(jAttackerPop, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                            .addComponent(jAttackerIron, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jAttackerWood)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jAttackerMud)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jAttackerIron)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jAttackerPop)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jAttackerBash)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(jPanel6, gridBagConstraints);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Verteidiger"));
        jPanel7.setPreferredSize(new java.awt.Dimension(254, 175));

        jDefenderWood.setEditable(false);
        jDefenderWood.setToolTipText("Holz (Verluste)");

        jDefenderMud.setEditable(false);
        jDefenderMud.setToolTipText("Lehm (Verluste)");

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/holz.png"))); // NOI18N
        jLabel17.setToolTipText("Holz (Verluste)");

        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/pop.png"))); // NOI18N
        jLabel20.setToolTipText("Bevölkerung (Verluste)");

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/eisen.png"))); // NOI18N
        jLabel19.setToolTipText("Eisen (Verluste)");

        jDefenderIron.setEditable(false);
        jDefenderIron.setToolTipText("Eisen (Verluste)");

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/lehm.png"))); // NOI18N
        jLabel18.setToolTipText("Lehm (Verluste)");

        jDefenderPop.setEditable(false);
        jDefenderPop.setToolTipText("Bevölkerung (Verluste)");

        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/skull.png"))); // NOI18N
        jLabel22.setToolTipText("Basherpunkte (Gewinn)");

        jDefenderBash.setEditable(false);
        jDefenderBash.setToolTipText("Basherpunkte (Gewinn)");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(18, 18, 18)
                        .addComponent(jDefenderWood, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addGap(18, 18, 18)
                        .addComponent(jDefenderMud, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20)
                            .addComponent(jLabel22))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDefenderBash, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                            .addComponent(jDefenderPop, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                            .addComponent(jDefenderIron, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jDefenderWood)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jDefenderMud)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jDefenderIron)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jDefenderPop)
                    .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jDefenderBash)
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(jPanel7, gridBagConstraints);

        jResultTabbedPane.addTab("Statistik", jPanel5);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jResultPanel.add(jResultTabbedPane, gridBagConstraints);

        jLabel32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/wall.png"))); // NOI18N
        jLabel32.setToolTipText("Wallstufe");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jResultPanel.add(jLabel32, gridBagConstraints);

        jWallInfo.setEditable(false);
        jWallInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jWallInfo.setMinimumSize(new java.awt.Dimension(200, 18));
        jWallInfo.setPreferredSize(new java.awt.Dimension(200, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jResultPanel.add(jWallInfo, gridBagConstraints);

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/main.png"))); // NOI18N
        jLabel33.setToolTipText("Gebäudestufe Katapultziel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jResultPanel.add(jLabel33, gridBagConstraints);

        jCataInfo.setEditable(false);
        jCataInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jResultPanel.add(jCataInfo, gridBagConstraints);

        jLabel34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/bomb_small.png"))); // NOI18N
        jLabel34.setToolTipText("Gebäudestufe Katapultziel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jResultPanel.add(jLabel34, gridBagConstraints);

        jNukeInfo.setEditable(false);
        jNukeInfo.setText("(Einzelangriff)");
        jNukeInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jResultPanel.add(jNukeInfo, gridBagConstraints);

        jPanel3.add(jResultPanel, java.awt.BorderLayout.CENTER);

        jSplitPane2.setBottomComponent(jPanel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSplitPane2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireStateChangedEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireStateChangedEvent
        // fireCalculateEvent();
}//GEN-LAST:event_fireStateChangedEvent

    private void fireNightBonusStateChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireNightBonusStateChangedEvent
        // fireCalculateEvent();
    }//GEN-LAST:event_fireNightBonusStateChangedEvent

    private void fireBombDefEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireBombDefEvent
        TroopAmountFixed offTroop = SimulatorTableModel.getSingleton().getOffTroops();
        TroopAmountFixed defTroop = SimulatorTableModel.getSingleton().getDefTroops();
        TechState offTech = SimulatorTableModel.getSingleton().getOffTech();
        TechState defTech = SimulatorTableModel.getSingleton().getDefTech();
        boolean nightBonus = jNightBonus.isSelected();
        int wallLevel = (Integer) jWallSpinner.getValue();
        int cataTarget = (Integer) jCataTargetSpinner.getValue();
        boolean cataChurch = false;
        boolean cataWall = jAimWall.isSelected();
        if (ServerSettings.getSingleton().isChurch()) {
            cataChurch = jAimChurch.isSelected();
        }
        double luck = (Double) jLuckSpinner.getValue();
        double moral = (Integer) jMoralSpinner.getValue();
        int farmLevel = 0;
        if (cataWall) {
            cataTarget = wallLevel;
            jCataTargetSpinner.setValue(cataTarget);
        }
        if (jFarmLevelSpinner.isEnabled()) {
            farmLevel = (Integer) jFarmLevelSpinner.getValue();
        }
        boolean attackerBelieve = true;
        boolean defenderBelieve = true;
        if (jAttackerBelieve.isEnabled() && jDefenderBelieve.isEnabled()) {
            attackerBelieve = jAttackerBelieve.isSelected();
            defenderBelieve = jDefenderBelieve.isSelected();
        }
        
        SimulatorResult result = sim.bunkerBuster(offTroop, defTroop, offTech, defTech, nightBonus, luck, moral, wallLevel, cataTarget, farmLevel, attackerBelieve, defenderBelieve, cataChurch, cataWall);
        int nukes = result.getNukes();
        if (nukes == Integer.MAX_VALUE) {
            jNukeInfo.setText("Dorf clean nach mehr als 1000 Angriffen (Abbruch)");
        } else {
            jNukeInfo.setText("Dorf clean nach " + ((nukes == 1) ? " 1 Angriff" : " " + nukes + " Angriffen"));
        }

        buildResultTable(result);
    }//GEN-LAST:event_fireBombDefEvent

    private void fireExitEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireExitEvent
        dispose();
    }//GEN-LAST:event_fireExitEvent

    private void fireAttackAgainEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireAttackAgainEvent
        try {
            SimulatorTableModel.getSingleton().setDef(lastResult.getSurvivingDef(), null);
            jWallSpinner.setValue(lastResult.getWallLevel());
            jCataTargetSpinner.setValue(lastResult.getBuildingLevel());
            fireCalculateEvent();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_fireAttackAgainEvent

    private void fireBelieveChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireBelieveChangedEvent
        //fireCalculateEvent();
    }//GEN-LAST:event_fireBelieveChangedEvent

    private void fireAlwaysOnTopChangeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangeEvent
        setAlwaysOnTop(jAlwaysOnTopButton.isSelected());
    }//GEN-LAST:event_fireAlwaysOnTopChangeEvent

    private void fireAimChurchStateChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAimChurchStateChangedEvent
        if (jAimChurch.isSelected()) {
            jAimWall.setSelected(false);
        }
    }//GEN-LAST:event_fireAimChurchStateChangedEvent

    private void fireDoSimulationEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireDoSimulationEvent
        fireCalculateEvent();
    }//GEN-LAST:event_fireDoSimulationEvent

    private void fireRemoveResults(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireRemoveResults
        ResultTableModel.getSingleton().clear();
    }//GEN-LAST:event_fireRemoveResults

    private void fireAimAtWallEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAimAtWallEvent
        if (jAimWall.isSelected()) {
            jAimChurch.setSelected(false);
        }
    }//GEN-LAST:event_fireAimAtWallEvent

    private void fireTransferToExternalAppEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireTransferToExternalAppEvent
        if (mReceiver != null && mCoordinates != null) {
            mReceiver.fireNotifyOnResultEvent(mCoordinates, lastResult.getNukes());
        }
    }//GEN-LAST:event_fireTransferToExternalAppEvent

    private void fireShowParseDialogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowParseDialogEvent
        UnitParserFrame upf = new UnitParserFrame();
        upf.pack();
        upf.setVisible(true);
    }//GEN-LAST:event_fireShowParseDialogEvent
    
    public void setApplicationFont(Font font) {
        Enumeration enumer = UIManager.getDefaults().keys();
        while (enumer.hasMoreElements()) {
            Object key = enumer.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, new javax.swing.plaf.FontUIResource(font));
            }
        }
    }
    
    @Override
    public void setVisible(boolean tVisibility) {
        //as far as I know these cannot be used any more
        if(tVisibility && ServerSettings.getSingleton().getKnightType()== ServerSettings.KNIGHT_WITH_ITEMS) {
            JOptionPaneHelper.showErrorBox(null, tans.get("ItemKnightDesc"), tans.get("ItemKnightTitle"));
        }
        super.setVisible(tVisibility);
    }

    private void fireCalculateEvent() {
        TroopAmountFixed offTroop = SimulatorTableModel.getSingleton().getOffTroops();
        TroopAmountFixed defTroop = SimulatorTableModel.getSingleton().getDefTroops();
        TechState offTech = SimulatorTableModel.getSingleton().getOffTech();
        TechState defTech = SimulatorTableModel.getSingleton().getDefTech();
        boolean nightBonus = jNightBonus.isSelected();
        boolean cataChurch = false;
        if (ServerSettings.getSingleton().isChurch()) {
            cataChurch = jAimChurch.isSelected();
        }
        boolean cataWall = jAimWall.isSelected();
        int wallLevel = (Integer) jWallSpinner.getValue();
        int cataTarget = (Integer) jCataTargetSpinner.getValue();
        int farmLevel = 0;
        if (jFarmLevelSpinner.isEnabled()) {
            farmLevel = (Integer) jFarmLevelSpinner.getValue();
        }
        if (cataWall) {
            cataTarget = wallLevel;
            jCataTargetSpinner.setValue(cataTarget);
        }
        double luck = (Double) jLuckSpinner.getValue();
        double moral = (Integer) jMoralSpinner.getValue();
        boolean attackerBelieve = true;
        boolean defenderBelieve = true;
        if (jAttackerBelieve.isEnabled() && jDefenderBelieve.isEnabled()) {
            attackerBelieve = jAttackerBelieve.isSelected();
            defenderBelieve = jDefenderBelieve.isSelected();
        }

        SimulatorResult result = sim.calculate(offTroop, defTroop, offTech, defTech, nightBonus, luck, moral, wallLevel, cataTarget, farmLevel, attackerBelieve, defenderBelieve, cataChurch, cataWall);
        jNukeInfo.setText("(Einzelangriff)");
        buildResultTable(result);
    }

    private void buildResultTable(SimulatorResult pResult) {

        // <editor-fold defaultstate="collapsed" desc="Build header renderer">
        for (int i = 0; i < jResultTable.getColumnCount(); i++) {
            jResultTable.getColumn(jResultTable.getColumnName(i)).setHeaderRenderer(new UnitTableHeaderRenderer());
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Build result table rows">
        addResult(pResult);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Winner/Loser color renderer">
        DefaultTableCellRenderer winLossRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                JLabel l;
                try {
                    l = (JLabel) c;
                } catch(ClassCastException e) {
                    logger.debug("Exeption happend: ", e);
                    return c;
                }
                
                if (!isSelected) {
                    l.setBackground(Constants.DS_BACK);
                } else {
                    l.setBackground(Constants.DS_BACK.darker());
                }
                int dataSet = ResultTableModel.getSingleton().getDataSetNumberForRow(row);
                boolean won = ResultTableModel.getSingleton().getResult(dataSet).isWin();
                if (table.getValueAt(row, 0).equals("")) {
                    if (!isSelected) {
                        l.setBackground(Constants.DS_BACK_LIGHT);
                    } else {
                        l.setBackground(Constants.DS_BACK_LIGHT.darker());
                    }
                } else {
                    String v = (String) table.getValueAt(row, 0);
                    if (v.startsWith("Ergebnis")) {
                        l.setBorder(BorderFactory.createEmptyBorder());
                    } else {
                        l.setBorder(BorderFactory.createLineBorder(Constants.DS_BACK, 1));
                    }
                }

                l.setHorizontalAlignment(SwingConstants.CENTER);
                if(value instanceof Integer) {
                    l.setText(Integer.toString((Integer) value));
                    l.setIcon(null);
                }
                else if(value instanceof String) {
                    String data = (String) value;
                    if (data.startsWith("Ergebnis")) {
                        l.setText("<html><b>" + data + "</b></html>");
                        l.setIcon(null);
                    } else if (data.equals("Wall")) {
                        l.setText("");
                        l.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/wall.png")));
                    } else if (data.equals("Gebäude")) {
                        l.setText("");
                        l.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/main.png")));
                    } else {
                        l.setText(data);
                        l.setIcon(null);
                    }
                }

                Color bg;
                if (ResultTableModel.getSingleton().isAttackerRow(row)) {
                    if (won) {
                        bg = Constants.WINNER_GREEN;
                    } else {
                        bg = Constants.LOSER_RED;
                    }
                } else if (ResultTableModel.getSingleton().isDefenderRow(row)) {
                    if (won) {
                        bg = Constants.LOSER_RED;
                    } else {
                        bg = Constants.WINNER_GREEN;
                    }
                } else {
                    bg = null;
                }

                if(bg != null)
                    if(!isSelected)
                        l.setBackground(bg);
                    else
                        l.setBackground(bg.darker());

                return l;
            }
        };
        // </editor-fold>

        jScrollPane3.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        jResultTable.setDefaultRenderer(Integer.class, winLossRenderer);
        jResultTable.setDefaultRenderer(String.class, winLossRenderer);
        jResultTable.getColumnModel().getColumn(0).setMinWidth(100);
        jResultTable.getColumnModel().getColumn(0).setResizable(false);

        int wall = (Integer) jWallSpinner.getValue();

        if (wall != pResult.getWallLevel()) {
            jWallInfo.setText("Wall zerstört von Stufe " + wall + " auf Stufe " + pResult.getWallLevel());
        } else if (wall == 0) {
            jWallInfo.setText("Wall nicht vorhanden");
        } else {
            jWallInfo.setText("Wall nicht beschädigt");
        }
        int building = (Integer) jCataTargetSpinner.getValue();
        if (jAimWall.isSelected()) {
            building = pResult.getWallLevel();
        }
        if (building != pResult.getBuildingLevel()) {
            jCataInfo.setText("Gebäude zerstört von Stufe " + building + " auf Stufe " + pResult.getBuildingLevel());
        } else if (building == 0) {
            jCataInfo.setText("Gebäude nicht vorhanden");
        } else {
            jCataInfo.setText("Gebäude nicht beschädigt");
        }
        lastResult = pResult;
        repaint();
    }

    private void addResult(SimulatorResult pResult) {
        if (sim.getOff() == null || sim.getDef() == null) {
            return;
        }
        pResult.setOffBefore(sim.getOff().clone());
        pResult.setDefBefore(sim.getDef().clone());
        pResult.setWallBefore((Integer) jWallSpinner.getValue());
        pResult.setCataAtWall(jAimWall.isSelected());
        
        TroopAmountFixed attLoss = sim.getOff().clone();
        TroopAmountFixed defLoss = sim.getDef().clone();
        attLoss.removeAmount(pResult.getSurvivingOff());
        defLoss.removeAmount(pResult.getSurvivingDef());

        int attWood = attLoss.getTroopWoodCost();
        int attMud = attLoss.getTroopStoneCost();
        int attIron = attLoss.getTroopIronCost();
        int attPop = attLoss.getTroopPopCount();
        int attBash = defLoss.getTroopAttackerBash();
        int defWood = defLoss.getTroopWoodCost();
        int defMud = defLoss.getTroopStoneCost();
        int defIron = defLoss.getTroopIronCost();
        int defPop = defLoss.getTroopPopCount();
        int defBash = attLoss.getTroopDefenderBash();

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        jAttackerWood.setText(nf.format(attWood));
        jAttackerMud.setText(nf.format(attMud));
        jAttackerIron.setText(nf.format(attIron));
        jAttackerPop.setText(nf.format(attPop));
        jAttackerBash.setText(nf.format(attBash));
        jDefenderWood.setText(nf.format(defWood));
        jDefenderMud.setText(nf.format(defMud));
        jDefenderIron.setText(nf.format(defIron));
        jDefenderPop.setText(nf.format(defPop));
        jDefenderBash.setText(nf.format(defBash));

        jResultTable.invalidate();
        ResultTableModel.getSingleton().addResult(pResult);
        jResultTable.revalidate();
    }

    private void updateResultSelection() {
        Integer[] datasets = getSelectedDataSets();
        
        int attWood = 0;
        int attMud = 0;
        int attIron = 0;
        int attPop = 0;
        int attBash = 0;
        int defWood = 0;
        int defMud = 0;
        int defIron = 0;
        int defPop = 0;
        int defBash = 0;
        for (Integer dataSetId : datasets) {
            SimulatorResult result = ResultTableModel.getSingleton().getResult(dataSetId);
            TroopAmountFixed attLoss = result.getOffBefore().clone();
            TroopAmountFixed defLoss = result.getDefBefore().clone();
            attLoss.removeAmount(result.getSurvivingOff());
            defLoss.removeAmount(result.getSurvivingDef());
            
            attWood += attLoss.getTroopWoodCost();
            attMud += attLoss.getTroopStoneCost();
            attIron += attLoss.getTroopIronCost();
            attPop += attLoss.getTroopPopCount();
            attBash += defLoss.getTroopAttackerBash();
            defWood += defLoss.getTroopWoodCost();
            defMud += defLoss.getTroopStoneCost();
            defIron += defLoss.getTroopIronCost();
            defPop += defLoss.getTroopPopCount();
            defBash += attLoss.getTroopDefenderBash();
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        jAttackerWood.setText(nf.format(attWood));
        jAttackerMud.setText(nf.format(attMud));
        jAttackerIron.setText(nf.format(attIron));
        jAttackerPop.setText(nf.format(attPop));
        jAttackerBash.setText(nf.format(attBash));
        jDefenderWood.setText(nf.format(defWood));
        jDefenderMud.setText(nf.format(defMud));
        jDefenderIron.setText(nf.format(defIron));
        jDefenderPop.setText(nf.format(defPop));
        jDefenderBash.setText(nf.format(defBash));
    }

    private Integer[] getSelectedDataSets() {

        int[] rows = jResultTable.getSelectedRows();
        List<Integer> selection = new LinkedList<>();
        for (int row : rows) {
            int ds = ResultTableModel.getSingleton().getDataSetNumberForRow(row);
            if (!selection.contains(ds)) {
                selection.add(ds);
            }
        }
        return selection.toArray(new Integer[]{});
    }

    public void fireGlobalErrorEvent(String pMessage) {
        JOptionPane.showMessageDialog(this, pMessage, "Fehler", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    public void fireGlobalWarningEvent(String pMessage) {
        JOptionPane.showMessageDialog(this, pMessage, "Warnung", JOptionPane.WARNING_MESSAGE);
    }

    public void addResultExternally(SimulatorResult pResult) {
        buildResultTable(pResult);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAimChurch;
    private javax.swing.JCheckBox jAimWall;
    private javax.swing.JToggleButton jAlwaysOnTopButton;
    private javax.swing.JPanel jAttackSetupPanel;
    private javax.swing.JTextField jAttackerBash;
    private javax.swing.JCheckBox jAttackerBelieve;
    private javax.swing.JTextField jAttackerIron;
    private javax.swing.JTextField jAttackerMud;
    private javax.swing.JTextField jAttackerPop;
    private javax.swing.JTable jAttackerTable;
    private javax.swing.JTextField jAttackerWood;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JPanel jCataChurchPanel;
    private javax.swing.JTextField jCataInfo;
    private javax.swing.JSpinner jCataTargetSpinner;
    private javax.swing.JPanel jCataWallPanel;
    private javax.swing.JTextField jDefPop;
    private javax.swing.JTextField jDefenderBash;
    private javax.swing.JCheckBox jDefenderBelieve;
    private javax.swing.JTextField jDefenderIron;
    private javax.swing.JTextField jDefenderMud;
    private javax.swing.JTextField jDefenderPop;
    private javax.swing.JTextField jDefenderWood;
    private javax.swing.JLabel jFarmLabel;
    private javax.swing.JSpinner jFarmLevelSpinner;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSpinner jLuckSpinner;
    private javax.swing.JPanel jMenuPanel;
    private javax.swing.JPanel jMiscInfoPanel;
    private javax.swing.JSpinner jMoralSpinner;
    private javax.swing.JCheckBox jNightBonus;
    private javax.swing.JTextField jNukeInfo;
    private javax.swing.JTextField jOffPop;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jResultPanel;
    private javax.swing.JTabbedPane jResultTabbedPane;
    private javax.swing.JTable jResultTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JButton jTransferButton;
    private javax.swing.JPanel jTroopsPanel;
    private javax.swing.JTextField jWallInfo;
    private javax.swing.JSpinner jWallSpinner;
    // End of variables declaration//GEN-END:variables

    @Override
    public void resetView() {
        logger.debug("setting up UI");
        SimulatorTableModel.getSingleton().reset();
        ResultTableModel.getSingleton().reset();
        SimulatorTableModel.getSingleton().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                updatePop();
            }
        });
        if (DataHolder.getSingleton().getUnitByPlainName("archer") != null) {
            sim = new NewSimulator();
            lastResult = null;
        } else {
            sim = new OldSimulator();
            lastResult = null;
        }

        jFarmLevelSpinner.setEnabled(ServerSettings.getSingleton().getFarmLimit() != 0);
        jFarmLabel.setEnabled(ServerSettings.getSingleton().getFarmLimit() != 0);
        jAttackerBelieve.setEnabled(ServerSettings.getSingleton().isChurch());
        jDefenderBelieve.setEnabled(ServerSettings.getSingleton().isChurch());
        jAimChurch.setEnabled(ServerSettings.getSingleton().isChurch());
        logger.debug("setting up tables");
        buildTables();
        buildResultTable(new SimulatorResult());
        logger.debug("finished setup");
    }

    @Override
    public void storeCustomProperties(Configuration pConfig) {
        pConfig.setProperty(getPropertyPrefix() + ".jSplitPane2.pos", jSplitPane2.getDividerLocation());
    }

    @Override
    public void restoreCustomProperties(Configuration pConfig) {
        try {
            jSplitPane2.setDividerLocation(pConfig.getDouble(getPropertyPrefix() + ".jSplitPane2.pos", jSplitPane2.getDividerLocation()));
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getPropertyPrefix() {
        return "simulator.view";
    }

    @Override
    public void fireVillagesDraggedEvent(List<Village> pVillages, Point pDropLocation) {
    }
}
