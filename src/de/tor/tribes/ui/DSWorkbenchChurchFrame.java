/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DSWorkbenchChurchFrame.java
 *
 * Created on 29.03.2009, 15:11:27
 */
package de.tor.tribes.ui;

import de.tor.tribes.types.Village;
import de.tor.tribes.ui.editors.VillageCellEditor;
import de.tor.tribes.util.Constants;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.church.ChurchManager;
import de.tor.tribes.util.church.ChurchManagerListener;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Charon
 */
public class DSWorkbenchChurchFrame extends AbstractDSWorkbenchFrame implements ChurchManagerListener {

    private static DSWorkbenchChurchFrame SINGLETON = null;
    private List<TableCellRenderer> mHeaderRenderers = null;

    public static DSWorkbenchChurchFrame getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DSWorkbenchChurchFrame();
        }
        return SINGLETON;
    }

    /** Creates new form DSWorkbenchChurchFrame */
    DSWorkbenchChurchFrame() {
        initComponents();
        try {
            jChurchFrameAlwaysOnTop.setSelected(Boolean.parseBoolean(GlobalOptions.getProperty("church.frame.alwaysOnTop")));
            setAlwaysOnTop(jChurchFrameAlwaysOnTop.isSelected());
        } catch (Exception e) {
            //setting not available
        }
        mHeaderRenderers = new LinkedList<TableCellRenderer>();

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);
                c.setBackground(Constants.DS_BACK);
                DefaultTableCellRenderer r = ((DefaultTableCellRenderer) c);
                r.setText("<html><b>" + r.getText() + "</b></html>");
                return c;
            }
        };

        for (int i = 0; i < 2; i++) {
            mHeaderRenderers.add(headerRenderer);
        }

        jChurchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selected = jChurchTable.getSelectedRows().length;
                if (selected == 0) {
                    setTitle("Kirchen");
                } else if (selected == 1) {
                    setTitle("Kirchen (1 Kirche ausgewählt)");
                } else if (selected > 1) {
                    setTitle("Kirchen (" + selected + " Kirchen ausgewählt)");
                }
            }
        });

        /*
        // <editor-fold defaultstate="collapsed" desc=" Init HelpSystem ">
        GlobalOptions.getHelpBroker().enableHelpKey(getRootPane(), "pages.markers_view", GlobalOptions.getHelpBroker().getHelpSet());
        // </editor-fold>
         */
        pack();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jChurchTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jChurchFrameAlwaysOnTop = new javax.swing.JCheckBox();

        setTitle("Kirchen");

        jPanel1.setBackground(new java.awt.Color(239, 235, 223));

        jChurchTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jChurchTable);

        jButton1.setBackground(new java.awt.Color(239, 235, 223));
        jButton1.setText("Zentrieren");

        jButton2.setBackground(new java.awt.Color(239, 235, 223));
        jButton2.setText("Entfernen");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jChurchFrameAlwaysOnTop.setText("Immer im Vordergrund");
        jChurchFrameAlwaysOnTop.setOpaque(false);
        jChurchFrameAlwaysOnTop.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fireChurchFrameOnTopEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jChurchFrameAlwaysOnTop, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jChurchFrameAlwaysOnTop)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void fireChurchFrameOnTopEvent(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fireChurchFrameOnTopEvent
        setAlwaysOnTop(!isAlwaysOnTop());
    }//GEN-LAST:event_fireChurchFrameOnTopEvent

    protected void setupChurchPanel() {
        jChurchTable.invalidate();
        jChurchTable.setModel(ChurchManager.getSingleton().getTableModel());
        ChurchManager.getSingleton().addChurchManagerListener(this);
        //setup renderer and general view
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jChurchTable.getModel());
        jChurchTable.setRowSorter(sorter);

        jChurchTable.setDefaultEditor(Village.class, new VillageCellEditor());
        jScrollPane1.getViewport().setBackground(Constants.DS_BACK_LIGHT);
        //update view
        ChurchManager.getSingleton().churchesUpdatedExternally();
        jChurchTable.revalidate();
        jChurchTable.updateUI();

    }

    @Override
    public void fireChurchesChangedEvent() {
        jChurchTable.invalidate();
        jChurchTable.setModel(ChurchManager.getSingleton().getTableModel());

        //setup marker table view
        jChurchTable.getColumnModel().getColumn(1).setMaxWidth(75);

        for (int i = 0; i < jChurchTable.getColumnCount(); i++) {
            jChurchTable.getColumn(jChurchTable.getColumnName(i)).setHeaderRenderer(mHeaderRenderers.get(i));
        }

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(ChurchManager.getSingleton().getTableModel());
        jChurchTable.setRowSorter(sorter);
        jChurchTable.revalidate();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DSWorkbenchChurchFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jChurchFrameAlwaysOnTop;
    private javax.swing.JTable jChurchTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
