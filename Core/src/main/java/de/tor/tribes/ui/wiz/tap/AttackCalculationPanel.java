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
package de.tor.tribes.ui.wiz.tap;

import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.types.TroopMovement;
import de.tor.tribes.types.UserProfile;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackSourceElement;
import de.tor.tribes.ui.wiz.tap.types.TAPAttackTargetElement;
import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.JOptionPaneHelper;
import de.tor.tribes.util.algo.AbstractAttackAlgorithm;
import de.tor.tribes.util.algo.BruteForce;
import de.tor.tribes.util.algo.Iterix;
import de.tor.tribes.util.algo.types.TimeFrame;
import java.awt.BorderLayout;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.wizard.*;

/**
 *
 * @author Torridity
 */
public class AttackCalculationPanel extends WizardPage {

    private static final String GENERAL_INFO = "<html>Bist du hier angekommen, steht einer Berechnung der Angriffe nichts mehr im Wege. "
            + "Im oberen Bereich werden noch einmal Informationen zu den bisherigen Einstellungen angezeigt, im mittleren Bereich "
            + "k&ouml;nnen letzte Einstellungen vorgenommen werden, die im Normalfall jedoch nicht ver&auml;ndert werden m&uuml;ssen. "
            + "Mit einem Klick auf 'Angriffe berechnen' startet die Berechnung."
            + "</html>";
    private static AttackCalculationPanel singleton = null;
    private AbstractAttackAlgorithm calculator = null;
    private SimpleDateFormat dateFormat = null;

    public static synchronized AttackCalculationPanel getSingleton() {
        if (singleton == null) {
            singleton = new AttackCalculationPanel();
        }
        return singleton;
    }

    /**
     * Creates new form AttackSourcePanel
     */
    AttackCalculationPanel() {
        initComponents();
        jXCollapsiblePane1.setLayout(new BorderLayout());
        jXCollapsiblePane1.add(jInfoScrollPane, BorderLayout.CENTER);
        jInfoTextPane.setText(GENERAL_INFO);
        StyledDocument doc = (StyledDocument) jTextPane1.getDocument();
        Style defaultStyle = doc.addStyle("Default", null);
        StyleConstants.setItalic(defaultStyle, true);
        StyleConstants.setFontFamily(defaultStyle, "SansSerif");
        dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    public static String getDescription() {
        return "Berechnung";
    }

    public static String getStep() {
        return "id-attack-calculation";
    }

    public void storeProperties() {
        UserProfile profile = GlobalOptions.getSelectedProfile();
        int algo = 0;
        if (jSystematicCalculation.isSelected()) {
            algo = 1;
        }
        profile.addProperty("tap.calculation.algo", algo);
        profile.addProperty("tap.calculation.fake.off", jAllowFakeOffs.isSelected());
    }

    public void restoreProperties() {
        calculator = null;
        UserProfile profile = GlobalOptions.getSelectedProfile();
        String value = profile.getProperty("tap.calculation.algo");

        int type = 0;
        try {
            type = Integer.parseInt(value);
        } catch (Exception ignored) {
        }
        if (type == 0) {
            jBruteForce.setSelected(true);
        } else {
            jSystematicCalculation.setSelected(true);
        }
        jAllowFakeOffs.setSelected(Boolean.parseBoolean(profile.getProperty("tap.calculation.fake.off")));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jInfoScrollPane = new javax.swing.JScrollPane();
        jInfoTextPane = new javax.swing.JTextPane();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jXCollapsiblePane1 = new org.jdesktop.swingx.JXCollapsiblePane();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jAllowFakeOffs = new javax.swing.JCheckBox();
        jBruteForce = new javax.swing.JRadioButton();
        jSystematicCalculation = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jCalculateButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jOverallSources = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jOverallAttacks = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jOverallFakes = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTargetAttacks = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jOverallTargets = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTargetFakes = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();

        jInfoScrollPane.setMinimumSize(new java.awt.Dimension(19, 180));
        jInfoScrollPane.setPreferredSize(new java.awt.Dimension(19, 180));

        jInfoTextPane.setContentType("text/html"); // NOI18N
        jInfoTextPane.setEditable(false);
        jInfoTextPane.setText("<html>Du befindest dich im <b>Angriffsmodus</b>. Hier kannst du die Herkunftsd&ouml;rfer ausw&auml;hlen, die f&uuml;r Angriffe verwendet werden d&uuml;rfen. Hierf&uuml;r hast die folgenden M&ouml;glichkeiten:\n<ul>\n<li>Einf&uuml;gen von Dorfkoordinaten aus der Zwischenablage per STRG+V</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus der Gruppen&uuml;bersicht</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus dem SOS-Analyzer</li>\n<li>Einf&uuml;gen der Herkunftsd&ouml;rfer aus Berichten</li>\n<li>Einf&uuml;gen aus der Auswahlübersicht</li>\n<li>Manuelle Eingabe</li>\n</ul>\n</html>\n");
        jInfoScrollPane.setViewportView(jInfoTextPane);

        setLayout(new java.awt.GridBagLayout());

        jXCollapsiblePane1.setCollapsed(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jXCollapsiblePane1, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Informationen einblenden");
        jLabel1.setToolTipText("Blendet Informationen zu dieser Ansicht und zu den Datenquellen ein/aus");
        jLabel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fireHideInfoEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Einstellungen"));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jAllowFakeOffs.setText("Fakes auf Off-Ziele erlauben");
        jAllowFakeOffs.setToolTipText("<html>Erlaubt das Zuweisen von Fakes auf Off-Ziele, die nicht mit Offs belegt werden konnten.<br/>Aktiviere diese Option, falls keine Fakes zugewiesen werden konnten  oder zuviele Off-Ziele &uuml;brig bleiben.</html>");
        jAllowFakeOffs.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jAllowFakeOffs, gridBagConstraints);

        buttonGroup1.add(jBruteForce);
        jBruteForce.setSelected(true);
        jBruteForce.setText("Zufällige Berechnung");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jBruteForce, gridBagConstraints);

        buttonGroup1.add(jSystematicCalculation);
        jSystematicCalculation.setText("Systematische Berechnung");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(jSystematicCalculation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel3, gridBagConstraints);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Informationen zur Berechnung"));
        jScrollPane1.setViewportView(jTextPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        jCalculateButton.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jCalculateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ui/select.png"))); // NOI18N
        jCalculateButton.setText("Angriffe berechnen");
        jCalculateButton.setMaximumSize(new java.awt.Dimension(167, 40));
        jCalculateButton.setMinimumSize(new java.awt.Dimension(167, 40));
        jCalculateButton.setPreferredSize(new java.awt.Dimension(167, 40));
        jCalculateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fireCalculateAttacksEvent(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jCalculateButton, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Zusammenfassung"));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Angreifende Dörfer");
        jLabel2.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel2, gridBagConstraints);

        jOverallSources.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jOverallSources, gridBagConstraints);

