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
package de.tor.tribes.ui.components;

import de.tor.tribes.control.GenericEventListener;
import javax.swing.JTabbedPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author extremeCrazyCoder
 */
public class TabPaneComponent extends javax.swing.JPanel {
    private static Logger logger = LogManager.getLogger("TabPaneComponent");
    
    private boolean editing = false;
    private boolean editable = true;
    private long lastClick = -1;
    
    private boolean closeable = true;
    
    private final JTabbedPane pane;
    
    private GenericEventListener startEditingListener = null;
    private GenericEventListener stopEditingListener = null;
    private GenericEventListener closeTabListener = null;
    
    /**
     * Creates new form TabPaneComponent
     */
    public TabPaneComponent(final JTabbedPane pPane) {
        this.pane = pPane;
        initComponents();
        rebuildUI();
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

        jLabelName = new javax.swing.JLabel() {
            @Override
            public String getText() {
                int i = pane.indexOfTabComponent(TabPaneComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
        jTextFieldName = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLabelName.setText("jLabel1");
        jLabelName.setOpaque(true);
        jLabelName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                startEditing(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jLabelName, gridBagConstraints);

        jTextFieldName.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jTextFieldName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                stopEditing(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jTextFieldName, gridBagConstraints);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/red_x.png"))); // NOI18N
        jButton1.setToolTipText("Delete this plan");
        jButton1.setBorderPainted(false);
        jButton1.setFocusable(false);
        jButton1.setMaximumSize(new java.awt.Dimension(17, 17));
        jButton1.setMinimumSize(new java.awt.Dimension(17, 17));
        jButton1.setPreferredSize(new java.awt.Dimension(17, 17));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeTab(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(jButton1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void startEditing(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startEditing
        if(editable) {
            if(System.currentTimeMillis() - lastClick < 1000) {
                editing = true;
                rebuildUI();
                jTextFieldName.setText(jLabelName.getText());
                jTextFieldName.requestFocus();
                
                if(startEditingListener != null) {
                    startEditingListener.event();
                }
            }
            lastClick = System.currentTimeMillis();
        }
        
        int i = pane.indexOfTabComponent(TabPaneComponent.this);
        pane.setSelectedIndex(i);
    }//GEN-LAST:event_startEditing

    private void stopEditing(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_stopEditing
        editing = false;
        rebuildUI();
        if(!editable) return;
        
        if(stopEditingListener != null) {
            stopEditingListener.event();
        }
    }//GEN-LAST:event_stopEditing

    private void closeTab(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeTab
        if(!closeable) return;
        
        if(closeTabListener != null) {
            closeTabListener.event();
        }
    }//GEN-LAST:event_closeTab


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables

    private void rebuildUI() {
        if(editing) {
            jTextFieldName.setVisible(true);
            jLabelName.setVisible(false);
        }
        else {
            jTextFieldName.setVisible(false);
            jLabelName.setVisible(true);
        }
        
        if(closeable) {
            jButton1.setVisible(true);
        }
        else {
            jButton1.setVisible(false);
        }
    }

    public void setStartEditingListener(GenericEventListener startEditingListener) {
        this.startEditingListener = startEditingListener;
    }

    public void setStopEditingListener(GenericEventListener stopEditingListener) {
        this.stopEditingListener = stopEditingListener;
    }

    public void setCloseTabListener(GenericEventListener closeTabListener) {
        this.closeTabListener = closeTabListener;
    }
    
    public String getEditedText() {
        return jTextFieldName.getText();
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
        rebuildUI();
    }
}
