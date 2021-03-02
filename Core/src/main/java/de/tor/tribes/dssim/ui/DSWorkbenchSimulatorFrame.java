/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * DSWorkbenchSimulatorFrame.java
 *
 * Created on 19.07.2009, 16:45:19
 */
package de.tor.tribes.dssim.ui;

import de.tor.tribes.dssim.Constants;
import de.tor.tribes.dssim.algo.AbstractSimulator;
import de.tor.tribes.dssim.algo.NewSimulator;
import de.tor.tribes.dssim.algo.OldSimulator;
import de.tor.tribes.dssim.editor.MultiCellEditor;
import de.tor.tribes.dssim.editor.SpreadSheetCellEditor;
import de.tor.tribes.dssim.editor.TechLevelCellEditor;
import de.tor.tribes.dssim.io.SimIOHelper;
import de.tor.tribes.dssim.model.ResultTableModel;
import de.tor.tribes.dssim.model.SimulatorTableModel;
import de.tor.tribes.dssim.renderer.MultiFunctionCellRenderer;
import de.tor.tribes.dssim.renderer.TableHeaderRenderer;
import de.tor.tribes.dssim.renderer.UnitTableCellRenderer;
import de.tor.tribes.dssim.types.AbstractUnitElement;
import de.tor.tribes.dssim.types.EventListener;
import de.tor.tribes.dssim.types.KnightItem;
import de.tor.tribes.dssim.types.SimulatorResult;
import de.tor.tribes.dssim.types.UnitHolder;
import de.tor.tribes.dssim.util.AStarResultReceiver;
import de.tor.tribes.dssim.util.ConfigManager;
import de.tor.tribes.dssim.util.ImageManager;
import de.tor.tribes.dssim.util.UnitManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.*;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Charon
 */
public class DSWorkbenchSimulatorFrame extends javax.swing.JFrame {

    private static Logger logger = LogManager.getLogger("SimFrame");

    private static DSWorkbenchSimulatorFrame SINGLETON = null;
    private final String SERVER_PROP = "default.server";
    private AbstractSimulator sim = null;
    private SimulatorResult lastResult = null;
    private Properties mProperties = null;
    private Point mCoordinates = null;
    private AStarResultReceiver mReceiver = null;

    public static synchronized DSWorkbenchSimulatorFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchSimulatorFrame();
        }
        return SINGLETON;
    }

    public Properties getProperties() {
        return mProperties;
    }

    /**
     * Creates new form DSWorkbenchSimulatorFrame
     */
    DSWorkbenchSimulatorFrame() {
        initComponents();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    mProperties.put("width", Integer.toString(getWidth()));
                    mProperties.put("height", Integer.toString(getHeight()));
                    mProperties.put("split", Integer.toString(jSplitPane2.getDividerLocation()));

                    String dataDir = SimIOHelper.getDataDir();
                    DSWorkbenchSimulatorFrame.getSingleton().getProperties().store(new FileOutputStream(dataDir + "/astar.props"), "");
                } catch (Exception e) {
                    //failed to store properties...so what!?
                }
            }
        }));

        try {
            String dataDir = SimIOHelper.getDataDir();
            mProperties = new Properties();
            mProperties.load(new FileInputStream(dataDir + "/astar.props"));
        } catch (Exception e) {
            //failed to load properties
        }

        setTitle("A*Star - Attack Simulator for Tribal Wars v" + Constants.VERSION + Constants.VERSION_ADDITION);
        try {
            ImageManager.loadUnitIcons();
        } catch (Exception e) {
            fireGlobalErrorEvent("Einheitensymbole konnten nicht geladen werden.");
        }

        buildServerList();
        String server = mProperties.getProperty(SERVER_PROP);
        if (server == null) {
            jServerList.setSelectedIndex(0);
        } else {
            jServerList.setSelectedItem(server);
        }

        fireServerChangedEvent(null);

        try {
            setSize(Integer.parseInt(mProperties.getProperty("width")), Integer.parseInt(mProperties.getProperty("height")));
        } catch (Exception e) {
        }
        try {
            jSplitPane2.setDividerLocation(Integer.parseInt(mProperties.getProperty("split")));
        } catch (Exception e) {
        }

        jAboutDialog.getContentPane().setBackground(Constants.DS_BACK);
        jAboutDialog.pack();
    }

    public void showIntegratedVersion(String pServer) {
        showIntegratedVersion(Proxy.NO_PROXY, pServer);
    }

    public void showIntegratedVersion(Proxy webProxy, String pServer) {
        jServerList.setSelectedItem(pServer);
        fireServerChangedEvent(null);
        ConfigManager.getSingleton().setWebPoxy(webProxy);

        //setBaseFont((Font) UIManager.get("Label.font"));
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("A*Star");
        setVisible(true);
    }

    public void insertValuesExternally(HashMap<String, Double> pValues) {
        insertValuesExternally(null, pValues, null);
    }

    public void insertValuesExternally(Point pCoordinates, HashMap<String, Double> pValues, AStarResultReceiver pReceiver) {
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

    public void insertMultipleUnits(HashMap<UnitHolder, Integer> pUnits) {
        if (pUnits == null || pUnits.isEmpty()) {
            return;
        }
        int col = jAttackerTable.getSelectedColumn();
        int row = 0;
        for (UnitHolder unit : UnitManager.getSingleton().getUnits()) {
            jAttackerTable.setValueAt(pUnits.get(unit), row, col);
            row++;
        }
    }

    public void insertAttackers(HashMap<UnitHolder, Integer> pUnits) {
        if (pUnits == null || pUnits.isEmpty()) {
            return;
        }
        int col = 0;
        for (int i = 0; i < jAttackerTable.getColumnCount(); i++) {
            if (jAttackerTable.getColumnClass(i).equals(Integer.class)) {
                col = i;
                break;
            }
        }
        int row = 0;
        for (UnitHolder unit : UnitManager.getSingleton().getUnits()) {
            Integer val = pUnits.get(unit);
            if (val != null) {
                jAttackerTable.setValueAt(val, row, col);
            }
            row++;
        }
    }

    public void insertDefenders(HashMap<UnitHolder, Integer> pUnits) {
        if (pUnits == null || pUnits.isEmpty()) {
            return;
        }
        int col = 0;
        boolean haveFirst = false;
        for (int i = 0; i < jAttackerTable.getColumnCount(); i++) {
            if (jAttackerTable.getColumnClass(i).equals(Integer.class)) {
                if (!haveFirst) {
                    haveFirst = true;
                } else {
                    col = i;
                    break;
                }
            }
        }
        int row = 0;
        for (UnitHolder unit : UnitManager.getSingleton().getUnits()) {
            Integer val = pUnits.get(unit);
            if (val != null) {
                jAttackerTable.setValueAt(val, row, col);
            }
            row++;
        }
    }

    private void buildServerList() {
        ConfigManager.getSingleton().addListener(new EventListener() {
            @Override
            public void fireEvent() {
                DefaultComboBoxModel model = new DefaultComboBoxModel();
                for (String server : ConfigManager.getSingleton().getServers()) {
                    model.addElement(server);
                }
                jServerList.setModel(model);
            }
        });
        
        try {
            ConfigManager.getSingleton().loadServers();
        } catch (Exception e) {
            logger.warn("Failed to load server list. Message: {0}", e.getMessage());
        }
    }

    private void buildTables() {
        //build attacker table
        SimulatorTableModel attackerModel = SimulatorTableModel.getSingleton();
        jAttackerTable.setModel(attackerModel);
        jAttackerTable.setRowHeight(20);
        jAttackerTable.setDefaultEditor(Double.class, new TechLevelCellEditor((ConfigManager.getSingleton().getTech() == ConfigManager.ID_TECH_3) ? 3 : 10));
        jAttackerTable.setDefaultEditor(Integer.class, new SpreadSheetCellEditor());
        jAttackerTable.setDefaultRenderer(String.class, new UnitTableCellRenderer());
        jAttackerTable.setDefaultRenderer(Object.class, new MultiFunctionCellRenderer());
        jAttackerTable.setDefaultEditor(Object.class, new MultiCellEditor());
        if (ConfigManager.getSingleton().getTech() != ConfigManager.ID_SIMPLE_TECH) {
            //old model (empty, unit, attacker, tech,empty, defender, tech, empty)
            jAttackerTable.getColumnModel().getColumn(1).setMaxWidth(60);
            jAttackerTable.getColumnModel().getColumn(2).setMaxWidth(80);
            jAttackerTable.getColumnModel().getColumn(3).setMaxWidth(40);
            jAttackerTable.getColumnModel().getColumn(4).setMaxWidth(10);
            jAttackerTable.getColumnModel().getColumn(5).setMaxWidth(80);
            jAttackerTable.getColumnModel().getColumn(6).setMaxWidth(40);
            jAttackerTable.invalidate();
            for (UnitHolder unit : UnitManager.getSingleton().getUnits()) {
                attackerModel.addRow(new Object[]{null, unit.getPlainName(), 0, 1.0, null, 0, 1.0, null});
            }
            jAttackerTable.revalidate();
        } else {
            //new model (empty, unit, attacker, empty, defender, empty)
            jAttackerTable.getColumnModel().getColumn(1).setMaxWidth(50);
            jAttackerTable.getColumnModel().getColumn(2).setMaxWidth(70);
            jAttackerTable.getColumnModel().getColumn(3).setMaxWidth(5);
            jAttackerTable.getColumnModel().getColumn(4).setMaxWidth(70);
            jAttackerTable.invalidate();
            for (UnitHolder unit : UnitManager.getSingleton().getUnits()) {
                attackerModel.addRow(new Object[]{null, unit.getPlainName(), 0, null, 0, null});
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

        //setup knight items
        DefaultListModel model = new DefaultListModel();
        DefaultListModel model2 = new DefaultListModel();
        if (ConfigManager.getSingleton().getKnightType() == ConfigManager.ID_KNIGHT_WITH_ITEMS) {
            for (int i = 0; i <= KnightItem.ID_SNOB; i++) {
                model.addElement(KnightItem.factoryKnightItem(i));
                model2.addElement(KnightItem.factoryKnightItem(i));
            }
        } else {
            model.addElement(KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM));
            model2.addElement(KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM));
        }
        jOffKnightItemList.setModel(model);
        jDefKnightItemList.setModel(model2);
        jOffKnightItemList.setSelectedIndex(0);
        jDefKnightItemList.setSelectedIndex(0);
        //list selection listeners
        jOffKnightItemList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                KnightItem item = (KnightItem) jOffKnightItemList.getSelectedValue();
                if (item == null || item.getItemId() == KnightItem.ID_NO_ITEM) {
                    UnitHolder knight = UnitManager.getSingleton().getUnitByPlainName("knight");
                    if (knight != null) {
                        SimulatorTableModel.getSingleton().setOffUnitCount(knight, 0);
                    }
                } else {
                    UnitHolder knight = UnitManager.getSingleton().getUnitByPlainName("knight");
                    if (knight != null) {
                        SimulatorTableModel.getSingleton().setOffUnitCount(knight, 1);
                    }
                }
            }
        });
        jDefKnightItemList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object[] selection = jDefKnightItemList.getSelectedValues();

                if (selection == null || selection.length == 0) {
                    UnitHolder knight = UnitManager.getSingleton().getUnitByPlainName("knight");
                    if (knight != null) {
                        SimulatorTableModel.getSingleton().setDefUnitCount(knight, 0);
                    }
                } else {
                    int itemCount = 0;
                    boolean noItemSelected = false;
                    for (Object o : selection) {
                        KnightItem item = (KnightItem) o;
                        if (item.getItemId() != KnightItem.ID_NO_ITEM) {
                            itemCount++;
                        } else {
                            noItemSelected = true;
                        }
                    }
                    UnitHolder knight = UnitManager.getSingleton().getUnitByPlainName("knight");
                    if (knight != null) {
                        SimulatorTableModel.getSingleton().setDefUnitCount(knight, itemCount);
                    }
                    if (noItemSelected && itemCount > 0) {
                        jDefKnightItemList.getSelectionModel().removeSelectionInterval(0, 0);
                    }
                }
            }
        });
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

        Dimension dim = new Dimension(jScrollPane1.getWidth(), UnitManager.getSingleton().getUnits().length * 20 + 20 + 1);
        jTroopsPanel.setPreferredSize(dim);
        //jTroopsPanel.setMinimumSize(dim);
        jTroopsPanel.setSize(dim);
        jTroopsPanel.doLayout();
        pack();
    }

    public void updatePop() {
        HashMap<UnitHolder, AbstractUnitElement> troops = SimulatorTableModel.getSingleton().getOff();
        int cnt = 0;
        for(UnitHolder unit: troops.keySet()) {
            cnt += unit.getPop() * troops.get(unit).getCount();
        }
        jOffPop.setText(Integer.toString(cnt));
        troops = SimulatorTableModel.getSingleton().getDef();
        cnt = 0;
        for(UnitHolder unit: troops.keySet()) {
            cnt += unit.getPop() * troops.get(unit).getCount();
        }

        if (ConfigManager.getSingleton().getFarmLimit() != 0) {
            int max = (Integer) jFarmLevelSpinner.getValue() * ConfigManager.getSingleton().getFarmLimit();
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

        jAboutDialog = new javax.swing.JDialog();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jMenuPanel = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jServerList = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jAlwaysOnTopButton = new javax.swing.JToggleButton();
        jButton5 = new javax.swing.JButton();
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
        jScrollPane6 = new javax.swing.JScrollPane();
        jOffKnightItemList = new javax.swing.JList();
        jScrollPane5 = new javax.swing.JScrollPane();
        jDefKnightItemList = new javax.swing.JList();
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

        jAboutDialog.setTitle("About...");
        jAboutDialog.setAlwaysOnTop(true);
        jAboutDialog.setResizable(false);
        jAboutDialog.setUndecorated(true);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(400, 184));

        jLabel6.setForeground(new java.awt.Color(0, 51, 255));
        jLabel6.setText("http://www.dsworkbench.de");
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireOpenHomepageEvent(evt);
            }
        });

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("<html><u><b>eMail:</b></u></html>");

        jLabel9.setText("support@dsworkbench.de");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("<html><u><b>Web:</b></u></html>");

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("<html><u><b>IRC:</b></u></html>");

        jLabel11.setText("#ds-workbench @irc.quakenet.org");

        jLabel12.setText("Ein besonderer Dank geht an capibarbaroja, Leandro, Cheesaurus und unzählige andere Spieler dafür,");

        jLabel13.setText("dass sie unermüdlich nach den grundlegenden Formeln des DS Kampfsystems gesucht haben.");

        jLabel5.setText("<html>&copy; Torridity (2009 - 2012)</html>");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/astar_splash.gif"))); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 560, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(16, 16, 16))
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)))
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                                .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButton1.setText("Schließen");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireCloseAboutEvent(evt);
            }
        });

        javax.swing.GroupLayout jAboutDialogLayout = new javax.swing.GroupLayout(jAboutDialog.getContentPane());
        jAboutDialog.getContentPane().setLayout(jAboutDialogLayout);
        jAboutDialogLayout.setHorizontalGroup(
            jAboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAboutDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jAboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE))
                .addContainerGap())
        );
        jAboutDialogLayout.setVerticalGroup(
            jAboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAboutDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("A*Star 1.0");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jMenuPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jMenuPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        jMenuPanel.setOpaque(false);
        jMenuPanel.setPreferredSize(new java.awt.Dimension(100, 611));
        jMenuPanel.setLayout(new java.awt.GridBagLayout());

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/bomb.png"))); // NOI18N
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

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/refresh.png"))); // NOI18N
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

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/exit.png"))); // NOI18N
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

        jServerList.setToolTipText("Aktiver Server");
        jServerList.setMinimumSize(new java.awt.Dimension(60, 18));
        jServerList.setPreferredSize(new java.awt.Dimension(60, 18));
        jServerList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fireServerChangedEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jServerList, gridBagConstraints);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Server");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jLabel1, gridBagConstraints);

        jAlwaysOnTopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pin_grey.png"))); // NOI18N
        jAlwaysOnTopButton.setToolTipText("A*Star immer im Vordergrund halten");
        jAlwaysOnTopButton.setMaximumSize(new java.awt.Dimension(50, 33));
        jAlwaysOnTopButton.setMinimumSize(new java.awt.Dimension(50, 33));
        jAlwaysOnTopButton.setPreferredSize(new java.awt.Dimension(50, 33));
        jAlwaysOnTopButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pin_blue.png"))); // NOI18N
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

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/information.png"))); // NOI18N
        jButton5.setToolTipText("About...");
        jButton5.setMaximumSize(new java.awt.Dimension(50, 33));
        jButton5.setMinimumSize(new java.awt.Dimension(50, 33));
        jButton5.setPreferredSize(new java.awt.Dimension(50, 33));
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireInformationEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMenuPanel.add(jButton5, gridBagConstraints);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/attack_axe.png"))); // NOI18N
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

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/remove.png"))); // NOI18N
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

        jTransferButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/next.png"))); // NOI18N
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

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/add_attack.png"))); // NOI18N
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

        jScrollPane6.setMaximumSize(new java.awt.Dimension(180, 70));
        jScrollPane6.setMinimumSize(new java.awt.Dimension(180, 70));
        jScrollPane6.setPreferredSize(new java.awt.Dimension(180, 70));

        jOffKnightItemList.setToolTipText("Paladingegenstand des Angreifers");
        jScrollPane6.setViewportView(jOffKnightItemList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMiscInfoPanel.add(jScrollPane6, gridBagConstraints);

        jScrollPane5.setMaximumSize(new java.awt.Dimension(180, 70));
        jScrollPane5.setMinimumSize(new java.awt.Dimension(180, 70));
        jScrollPane5.setPreferredSize(new java.awt.Dimension(180, 70));

        jDefKnightItemList.setToolTipText("Paladingegenstände des Verteidigers");
        jScrollPane5.setViewportView(jDefKnightItemList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jMiscInfoPanel.add(jScrollPane5, gridBagConstraints);

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

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pop.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jMiscInfoPanel.add(jLabel4, gridBagConstraints);

        jLabel35.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pop.png"))); // NOI18N
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

        jLabel28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/wall.png"))); // NOI18N
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

        jLabel23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/cata.png"))); // NOI18N
        jLabel23.setToolTipText("Katapulte auf den Wall ausrichten");
        jCataWallPanel.add(jLabel23);

        jLabel24.setText(">>");
        jLabel24.setToolTipText("Katapulte auf den Wall ausrichten");
        jCataWallPanel.add(jLabel24);

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/wall.png"))); // NOI18N
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

        jLabel31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/main.png"))); // NOI18N
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

        jLabel26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/cata.png"))); // NOI18N
        jLabel26.setToolTipText("Katapulte auf die Kirche ausrichten");
        jCataChurchPanel.add(jLabel26);

        jLabel27.setText(">>");
        jLabel27.setToolTipText("Katapulte auf die Kirche ausrichten");
        jCataChurchPanel.add(jLabel27);

        jLabel37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/church.png"))); // NOI18N
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

        jFarmLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/farm.png"))); // NOI18N
        jFarmLabel.setToolTipText("Gebäudestufe des Bauernhofs");
        jFarmLabel.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/farm_disabled.png"))); // NOI18N
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

        jLabel29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/masks.png"))); // NOI18N
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

        jLabel30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/klee.png"))); // NOI18N
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
        jNightBonus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/sun.gif"))); // NOI18N
        jNightBonus.setIconTextGap(2);
        jNightBonus.setMargin(new java.awt.Insets(2, 0, 2, 2));
        jNightBonus.setMaximumSize(new java.awt.Dimension(100, 25));
        jNightBonus.setMinimumSize(new java.awt.Dimension(100, 25));
        jNightBonus.setPreferredSize(new java.awt.Dimension(100, 25));
        jNightBonus.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/moon.png"))); // NOI18N
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

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/holz.png"))); // NOI18N
        jLabel3.setToolTipText("Holz (Verluste)");

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/lehm.png"))); // NOI18N
        jLabel14.setToolTipText("Lehm (Verluste)");

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/eisen.png"))); // NOI18N
        jLabel15.setToolTipText("Eisen (Verluste)");

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pop.png"))); // NOI18N
        jLabel16.setToolTipText("Bevölkerung (Verluste)");

        jAttackerWood.setEditable(false);
        jAttackerWood.setToolTipText("Holz (Verluste)");

        jAttackerMud.setEditable(false);
        jAttackerMud.setToolTipText("Lehm (Verluste)");

        jAttackerIron.setEditable(false);
        jAttackerIron.setToolTipText("Eisen (Verluste)");

        jAttackerPop.setEditable(false);
        jAttackerPop.setToolTipText("Bevölkerung (Verluste)");

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/skull.png"))); // NOI18N
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
                        .addComponent(jAttackerWood, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(18, 18, 18)
                        .addComponent(jAttackerMud, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(jLabel21))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jAttackerBash, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                            .addComponent(jAttackerPop, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                            .addComponent(jAttackerIron, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jAttackerWood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jAttackerMud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jAttackerIron, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(jAttackerPop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21)
                    .addComponent(jAttackerBash, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/holz.png"))); // NOI18N
        jLabel17.setToolTipText("Holz (Verluste)");

        jLabel20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/pop.png"))); // NOI18N
        jLabel20.setToolTipText("Bevölkerung (Verluste)");

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/eisen.png"))); // NOI18N
        jLabel19.setToolTipText("Eisen (Verluste)");

        jDefenderIron.setEditable(false);
        jDefenderIron.setToolTipText("Eisen (Verluste)");

        jLabel18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/lehm.png"))); // NOI18N
        jLabel18.setToolTipText("Lehm (Verluste)");

        jDefenderPop.setEditable(false);
        jDefenderPop.setToolTipText("Bevölkerung (Verluste)");

        jLabel22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/skull.png"))); // NOI18N
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
                        .addComponent(jDefenderWood, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addGap(18, 18, 18)
                        .addComponent(jDefenderMud, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20)
                            .addComponent(jLabel22))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDefenderBash, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                            .addComponent(jDefenderPop, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                            .addComponent(jDefenderIron, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jDefenderWood, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(jDefenderMud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addComponent(jDefenderIron, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20)
                    .addComponent(jDefenderPop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addComponent(jDefenderBash, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        jLabel32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/wall.png"))); // NOI18N
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

        jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/main.png"))); // NOI18N
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

        jLabel34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/bomb_small.png"))); // NOI18N
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
        HashMap<UnitHolder, AbstractUnitElement> off = SimulatorTableModel.getSingleton().getOff();
        HashMap<UnitHolder, AbstractUnitElement> def = SimulatorTableModel.getSingleton().getDef();
        boolean nightBonus = jNightBonus.isSelected();
        int wallLevel = (Integer) jWallSpinner.getValue();
        int cataTarget = (Integer) jCataTargetSpinner.getValue();
        boolean cataFarm = false;
        boolean cataChurch = false;
        boolean cataWall = jAimWall.isSelected();
        if (ConfigManager.getSingleton().isChurch()) {
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
        //build knight item list
        List<KnightItem> defItems = new LinkedList<>();

        KnightItem offItem = (KnightItem) jOffKnightItemList.getSelectedValue();
        if (offItem == null) {
            offItem = KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM);
        }
        if (jDefKnightItemList.getSelectedIndices() != null) {
            for (int i : jDefKnightItemList.getSelectedIndices()) {
                KnightItem item = (KnightItem) jDefKnightItemList.getModel().getElementAt(i);
                if (item.getItemId() != KnightItem.ID_NO_ITEM) {
                    defItems.add(item);
                }
            }
        }
        SimulatorResult result = sim.bunkerBuster(off, def, offItem, defItems, nightBonus, luck, moral, wallLevel, cataTarget, farmLevel, attackerBelieve, defenderBelieve, cataChurch, cataFarm, cataWall);
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
            SimulatorTableModel.getSingleton().setDef(lastResult.getSurvivingDef());
            jWallSpinner.setValue(lastResult.getWallLevel());
            jCataTargetSpinner.setValue(lastResult.getBuildingLevel());
            fireCalculateEvent();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_fireAttackAgainEvent

    private void fireServerChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireServerChangedEvent
        if (evt == null || evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            try {
                logger.debug("reading server settings");
                String serverID = (String) jServerList.getSelectedItem();
                ConfigManager.getSingleton().parseConfig(serverID);
                UnitManager.getSingleton().parseUnits(serverID);
                logger.debug("setting up UI");
                SimulatorTableModel.getSingleton().reset();
                ResultTableModel.getSingleton().reset();
                SimulatorTableModel.getSingleton().addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent e) {
                        updatePop();
                    }
                });
                if (UnitManager.getSingleton().getUnitByPlainName("archer") != null) {
                    sim = new NewSimulator();
                    lastResult = null;
                } else {
                    sim = new OldSimulator();
                    lastResult = null;
                }

                jFarmLevelSpinner.setEnabled(ConfigManager.getSingleton().getFarmLimit() != 0);
                jFarmLabel.setEnabled(ConfigManager.getSingleton().getFarmLimit() != 0);
                jAttackerBelieve.setEnabled(ConfigManager.getSingleton().isChurch());
                jDefenderBelieve.setEnabled(ConfigManager.getSingleton().isChurch());
                jAimChurch.setEnabled(ConfigManager.getSingleton().isChurch());
                logger.debug("setting up tables");
                buildTables();
                buildResultTable(new SimulatorResult());
                mProperties.put(SERVER_PROP, serverID);
                logger.debug("finished server change");
            } catch (Exception e) {
                logger.error("Fehler beim Wechseln des Servers" ,e);
                fireGlobalWarningEvent("Fehler beim Wechseln des Servers (Grund: " + e.getMessage() + ")");
            }
        }
    }//GEN-LAST:event_fireServerChangedEvent

    private void fireBelieveChangedEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireBelieveChangedEvent
        //fireCalculateEvent();
    }//GEN-LAST:event_fireBelieveChangedEvent

    private void fireAlwaysOnTopChangeEvent(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fireAlwaysOnTopChangeEvent
        setAlwaysOnTop(jAlwaysOnTopButton.isSelected());
    }//GEN-LAST:event_fireAlwaysOnTopChangeEvent

    private void fireInformationEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireInformationEvent

        jAboutDialog.setLocationRelativeTo(this);
        jAboutDialog.setVisible(true);
    }//GEN-LAST:event_fireInformationEvent

    private void fireOpenHomepageEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireOpenHomepageEvent
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URL("https://forum.die-staemme.de/index.php?threads/ds-workbench.80831/").toURI());
            }
        } catch (URISyntaxException | IOException e) {
        }
}//GEN-LAST:event_fireOpenHomepageEvent

    private void fireCloseAboutEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCloseAboutEvent
        jAboutDialog.setVisible(false);
    }//GEN-LAST:event_fireCloseAboutEvent

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

    private void fireCalculateEvent() {
        HashMap<UnitHolder, AbstractUnitElement> off = SimulatorTableModel.getSingleton().getOff();
        HashMap<UnitHolder, AbstractUnitElement> def = SimulatorTableModel.getSingleton().getDef();
        boolean nightBonus = jNightBonus.isSelected();
        boolean cataFarm = false;
        boolean cataChurch = false;
        if (ConfigManager.getSingleton().isChurch()) {
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

        //build knight item list
        List<KnightItem> defItems = new LinkedList<>();

        KnightItem offItem = (KnightItem) jOffKnightItemList.getSelectedValue();
        if (offItem == null) {
            offItem = KnightItem.factoryKnightItem(KnightItem.ID_NO_ITEM);
        }
        if (jDefKnightItemList.getSelectedIndices() != null) {
            for (int i : jDefKnightItemList.getSelectedIndices()) {
                KnightItem item = (KnightItem) jDefKnightItemList.getModel().getElementAt(i);
                if (item.getItemId() != KnightItem.ID_NO_ITEM) {
                    defItems.add(item);
                }
            }
        }

        SimulatorResult result = sim.calculate(off, def, offItem, defItems, nightBonus, luck, moral, wallLevel, cataTarget, farmLevel, attackerBelieve, defenderBelieve, cataChurch, cataFarm, cataWall);
        jNukeInfo.setText("(Einzelangriff)");
        buildResultTable(result);
    }

    private void buildResultTable(SimulatorResult pResult) {

        // <editor-fold defaultstate="collapsed" desc="Build header renderer">
        for (int i = 0; i < jResultTable.getColumnCount(); i++) {
            jResultTable.getColumn(jResultTable.getColumnName(i)).setHeaderRenderer(new UnitTableCellRenderer());
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
                        l.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/wall.png")));
                    } else if (data.equals("Gebäude")) {
                        l.setText("");
                        l.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/icons/main.png")));
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
        HashMap<UnitHolder, AbstractUnitElement> off = sim.getOff();
        HashMap<UnitHolder, AbstractUnitElement> def = sim.getDef();
        pResult.setOffBefore(off);
        pResult.setDefBefore(def);
        pResult.setWallBefore((Integer) jWallSpinner.getValue());
        pResult.setCataAtWall(jAimWall.isSelected());
        if (off == null || def == null) {
            return;
        }

        //disabled due to missing information in unit_info
        //double attWood = 0;
        //double attMud = 0;
        //double attIron = 0;
        double attPop = 0;
        double attBash = 0;
        //double defWood = 0;
        //double defMud = 0;
        //double defIron = 0;
        double defPop = 0;
        double defBash = 0;
        for (int i = 1; i < ResultTableModel.getSingleton().getColumnCount(); i++) {
            UnitHolder u = UnitManager.getSingleton().getUnitByPlainName(ResultTableModel.getSingleton().getColumnName(i));
            AbstractUnitElement offElement = off.get(u);
            AbstractUnitElement defElement = def.get(u);
            int attackLoss = offElement.getCount() - pResult.getSurvivingOff().get(u).getCount();
            //attWood += u.getWood() * attackLoss;
            //attMud += u.getStone() * attackLoss;
            //attIron += u.getIron() * attackLoss;
            attPop += u.getPop() * attackLoss;
            defBash += getDefBash(u, attackLoss);
            //  attackerLosses.add(attackLoss);
            int defenderLoss = defElement.getCount() - pResult.getSurvivingDef().get(u).getCount();
            //defWood += u.getWood() * defenderLoss;
            //defMud += u.getStone() * defenderLoss;
            //defIron += u.getIron() * defenderLoss;
            defPop += u.getPop() * defenderLoss;
            attBash += getAttBash(u, defenderLoss);
            //defenderLosses.add(defenderLoss);
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        jAttackerWood.setText("Keine Informationen");//nf.format(attWood));
        jAttackerMud.setText("Keine Informationen");//nf.format(attMud));
        jAttackerIron.setText("Keine Informationen");//nf.format(attIron));
        jAttackerPop.setText(nf.format(attPop));
        jAttackerBash.setText(nf.format(attBash));
        jDefenderWood.setText("Keine Informationen");//nf.format(defWood));
        jDefenderMud.setText("Keine Informationen");//nf.format(defMud));
        jDefenderIron.setText("Keine Informationen");//nf.format(defIron));
        jDefenderPop.setText(nf.format(defPop));
        jDefenderBash.setText(nf.format(defBash));

        jResultTable.invalidate();
        ResultTableModel.getSingleton().addResult(pResult);
        jResultTable.revalidate();
    }

    private void updateResultSelection() {
        Integer[] datasets = getSelectedDataSets();
        //disabled due to missing information in unit_info
        //double attWood = 0;
        //double attMud = 0;
        //double attIron = 0;
        double attPop = 0;
        double attBash = 0;
        //double defWood = 0;
        //double defMud = 0;
        //double defIron = 0;
        double defPop = 0;
        double defBash = 0;
        for (Integer dataSetId : datasets) {
            SimulatorResult result = ResultTableModel.getSingleton().getResult(dataSetId);
            for(UnitHolder unit: result.getOffBefore().keySet()) {
                AbstractUnitElement offElement = result.getOffBefore().get(unit);
                AbstractUnitElement defElement = result.getDefBefore().get(unit);
                int attackLoss = offElement.getCount() - result.getSurvivingOff().get(unit).getCount();
                //attWood += u.getWood() * attackLoss;
                //attMud += u.getStone() * attackLoss;
                //attIron += u.getIron() * attackLoss;
                attPop += unit.getPop() * attackLoss;
                defBash += getDefBash(unit, attackLoss);
                int defenderLoss = defElement.getCount() - result.getSurvivingDef().get(unit).getCount();
                //defWood += u.getWood() * defenderLoss;
                //defMud += u.getStone() * defenderLoss;
                //defIron += u.getIron() * defenderLoss;
                defPop += unit.getPop() * defenderLoss;
                attBash += getAttBash(unit, defenderLoss);
            }
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        jAttackerWood.setText("Keine Informationen");//nf.format(attWood));
        jAttackerMud.setText("Keine Informationen");//nf.format(attMud));
        jAttackerIron.setText("Keine Informationen");//nf.format(attIron));
        jAttackerPop.setText(nf.format(attPop));
        jAttackerBash.setText(nf.format(attBash));
        jDefenderWood.setText("Keine Informationen");//nf.format(defWood));
        jDefenderMud.setText("Keine Informationen");//nf.format(defMud));
        jDefenderIron.setText("Keine Informationen");//nf.format(defIron));
        jDefenderPop.setText(nf.format(defPop));
        jDefenderBash.setText(nf.format(defBash));
    }

    private int getDefBash(UnitHolder pUnit, int pCount) {
        switch (pUnit.getPlainName()) {
            case "spear":
                return pCount * 1;
            case "sword":
                return pCount * 2;
            case "axe":
                return pCount * 4;
            case "archer":
                return pCount * 2;
            case "spy":
                return pCount * 2;
            case "light":
                return pCount * 13;
            case "marcher":
                return pCount * 12;
            case "heavy":
                return pCount * 15;
            case "ram":
                return pCount * 8;
            case "catapult":
                return pCount * 10;
            case "knight":
                return pCount * 20;
            case "snob":
                return pCount * 200;
            default:
                return pCount * 1;
        }
    }

    private int getAttBash(UnitHolder pUnit, int pCount) {
        switch (pUnit.getPlainName()) {
            case "spear":
                return pCount * 4;
            case "sword":
                return pCount * 5;
            case "axe":
                return pCount * 1;
            case "archer":
                return pCount * 5;
            case "spy":
                return pCount * 1;
            case "light":
                return pCount * 5;
            case "marcher":
                return pCount * 6;
            case "heavy":
                return pCount * 23;
            case "ram":
                return pCount * 4;
            case "catapult":
                return pCount * 12;
            case "knight":
                return pCount * 40;
            case "snob":
                return pCount * 200;
            default:
                return pCount * 1;
        }
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            logger.debug("loading servers");
            ConfigManager.getSingleton().loadServers();
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    try {
                        DSWorkbenchSimulatorFrame.getSingleton().setVisible(true);
                    } catch (Throwable t) {
                        JOptionPane.showMessageDialog(null, "A*Star konnte nicht gestartet werden. (Grund: " + t.getMessage() + ")");
                        System.exit(1);
                    }
                }
            });
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(null, "A*Star konnte nicht gestartet werden. (Grund: " + t.getMessage() + ")");
            System.exit(1);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog jAboutDialog;
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JPanel jCataChurchPanel;
    private javax.swing.JTextField jCataInfo;
    private javax.swing.JSpinner jCataTargetSpinner;
    private javax.swing.JPanel jCataWallPanel;
    private javax.swing.JList jDefKnightItemList;
    private javax.swing.JTextField jDefPop;
    private javax.swing.JTextField jDefenderBash;
    private javax.swing.JCheckBox jDefenderBelieve;
    private javax.swing.JTextField jDefenderIron;
    private javax.swing.JTextField jDefenderMud;
    private javax.swing.JTextField jDefenderPop;
    private javax.swing.JTextField jDefenderWood;
    private javax.swing.JLabel jFarmLabel;
    private javax.swing.JSpinner jFarmLevelSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
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
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner jLuckSpinner;
    private javax.swing.JPanel jMenuPanel;
    private javax.swing.JPanel jMiscInfoPanel;
    private javax.swing.JSpinner jMoralSpinner;
    private javax.swing.JCheckBox jNightBonus;
    private javax.swing.JTextField jNukeInfo;
    private javax.swing.JList jOffKnightItemList;
    private javax.swing.JTextField jOffPop;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jResultPanel;
    private javax.swing.JTabbedPane jResultTabbedPane;
    private javax.swing.JTable jResultTable;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JComboBox jServerList;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JButton jTransferButton;
    private javax.swing.JPanel jTroopsPanel;
    private javax.swing.JTextField jWallInfo;
    private javax.swing.JSpinner jWallSpinner;
    // End of variables declaration//GEN-END:variables
}