        jLabel4.setText("<html>&nbsp;&nbsp;&nbsp;Angriffe</html>");
        jLabel4.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel4, gridBagConstraints);

        jOverallAttacks.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jOverallAttacks, gridBagConstraints);

        jLabel6.setText("<html>&nbsp;&nbsp;&nbsp;Fakes</html>");
        jLabel6.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel6, gridBagConstraints);

        jOverallFakes.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jOverallFakes, gridBagConstraints);

        jLabel8.setText("<html>&nbsp;&nbsp;&nbsp;Angriffe</html>");
        jLabel8.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel8, gridBagConstraints);

        jTargetAttacks.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jTargetAttacks, gridBagConstraints);

        jLabel10.setText("Angegriffene Dörfer");
        jLabel10.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel10, gridBagConstraints);

        jOverallTargets.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jOverallTargets, gridBagConstraints);

        jLabel12.setText("<html>&nbsp;&nbsp;&nbsp;Fakes</html>");
        jLabel12.setPreferredSize(new java.awt.Dimension(200, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jLabel12, gridBagConstraints);

        jTargetFakes.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(jTargetFakes, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jPanel1, gridBagConstraints);

        jProgressBar1.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(jProgressBar1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel2, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void fireHideInfoEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideInfoEvent
        if (jXCollapsiblePane1.isCollapsed()) {
            jXCollapsiblePane1.setCollapsed(false);
            jLabel1.setText("Informationen ausblenden");
        } else {
            jXCollapsiblePane1.setCollapsed(true);
            jLabel1.setText("Informationen einblenden");
        }
    }//GEN-LAST:event_fireHideInfoEvent

    private void fireCalculateAttacksEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireCalculateAttacksEvent
        if (calculator == null) {//not used yet
            initializeCalculation();
        } else {//in use or finished
            if (calculator.isRunning()) {//in use...abort
                calculator.abort();
                return;
            } else {//not in use...recalculate
                if (calculator.hasResults() && JOptionPaneHelper.showQuestionConfirmBox(this, "Vorherige Berechnung verwerfen?", "Berechnung verwerfen", "Nein", "Ja") == JOptionPane.NO_OPTION) {
                    //not recalculate
                    return;
                } else {
                    //recalculate
                    initializeCalculation();
                }
            }
        }

        jCalculateButton.setText("Abbrechen");
        calculator.start();
        setBusy(true);
        //wait until calculation is running
        try {
            Thread.sleep(20);
        } catch (Exception ignored) {
        }

    }//GEN-LAST:event_fireCalculateAttacksEvent

    private void initializeCalculation() {
        if (jBruteForce.isSelected()) {
            calculator = new BruteForce();
        } else if (jSystematicCalculation.isSelected()) {
            calculator = new Iterix();
        }
        HashMap<UnitHolder, List<Village>> sources = new HashMap<>();
        HashMap<UnitHolder, List<Village>> fakeSources = new HashMap<>();
        for (TAPAttackSourceElement element : AttackSourceFilterPanel.getSingleton().getFilteredElements()) {
            List<Village> sourcesForUnit;
            if (element.isFake()) {
                sourcesForUnit = fakeSources.get(element.getUnit());
                if (sourcesForUnit == null) {
                    sourcesForUnit = new LinkedList<>();
                    fakeSources.put(element.getUnit(), sourcesForUnit);
                }
            } else {
                sourcesForUnit = sources.get(element.getUnit());
                if (sourcesForUnit == null) {
                    sourcesForUnit = new LinkedList<>();
                    sources.put(element.getUnit(), sourcesForUnit);
                }
            }
            sourcesForUnit.add(element.getVillage());
        }
        List<Village> targets = new LinkedList<>();
        List<Village> fakeTargets = new LinkedList<>();
        HashMap<Village, Integer> maxAttacks = new HashMap<>();
        for (TAPAttackTargetElement element : AttackTargetFilterPanel.getSingleton().getFilteredElements()) {
            if (element.isFake()) {
                fakeTargets.add(element.getVillage());
            } else {
                targets.add(element.getVillage());
            }
            maxAttacks.put(element.getVillage(), element.getAttacks());
        }

        TimeFrame timeFrame = TimeSettingsPanel.getSingleton().getTimeFrame();
        calculator.initialize(sources, fakeSources, targets, fakeTargets, maxAttacks, timeFrame, jAllowFakeOffs.isSelected());
        jProgressBar1.setValue(0);
        calculator.setLogListener(new AbstractAttackAlgorithm.LogListener() {

            @Override
            public void logMessage(String pMessage) {
                notifyStatusUpdate(pMessage);
            }

            @Override
            public void calculationFinished() {
                notifyCalculationFinished();
            }

            @Override
            public void updateProgress(double pPercent) {
                jProgressBar1.setValue((int) Math.rint(pPercent));
            }
        });
    }

    public void updateStatus() {
        TAPAttackSourceElement[] elements = AttackSourceFilterPanel.getSingleton().getFilteredElements();
        jOverallSources.setText(Integer.toString(elements.length));
        int offs = 0;
        int fakes = 0;
        for (TAPAttackSourceElement element : elements) {
            if (element.isFake()) {
                fakes++;
            } else {
                offs++;
            }
        }

        jOverallAttacks.setText(Integer.toString(offs));
        jOverallFakes.setText(Integer.toString(fakes));

        TAPAttackTargetElement[] targetElements = AttackTargetFilterPanel.getSingleton().getFilteredElements();
        jOverallTargets.setText(Integer.toString(targetElements.length));
        offs = 0;
        fakes = 0;
        for (TAPAttackTargetElement element : targetElements) {
            if (element.isFake()) {
                fakes += element.getAttacks();
            } else {
                offs += element.getAttacks();
            }
        }

        jTargetAttacks.setText(Integer.toString(offs));
        jTargetFakes.setText(Integer.toString(fakes));
    }

    public void notifyCalculationFinished() {
        setBusy(false);
        if (calculator.hasResults()) {
            setProblem(null);
        } else {
            setProblem("Berechnung erzielte keine Ergebnisse");
        }
        jCalculateButton.setText("Angriffe berechnen");
    }

    public void notifyStatusUpdate(String pMessage) {
        try {
            StyledDocument doc = jTextPane1.getStyledDocument();
            doc.insertString(doc.getLength(), "(" + dateFormat.format(new Date(System.currentTimeMillis())) + ") " + pMessage + "\n", doc.getStyle("Info"));
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    scroll();
                }
            });
        } catch (BadLocationException ignored) {
        }
    }

    private void scroll() {
        Point point = new Point(0, (int) (jTextPane1.getSize().getHeight()));
        JViewport vp = jScrollPane1.getViewport();
        if ((vp == null) || (point == null)) {
            return;
        }
        vp.setViewPosition(point);
    }

    public List<TroopMovement> getResults() {
        return calculator.getResults();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox jAllowFakeOffs;
    private javax.swing.JRadioButton jBruteForce;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JScrollPane jInfoScrollPane;
    private javax.swing.JTextPane jInfoTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jOverallAttacks;
    private javax.swing.JLabel jOverallFakes;
    private javax.swing.JLabel jOverallSources;
    private javax.swing.JLabel jOverallTargets;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton jSystematicCalculation;
    private javax.swing.JLabel jTargetAttacks;
    private javax.swing.JLabel jTargetFakes;
    private javax.swing.JTextPane jTextPane1;
    private org.jdesktop.swingx.JXCollapsiblePane jXCollapsiblePane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public WizardPanelNavResult allowNext(String string, Map map, Wizard wizard) {
        if (calculator == null) {
            setProblem("Noch keine Berechnung durchgeführt");
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        if (calculator != null && calculator.isRunning()) {
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        AttackFinishPanel.getSingleton().update();
        return WizardPanelNavResult.PROCEED;
    }

    @Override
    public WizardPanelNavResult allowBack(String string, Map map, Wizard wizard) {
        if (calculator != null && calculator.isRunning()) {
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        return WizardPanelNavResult.PROCEED;

    }

    @Override
    public WizardPanelNavResult allowFinish(String string, Map map, Wizard wizard) {
        if (calculator != null && calculator.isRunning()) {
            return WizardPanelNavResult.REMAIN_ON_PAGE;
        }
        return WizardPanelNavResult.PROCEED;
    }
}
