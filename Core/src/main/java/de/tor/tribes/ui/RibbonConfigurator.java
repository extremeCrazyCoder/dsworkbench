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
package de.tor.tribes.ui;

import de.tor.tribes.dssim.ui.DSWorkbenchSimulatorFrame;
import de.tor.tribes.ui.panels.MapPanel;
import de.tor.tribes.ui.panels.MinimapPanel;
import de.tor.tribes.ui.views.*;
import de.tor.tribes.ui.windows.BBCodeEditor;
import de.tor.tribes.ui.windows.ClockFrame;
import de.tor.tribes.ui.windows.DSWorkbenchMainFrame;
import de.tor.tribes.ui.windows.FormConfigFrame;
import de.tor.tribes.ui.wiz.red.ResourceDistributorWizard;
import de.tor.tribes.ui.wiz.tap.TacticsPlanerWizard;
import de.tor.tribes.util.*;
import de.tor.tribes.util.translation.TranslationManager;
import de.tor.tribes.util.translation.Translator;
import de.tor.tribes.util.village.KnownVillageManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.EmptyResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;

/**
 * @author Torridity
 * @author extremeCrazyCoder
 */
public class RibbonConfigurator {
    private static Logger logger = LogManager.getLogger("RibbonConfigurator");

    private static Translator trans = TranslationManager.getTranslator("ui.RibbonConfigurator");
    
    public static void addAppIcons(JRibbonFrame frame) {
        RibbonApplicationMenu appmen = new RibbonApplicationMenu();

        // <editor-fold defaultstate="collapsed" desc="Main Menue">
        frame.setApplicationIcon(getResizableIconFromFile("graphics/big/axe.png"));

        RibbonApplicationMenuEntryPrimary importEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/24x24/load.png"), "Import", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().doImport();
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);
        RibbonApplicationMenuEntryPrimary exportEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/24x24/save.png"), "Export", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().doExport();
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        exportEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel(trans.get("ExportDaten")), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });
        importEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel(trans.get("ImportDaten")), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });

        appmen.addMenuEntry(importEntry);
        appmen.addMenuEntry(exportEntry);
        appmen.addMenuSeparator();

        RibbonApplicationMenuEntryPrimary bbEditorEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/bbeditor.png"), trans.get("BBTemplate"), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BBCodeEditor.getSingleton().setVisible(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        bbEditorEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel(trans.get("BBEditor")), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });

        appmen.addMenuEntry(bbEditorEntry);

        RibbonApplicationMenuEntryPrimary standardAttackFrame = new RibbonApplicationMenuEntryPrimary(getResizableIconFromResource("/res/ui/troop_info_add.png"), trans.get("Standardangriffe"), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TroopSetupConfigurationFrame.getSingleton().setVisible(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        standardAttackFrame.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel(trans.get("Standardangriffe_Text")), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });

        appmen.addMenuEntry(standardAttackFrame);

        if (!GlobalOptions.isMinimal()) {
            RibbonApplicationMenuEntryPrimary layerEditor = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/24x24/layer_settings.gif"), trans.get("Ebeneneinstellungen"), new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LayerOrderConfigurationFrame.getSingleton().setAlwaysOnTop(true);
                    LayerOrderConfigurationFrame.getSingleton().setVisible(true);
                }
            }, JCommandButton.CommandButtonKind.ACTION_ONLY);

            layerEditor.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
                @Override
                public void menuEntryActivated(JPanel targetPanel) {
                    targetPanel.removeAll();
                    targetPanel.setLayout(new BorderLayout());
                    targetPanel.add(new JLabel(trans.get("Ebenen_Text")), BorderLayout.CENTER);
                    targetPanel.revalidate();
                }
            });

            appmen.addMenuEntry(layerEditor);
        }

        RibbonApplicationMenuEntryPrimary settingsEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/settings.png"), trans.get("Einstellungen"), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalOptions.storeViewStates();
                DSWorkbenchSettingsDialog.getSingleton().setVisible(true);
            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        settingsEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel(trans.get("Einstellungen_Text")), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });

        appmen.addMenuEntry(settingsEntry);

        appmen.addMenuSeparator();
        RibbonApplicationMenuEntryPrimary exitEntry = new RibbonApplicationMenuEntryPrimary(getResizableIconFromFile("graphics/icons/logout.png"), trans.get("Beenden"), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().doExit();

            }
        }, JCommandButton.CommandButtonKind.ACTION_ONLY);

        exitEntry.setRolloverCallback(new RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback() {
            @Override
            public void menuEntryActivated(JPanel targetPanel) {
                targetPanel.removeAll();
                targetPanel.setLayout(new BorderLayout());
                targetPanel.add(new JLabel(trans.get("Beenden_Text")), BorderLayout.CENTER);
                targetPanel.revalidate();
            }
        });
        appmen.addMenuEntry(exitEntry);
        appmen.addFooterEntry(new RibbonApplicationMenuEntryFooter(getResizableIconFromFile("graphics/icons/logout.png"), trans.get("Beenden"), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().doExit();
            }
        }));

        frame.getRibbon().setApplicationMenu(appmen);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Help button">
        frame.getRibbon().configureHelp(getResizableIconFromFile("graphics/big/help2.png"),
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Constants.DEBUG) {
                    GlobalOptions.getHelpBroker().setDisplayed(true);
                }
            }
        });
        // </editor-fold>
    }

    public static void addGeneralToolsTask(JRibbonFrame frame) {
        JRibbonBand attackToolsBand = new JRibbonBand(trans.get("Angriff"), getResizableIconFromFile("graphics/big/axe.png"));
        JRibbonBand defendToolsBand = new JRibbonBand(trans.get("Verteidigung"), getResizableIconFromFile("graphics/big/def.png"));
        JRibbonBand infoToolBand = new JRibbonBand(trans.get("Information"), getResizableIconFromFile("graphics/big/information.png"));
        JRibbonBand miscToolsBand = new JRibbonBand(trans.get("Sonstige"), getResizableIconFromFile("graphics/big/box.png"));

        // <editor-fold defaultstate="collapsed" desc="attackToolsBand setup">
        JCommandButton attackPlanerToolButton = factoryButton(trans.get("Taktikplaner"), "graphics/big/att_auto.png", trans.get("OeffnetdenTaktikplaner"),
                trans.get("Taktikplaner_Text"), true);
        attackPlanerToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        TacticsPlanerWizard.show();
                    }
                });
            }
        });
        JCommandButton manualAttackPlanerToolButton = factoryButton(trans.get("Angriffsplaner"), "graphics/big/att_manual.png", trans.get("open_angriffsplaner"), trans.get("angriffsplaner_text"), true);
        manualAttackPlanerToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().setVisible(true);
                        DSWorkbenchDoItYourselfAttackPlaner.getSingleton().requestFocus();
                    }
                });
            }
        });
        attackToolsBand.addCommandButton(attackPlanerToolButton, RibbonElementPriority.TOP);
        attackToolsBand.addCommandButton(manualAttackPlanerToolButton, RibbonElementPriority.TOP);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="defendToolsBand setup">
        JCommandButton astarToolButton = factoryButton(trans.get("AStar"), "graphics/big/astar.png", trans.get("Simulator"), trans.get("AStar_Text"), true);
        astarToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchSimulatorFrame.getSingleton().setVisible(true);
                        DSWorkbenchSimulatorFrame.getSingleton().requestFocus();
                    }
                });

            }
        });
        JCommandButton sosAnalyzerToolButton = factoryButton(trans.get("SOSAnalyzer"), "graphics/big/lifebelt.png", trans.get("open_SOSAnalyzer"), trans.get("SOSAnalyzer_Text"), true);
        sosAnalyzerToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchSOSRequestAnalyzer.getSingleton().setVisible(true);
                        DSWorkbenchSOSRequestAnalyzer.getSingleton().requestFocus();
                    }
                });
            }
        });
        /*
         * JCommandButton retimeToolButton = factoryButton("Re-Timer", "graphics/big/retime.png", "Öffnet den Re-Timer", "Der Re-Timer
         * erlaubt es zu Einzelangriffen, die man einfach aus dem Spiel in ein entsprechendes Textfeld kopiert, mögliche re-time Angriffe zu
         * berechnen, welche die angreifenden Truppen bei der Rückkehr in ihr Herkunftsdorf vernichten können. Voraussetzung sind korrekt
         * importierte Truppeninformationen aus dem Spiel (siehe Hilfe) und ein gutes Timing.", true);
         * retimeToolButton.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { SwingUtilities.invokeLater(new Runnable() {
         *
         * @Override public void run() { DSWorkbenchReTimerFrame.getSingleton().setVisible(true);
         * DSWorkbenchReTimerFrame.getSingleton().requestFocus(); } }); } });
         */
        defendToolsBand.addCommandButton(astarToolButton, RibbonElementPriority.MEDIUM);
        defendToolsBand.addCommandButton(sosAnalyzerToolButton, RibbonElementPriority.MEDIUM);
        // defendToolsBand.addCommandButton(retimeToolButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="infoToolBand setup">
        JCommandButton selectionToolButton = factoryButton(trans.get("Auswahluebersicht"), "graphics/icons/selection.png", trans.get("open_Auswahluebersicht"), trans.get("DieAuswahluebersicht_text"), true);
        selectionToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchSelectionFrame.getSingleton().setVisible(true);
                        DSWorkbenchSelectionFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton searchToolButton = factoryButton(trans.get("Suche"), "graphics/big/find.png", trans.get("werkzeug_suche"), trans.get("Werkzeug_text"), true);
        searchToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchSearchFrame.getSingleton().setVisible(true);
                        DSWorkbenchSearchFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton distanceToolButton = factoryButton(trans.get("Entfernungsberechnung"), "graphics/icons/measure.png", trans.get("open_Entfernungsberechnung"), trans.get("Entfernungsberechnung_text"), true);
        distanceToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchDistanceFrame.getSingleton().setVisible(true);
                        DSWorkbenchDistanceFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        infoToolBand.addCommandButton(selectionToolButton, RibbonElementPriority.MEDIUM);
        infoToolBand.addCommandButton(searchToolButton, RibbonElementPriority.LOW);
        infoToolBand.addCommandButton(distanceToolButton, RibbonElementPriority.LOW);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="miscToolsBand setup">
        JCommandButton resourceDistributorToolButton = factoryButton(
                trans.get("Rohstoffverteiler"), "graphics/big/resource_distrib.png", 
                trans.get("Rohstoffverteiler_tool"), 
                trans.get("Rohstoffverteiler_text"), true);
        resourceDistributorToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        ResourceDistributorWizard.show();
                    }
                });
            }
        });

        JCommandButton farmManagerButton = factoryButton(trans.get("Farmmanager"), "graphics/big/farm_tool.png", 
                trans.get("Farmmanager_tool"), 
                trans.get("Farmmanager_text"), true);
        farmManagerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isHaulActive()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            DSWorkbenchFarmManager.getSingleton().setVisible(true);
                            DSWorkbenchFarmManager.getSingleton().requestFocus();
                        }
                    });
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo(trans.get("Farmmanager_notactive"));
                }
            }
        });

        JCommandButton mapshotToolButton = factoryButton(trans.get("Screenshoterstellen"), 
                "graphics/big/camera.png", 
                trans.get("Screenshot_tool"), 
                trans.get("Screenshot_text"), true);
        mapshotToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().planMapshot();
            }
        });
        JCommandButton runtimeToolButton = factoryButton(trans.get("Laufzeiten"), "graphics/big/speed.png", 
                trans.get("Laufzeiten_tool"), 
                trans.get("Laufzeiten_text"), true);
        runtimeToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        UnitOrderBuilder.showUnitOrder(null, null);
                    }
                });

            }
        });
        JCommandButton clockToolButton = factoryButton(trans.get("Uhr"), "graphics/big/clock.png", 
                trans.get("Uhr_tool"), 
                trans.get("Uhr_Text"), true);
        clockToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        ClockFrame.getSingleton().setVisible(true);
                        ClockFrame.getSingleton().requestFocus();
                    }
                });

            }
        });
        miscToolsBand.addCommandButton(resourceDistributorToolButton, RibbonElementPriority.TOP);
        miscToolsBand.addCommandButton(farmManagerButton, RibbonElementPriority.TOP);
        miscToolsBand.addCommandButton(mapshotToolButton, RibbonElementPriority.MEDIUM);
        miscToolsBand.addCommandButton(runtimeToolButton, RibbonElementPriority.MEDIUM);
        miscToolsBand.addCommandButton(clockToolButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>

        attackToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(attackToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(attackToolsBand.getControlPanel())));
        defendToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(defendToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(defendToolsBand.getControlPanel())));

        infoToolBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(infoToolBand.getControlPanel()),
                //new CoreRibbonResizePolicies.Mid2Low(infoToolBand.getControlPanel()),
                new IconRibbonBandResizePolicy(infoToolBand.getControlPanel())));

        miscToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(miscToolsBand.getControlPanel()),
                //new CoreRibbonResizePolicies.Mid2Low(miscToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(miscToolsBand.getControlPanel())));

        RibbonTask task1 = new RibbonTask(trans.get("AllgemeineWerkzeuge"), attackToolsBand, defendToolsBand, infoToolBand, miscToolsBand);
        frame.getRibbon().addTask(task1);
    }

    public static void addMapToolsTask(JRibbonFrame frame) {
        JRibbonBand baseToolsBand = new JRibbonBand(trans.get("Allgemein"), getResizableIconFromFile("graphics/cursors/default.png"));
        JRibbonBand attackToolsBand = new JRibbonBand(trans.get("Angriff"), getResizableIconFromFile("graphics/big/axe.png"));
        JRibbonBand defendToolsBand = new JRibbonBand(trans.get("Verteidigung"), getResizableIconFromFile("graphics/big/def.png"));
        JRibbonBand infoToolBand = new JRibbonBand(trans.get("Doerfer"), getResizableIconFromFile("graphics/icons/map_tools.png"));
        JRibbonBand drawToolsBand = new JRibbonBand(trans.get("Zeichnen"), getResizableIconFromFile("graphics/big/palette2.png"));
        JRibbonBand minimapToolsBand = new JRibbonBand(trans.get("Minimap"), getResizableIconFromFile("graphics/icons/minimap.png"));

        // <editor-fold defaultstate="collapsed" desc="baseToolsBand setup">
        JCommandButton noToolButton = factoryButton(trans.get("Werkzeugabwaehlen"), "graphics/cursors/default.png", trans.get("Deaktiviert_Karte"), trans.get("Deaktiviert_Text"), true);
        noToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DEFAULT);
            }
        });
        baseToolsBand.addCommandButton(noToolButton, RibbonElementPriority.TOP);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="attackToolsBand setup">
        JCommandButton attackRamToolButton = factoryButton(null, "graphics/big/ram.png", trans.get("Erstellt_Ram"), 
                trans.get("Erstellt_Ram_Text"), true);
        attackRamToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_RAM);
            }
        });
        JCommandButton attackSnobToolButton = factoryButton(null, "graphics/big/snob.png", trans.get("Erstellt_AG"), 
                trans.get("Erstellt_AG_Text"), true);
        attackSnobToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SNOB);
            }
        });
        JCommandButton attackSpyToolButton = factoryButton(null, "graphics/big/spy.png", trans.get("Erstellt_Spaeh"), 
                trans.get("Erstellt_Spaeh_Text"), true);
        attackSpyToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SPY);
            }
        });
        JCommandButton attackAxeToolButton = factoryButton(null, "graphics/big/axe.png", trans.get("Erstellt_Axtlaufzeit"), 
                trans.get("Erstellt_Axtlaufzeit_Text"), true);
        attackAxeToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_AXE);
            }
        });
        JCommandButton attackLightToolButton = factoryButton(null, "graphics/big/light.png", trans.get("Erstellt_LKav"), 
                trans.get("Erstellt_LKav_Text"), true);
        attackLightToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_LIGHT);
            }
        });
        JCommandButton attackHeavyToolButton = factoryButton(null, "graphics/big/heavy.png", trans.get("Erstellt_SKav"), 
                trans.get("Erstellt_SKav_Text"), true);
        attackHeavyToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_HEAVY);
            }
        });
        JCommandButton attackSwordToolButton = factoryButton(null, "graphics/big/sword.png", trans.get("Erstellt_Schwert"), 
                trans.get("Erstellt_Schwert_Text"), true);
        attackSwordToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ATTACK_SWORD);
            }
        });
        attackToolsBand.addCommandButton(attackRamToolButton, RibbonElementPriority.TOP);
        attackToolsBand.addCommandButton(attackSnobToolButton, RibbonElementPriority.TOP);
        attackToolsBand.addCommandButton(attackSpyToolButton, RibbonElementPriority.MEDIUM);
        attackToolsBand.addCommandButton(attackAxeToolButton, RibbonElementPriority.MEDIUM);
        attackToolsBand.addCommandButton(attackLightToolButton, RibbonElementPriority.LOW);
        attackToolsBand.addCommandButton(attackHeavyToolButton, RibbonElementPriority.LOW);
        attackToolsBand.addCommandButton(attackSwordToolButton, RibbonElementPriority.LOW);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="defendToolsBand setup">
        JCommandButton supportToolButton = factoryButton(trans.get("Unterstuetzungenbestimmen"), "graphics/big/support.png", 
                trans.get("Unterstuetzungenbestimmen_open"), 
                trans.get("Unterstuetzungenbestimmen_text"), true);
        supportToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SUPPORT);
            }
        });
        defendToolsBand.addCommandButton(supportToolButton, RibbonElementPriority.LOW);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="infoToolBand setup">
        JCommandButton selectToolButton = factoryButton(trans.get("Hauptkarte"), "graphics/big/selection.png", 
                trans.get("Hauptkarte_open"), 
                trans.get("Hauptkarte_text"), true);
        selectToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SELECTION);
            }
        });
        JCommandButton markToolButton = factoryButton(trans.get("Spielermarkieren"), "graphics/big/brush3.png", trans.get("SpielerHauptkarte"), 
                trans.get("SpielerHauptkarte_text"), true);
        markToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MARK);
            }
        });
        JCommandButton noteToolButton = factoryButton(trans.get("Notizerstellen"), "graphics/big/notebook_add.png", 
                trans.get("Notiz_open"), 
                trans.get("Notiz_text"), true);
        noteToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_NOTE);
            }
        });
        JCommandButton distanceToolButton = factoryButton(trans.get("Laufzeitbestimmen"), "graphics/big/tape_measure1.png", 
                trans.get("Laufzeitbestimmen_open"), 
                        trans.get("Laufzeitbestimmen_text"), true);
        distanceToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MEASURE);
            }
        });
        JCommandButton radarToolButton = factoryButton(trans.get("Laufzeitradar"), "graphics/icons/radar.png", 
                trans.get("Laufzeitradar_open"), 
                        trans.get("Laufzeitradar_text"), true);
        radarToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_RADAR);
            }
        });
        JCommandButton tagToolButton = factoryButton(trans.get("Gruppezuweisen"), "graphics/icons/tag.png", 
                trans.get("Gruppezuweisen_open"), 
                        trans.get("Gruppezuweisen_text"), true);
        tagToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_TAG);
            }
        });

        // <editor-fold defaultstate="collapsed" desc="church button setup">
        JCommandButton createChurchToolButton = factoryButton(null, "graphics/big/Church1.png", trans.get("KircheDorf"), trans.get("KircheDorfWerkzeug"), true);
        JCommandButton createChurch1ToolButton = factoryButton(null, "graphics/big/Church1.png", trans.get("KircheStufeerstellen"), trans.get("KircheStufeerstellen_tool"), true);
        createChurch1ToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_1);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo(trans.get("Kirchenwelten"));
                }
            }
        });
        JCommandButton createChurch2ToolButton = factoryButton(null, "graphics/big/Church2.png", trans.get("KircheStufeerstellenzwei_tool"), trans.get("KircheStufeerstellenzwei_Werkzeug"), true);
        createChurch2ToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_2);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo(trans.get("Kirchenwelten"));
                }
            }
        });
        JCommandButton createChurch3ToolButton = factoryButton(null, "graphics/big/Church3.png", trans.get("KircheStufeerstellendrei_tool"), trans.get("KircheStufeerstellendrei_Werkzeug"), true);
        createChurch3ToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_CHURCH_3);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo(trans.get("Kirchenwelten"));
                }
            }
        });
        JCommandButton removeChurchToolButton = factoryButton(null, "graphics/big/NoChurch.png", trans.get("Kircheloeschen"), trans.get("Kircheloeschen_text"), true);
        removeChurchToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_REMOVE_CHURCH);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo(trans.get("Kirchenwelten"));
                }
            }
        });

        JCommandButtonPanel cbpChurch = new JCommandButtonPanel(CommandButtonDisplayState.FIT_TO_ICON);
        cbpChurch.setLayoutKind(JCommandButtonPanel.LayoutKind.ROW_FILL);
        cbpChurch.addButtonGroup(trans.get("Stufe"));
        cbpChurch.addButtonToLastGroup(createChurch1ToolButton);
        cbpChurch.addButtonToLastGroup(createChurch2ToolButton);
        cbpChurch.addButtonToLastGroup(createChurch3ToolButton);
        cbpChurch.addButtonToLastGroup(removeChurchToolButton);
        final JCommandPopupMenu popupMenuChurch = new JCommandPopupMenu(
                cbpChurch, 1, 4);

        createChurchToolButton.setPopupCallback(new PopupPanelCallback() {
            @Override
            public JPopupPanel getPopupPanel(JCommandButton commandButton) {
                return popupMenuChurch;
            }
        });
        createChurchToolButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="watchtower button setup">
        JCommandButton createWatchtowerToolButton = factoryButton(null, "graphics/big/Watchtower1.png", trans.get("WachturmDorfWerkzeug"), trans.get("Wachturmaktiv"), true);
        JCommandButton createWatchtower1ToolButton = factoryButton(null, "graphics/big/Watchtower1.png", trans.get("WachturmStufeerstellen"), trans.get("WachturmStufeerstellen_tool"), true);
        createWatchtower1ToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isWatchtower()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_WATCHTOWER_1);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo(trans.get("VerfuegbarWachturm"));
                }
            }
        });
        JCommandButton createWatchtowerInToolButton = factoryButton(null, "graphics/big/WatchtowerIn.png", trans.get("Wachturmerstellen"), trans.get("Wachturm_text"), true);
        createWatchtowerInToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isWatchtower()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_WATCHTOWER_INPUT);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo(trans.get("VerfuegbarWachturm"));
                }
            }
        });
        JCommandButton removeWatchtowerToolButton = factoryButton(null, "graphics/big/NoWatchtower.png", trans.get("Wachturmlöschen"), trans.get("Wachturmlöschen_text"), true);
        removeWatchtowerToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isWatchtower()) {
                    MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_REMOVE_WATCHTOWER);
                } else {
                    DSWorkbenchMainFrame.getSingleton().showInfo(trans.get("VerfuegbarWachturm"));
                }
            }
        });

        JCommandButtonPanel wbp = new JCommandButtonPanel(CommandButtonDisplayState.FIT_TO_ICON);
        wbp.setLayoutKind(JCommandButtonPanel.LayoutKind.ROW_FILL);
        wbp.addButtonGroup(trans.get("Stufe"));
        wbp.addButtonToLastGroup(createWatchtower1ToolButton);
        wbp.addButtonToLastGroup(createWatchtowerInToolButton);
        wbp.addButtonToLastGroup(removeWatchtowerToolButton);
        final JCommandPopupMenu watchtoerPopupMenu = new JCommandPopupMenu(
                wbp, 1, 3);

        createWatchtowerToolButton.setPopupCallback(new PopupPanelCallback() {
            @Override
            public JPopupPanel getPopupPanel(JCommandButton commandButton) {
                return watchtoerPopupMenu;
            }
        });
        createWatchtowerToolButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
        // </editor-fold>

        infoToolBand.addCommandButton(selectToolButton, RibbonElementPriority.TOP);
        infoToolBand.addCommandButton(markToolButton, RibbonElementPriority.TOP);
        infoToolBand.addCommandButton(noteToolButton, RibbonElementPriority.TOP);
        infoToolBand.addCommandButton(distanceToolButton, RibbonElementPriority.MEDIUM);
        infoToolBand.addCommandButton(radarToolButton, RibbonElementPriority.MEDIUM);
        infoToolBand.addCommandButton(tagToolButton, RibbonElementPriority.LOW);
        infoToolBand.addCommandButton(createChurchToolButton, RibbonElementPriority.LOW);
        infoToolBand.addCommandButton(createWatchtowerToolButton, RibbonElementPriority.LOW);

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="drawToolsBand setup">
        JCommandButton drawRectToolButton = factoryButton(trans.get("Rechteckzeichnen"), "graphics/icons/draw_rect.png", trans.get("Rechteckzeichnen_text"), null, true);
        drawRectToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_RECT);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Rectangle.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawCircleToolButton = factoryButton(trans.get("Kreiszeichnen"), "graphics/icons/draw_circle.png", trans.get("Kreiszeichnen_text"), null, true);
        drawCircleToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_CIRCLE);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Circle.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawLineToolButton = factoryButton(trans.get("Liniezeichnen"), "graphics/icons/draw_line.png", trans.get("Liniezeichnen_text"), null, true);
        drawLineToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_LINE);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Line.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawArrowToolButton = factoryButton(trans.get("Pfeilzeichnen"), "graphics/icons/draw_arrow.png", trans.get("Pfeilzeichnen_text"), null, true);
        drawArrowToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_ARROW);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Arrow.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawFreehandToolButton = factoryButton(trans.get("Freihandzeichnen"), "graphics/icons/draw_freeform.png", trans.get("Freihandzeichnen_text"), null, true);
        drawFreehandToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_FREEFORM);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.FreeForm.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton drawTextToolButton = factoryButton(trans.get("Textzeichnen"), "graphics/icons/draw_text.png", trans.get("Textzeichnen_text"), null, true);
        drawTextToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        MapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_DRAW_TEXT);
                        FormConfigFrame.getSingleton().setupAndShow(de.tor.tribes.types.drawing.Text.class);
                        FormConfigFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        drawToolsBand.addCommandButton(drawRectToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawCircleToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawLineToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawArrowToolButton, RibbonElementPriority.MEDIUM);
        drawToolsBand.addCommandButton(drawFreehandToolButton, RibbonElementPriority.LOW);
        drawToolsBand.addCommandButton(drawTextToolButton, RibbonElementPriority.LOW);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="minimapToolsBand setup">
        JCommandButton minimapMoveToolButton = factoryButton(trans.get("Ausschnittbewegen"), "graphics/icons/move.png", trans.get("Ausschnittbewegen_text"), trans.get("Ausschnittbewegen_tool"), true);
        minimapMoveToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_MOVE);
            }
        });
        JCommandButton minimapZoomToolButton = factoryButton(trans.get("Minimapvergoessern"), "graphics/big/view.png", trans.get("Minimapvergoessern_text"), trans.get("Minimapvergoessern_tool"), true);
        minimapZoomToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_ZOOM);
            }
        });
        JCommandButton minimapShotToolButton = factoryButton(trans.get("Screenshoterstellen"), "graphics/big/camera.png", trans.get("Screenshoterstellen_text"), trans.get("Screenshoterstellen_tool"), true);
        minimapShotToolButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MinimapPanel.getSingleton().setCurrentCursor(ImageManager.CURSOR_SHOT);
            }
        });
        minimapToolsBand.addCommandButton(minimapMoveToolButton, RibbonElementPriority.TOP);
        minimapToolsBand.addCommandButton(minimapZoomToolButton, RibbonElementPriority.LOW);
        minimapToolsBand.addCommandButton(minimapShotToolButton, RibbonElementPriority.LOW);
        // </editor-fold>

        baseToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(baseToolsBand.getControlPanel())));

        attackToolsBand.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesRestrictive(attackToolsBand));

        drawToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(drawToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(drawToolsBand.getControlPanel())));

        minimapToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(minimapToolsBand.getControlPanel()),
                new IconRibbonBandResizePolicy(minimapToolsBand.getControlPanel())));

        defendToolsBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(defendToolsBand.getControlPanel())));

        infoToolBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(infoToolBand.getControlPanel()),
                new IconRibbonBandResizePolicy(infoToolBand.getControlPanel())));

        RibbonTask task1 = new RibbonTask(trans.get("Kartenwerkzeuge"), baseToolsBand, attackToolsBand, defendToolsBand, infoToolBand, drawToolsBand, minimapToolsBand);
        frame.getRibbon().addTask(task1);
    }

    public static void addViewTask(JRibbonFrame frame) {
        JRibbonBand attackViewBand = new JRibbonBand(trans.get("Angriff"), getResizableIconFromFile("graphics/big/axe.png"));
        JRibbonBand ingameInfoViewBand = new JRibbonBand(trans.get("ImportierteDaten"), getResizableIconFromFile("graphics/big/clipboard_next.png"));
        JRibbonBand infoViewBand = new JRibbonBand(trans.get("Informationen"), getResizableIconFromFile("graphics/big/information.png"));

        // <editor-fold defaultstate="collapsed" desc="attackViewBand setup">
        JCommandButton attackViewButton = factoryButton(trans.get("Befehle"), "graphics/big/axe_sword.png", trans.get("Befehle_open"),
                trans.get("Befehle_text"), true);
        attackViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchAttackFrame.getSingleton().setVisible(true);
                        DSWorkbenchAttackFrame.getSingleton().requestFocus();
                    }
                });
            }
        });

        JCommandButton markerViewButton = factoryButton(trans.get("Markierungen"), "graphics/icons/mark.png", 
                trans.get("Markierungen_open"), 
                trans.get("Markierungen_text"), true);
        markerViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchMarkerFrame.getSingleton().setVisible(true);
                        DSWorkbenchMarkerFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton formsViewButton = factoryButton(trans.get("Zeichnungen"), "graphics/big/palette2.png", 
                trans.get("Zeichnungen_open"), 
                trans.get("Zeichnungen_text"), true);
        formsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchFormFrame.getSingleton().setVisible(true);
                        DSWorkbenchFormFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton churchViewButton = factoryButton(trans.get("Kirchen"), "graphics/big/Church1.png", trans.get("Kirchen_open"), trans.get("Kirchen_text"), true);
        churchViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isChurch()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            DSWorkbenchChurchFrame.getSingleton().setVisible(true);
                            DSWorkbenchChurchFrame.getSingleton().requestFocus();
                        }
                    });
                } else {
                    JOptionPaneHelper.showInformationBox(DSWorkbenchMainFrame.getSingleton(), trans.get("KirchennichtAktiv"), trans.get("Information"));
                }
            }
        });
        JCommandButton watchtowerViewButton = factoryButton(trans.get("Wachtuerm"), "graphics/big/Watchtower1.png", trans.get("Wachtuerm_open"), trans.get("Wachtuerm_text"), true);
        watchtowerViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ServerSettings.getSingleton().isWatchtower()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            DSWorkbenchWatchtowerFrame.getSingleton().setVisible(true);
                            DSWorkbenchWatchtowerFrame.getSingleton().requestFocus();
                        }
                    });
                } else {
                    JOptionPaneHelper.showInformationBox(DSWorkbenchMainFrame.getSingleton(), trans.get("Wachtuermnichtaktiv"), "Information");
                }
            }
        });
        JCommandButton villageViewButton = factoryButton(trans.get("Doerfer"), "graphics/big/village.png", 
                trans.get("Doerfer_open"), 
                trans.get("Doerfer_text"), true);
        villageViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        KnownVillageManager.getSingleton().cleanEmptyVillages();
                        DSWorkbenchKnownVillageFrame.getSingleton().setVisible(true);
                        DSWorkbenchKnownVillageFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        //   attackViewBand.startGroup();
        attackViewBand.addCommandButton(attackViewButton, RibbonElementPriority.TOP);
        attackViewBand.addCommandButton(markerViewButton, RibbonElementPriority.MEDIUM);
        attackViewBand.addCommandButton(formsViewButton, RibbonElementPriority.MEDIUM);
        attackViewBand.addCommandButton(churchViewButton, RibbonElementPriority.MEDIUM);
        attackViewBand.addCommandButton(watchtowerViewButton, RibbonElementPriority.MEDIUM);
        attackViewBand.addCommandButton(villageViewButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="ingameInfoViewBand setup">
        JCommandButton tagsViewButton = factoryButton(trans.get("Gruppen"), "graphics/icons/tag.png", 
                trans.get("Gruppen_open"), 
                trans.get("Gruppen_text"), true);
        tagsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchTagFrame.getSingleton().setVisible(true);
                        DSWorkbenchTagFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton troopsViewButton = factoryButton(trans.get("Truppen"), "graphics/big/troops.png", 
                trans.get("Truppen_open"), 
                trans.get("Truppen_text"), true);
        troopsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchTroopsFrame.getSingleton().setVisible(true);
                        DSWorkbenchTroopsFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton reportsViewButton = factoryButton(trans.get("Berichte"), "graphics/big/report.png", 
                trans.get("Berichte_open"), 
                trans.get("Berichte_text"), true);
        reportsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchReportFrame.getSingleton().setVisible(true);
                        DSWorkbenchReportFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        ingameInfoViewBand.addCommandButton(tagsViewButton, RibbonElementPriority.MEDIUM);
        ingameInfoViewBand.addCommandButton(troopsViewButton, RibbonElementPriority.MEDIUM);
        ingameInfoViewBand.addCommandButton(reportsViewButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="infoViewBand setup">
        JCommandButton notesViewButton = factoryButton(trans.get("Notizblock"), "graphics/big/notebook.png", 
                trans.get("Notizblock_open"), 
                trans.get("Notizblock_text"), true);
        notesViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchNotepad.getSingleton().setVisible(true);
                        DSWorkbenchNotepad.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton conquerViewButton = factoryButton(trans.get("Eroberungen"), "graphics/big/snob.png", 
                trans.get("Eroberungen_open"), 
                trans.get("Eroberungen_text"), true);
        conquerViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchConquersFrame.getSingleton().setVisible(true);
                        DSWorkbenchConquersFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton rankViewButton = factoryButton(trans.get("Ranglisten"), "graphics/big/medal.png", 
                trans.get("Ranglisten_open"), 
                trans.get("Ranglisten_text"), true);
        rankViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchRankFrame.getSingleton().setVisible(true);
                        DSWorkbenchRankFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        JCommandButton statsViewButton = factoryButton(trans.get("Statistiken"), "graphics/icons/ally_chart.png", 
                trans.get("Statistiken_open"), 
                trans.get("Statistiken_text"), true);
        statsViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        DSWorkbenchStatsFrame.getSingleton().setVisible(true);
                        DSWorkbenchStatsFrame.getSingleton().requestFocus();
                    }
                });
            }
        });
        infoViewBand.addCommandButton(notesViewButton, RibbonElementPriority.TOP);
        infoViewBand.addCommandButton(conquerViewButton, RibbonElementPriority.MEDIUM);
        infoViewBand.addCommandButton(rankViewButton, RibbonElementPriority.MEDIUM);
        infoViewBand.addCommandButton(statsViewButton, RibbonElementPriority.MEDIUM);
        // </editor-fold>

        attackViewBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(attackViewBand.getControlPanel()),
                // new CoreRibbonResizePolicies.Mid2Low(attackViewBand.getControlPanel()),
                new IconRibbonBandResizePolicy(attackViewBand.getControlPanel())));
        ingameInfoViewBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(ingameInfoViewBand.getControlPanel()),
                // new CoreRibbonResizePolicies.Mirror(ingameInfoViewBand.getControlPanel()),
                // new CoreRibbonResizePolicies.High2Low(ingameInfoViewBand.getControlPanel()),
                new IconRibbonBandResizePolicy(ingameInfoViewBand.getControlPanel())));
        infoViewBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.Mirror(infoViewBand.getControlPanel()),
                // new CoreRibbonResizePolicies.Mid2Low(infoViewBand.getControlPanel()),
                new IconRibbonBandResizePolicy(infoViewBand.getControlPanel())));
        RibbonTask task2 = new RibbonTask(trans.get("Ansicht"), attackViewBand, ingameInfoViewBand, infoViewBand);

        //  frame.getRibbon().addTask(task1);
        frame.getRibbon().addTask(task2);
    }

    public static void addMiscTask(JRibbonFrame frame) {
        JRibbonBand miscBand = new JRibbonBand(trans.get("Sonstiges"), getResizableIconFromResource("/res/128x128/help.png"));

        // <editor-fold defaultstate="collapsed" desc="miscBand setup">
        JCommandButton helpButton = factoryButton(trans.get("Hilfe"), "/res/128x128/help.png", 
                trans.get("Hilfe_open"), 
                trans.get("Hilfe_text"), true);
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalOptions.getHelpBroker().setDisplayed(true);
            }
        });
        JCommandButton facebookButton = factoryButton(trans.get("Facebook"), "/res/128x128/facebook.png", 
                trans.get("Facebook_open"), 
                trans.get("Facebook_text"), true);
        facebookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserInterface.openPage("http://www.facebook.com/pages/DS-Workbench/182068775185568");
            }
        });
        JCommandButton donateButton = factoryButton(trans.get("Spenden"), "/res/ui/paypal.gif", 
                trans.get("Spenden_open"), 
                trans.get("Spenden_text"), true);
        donateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserInterface.openPage("https://www.paypal.com/paypalme/mecqq");
            }
        });
        JCommandButton aboutButton = factoryButton(trans.get("About"), "/res/ui/about.png", 
                trans.get("About_open"), 
                trans.get("About_text"), true);
        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DSWorkbenchMainFrame.getSingleton().showAboutDialog();
            }
        });
        miscBand.addCommandButton(helpButton, RibbonElementPriority.TOP);
        miscBand.addCommandButton(facebookButton, RibbonElementPriority.TOP);
        miscBand.addCommandButton(donateButton, RibbonElementPriority.TOP);
        miscBand.addCommandButton(aboutButton, RibbonElementPriority.TOP);
        // </editor-fold>

        miscBand.setResizePolicies((List) Arrays.asList(
                new CoreRibbonResizePolicies.None(miscBand.getControlPanel()),
                new IconRibbonBandResizePolicy(miscBand.getControlPanel())
        ));
        RibbonTask task1 = new RibbonTask(trans.get("Sonstiges"), miscBand);
        frame.getRibbon().addTask(task1);
    }

    private static JCommandButton factoryButton(String pLabel, String pIconPath, String pTooltipText, String pSecondaryTooltipText, boolean pShowLabel) {
        JCommandButton button = null;

        if (!new File(pIconPath).exists()) {
            button = new JCommandButton((pShowLabel) ? pLabel : null, getResizableIconFromResource(pIconPath));
        } else {
            button = new JCommandButton((pShowLabel) ? pLabel : null, getResizableIconFromFile(pIconPath));
        }

        if (pTooltipText != null) {
            RichTooltip rt = new RichTooltip((pLabel != null) ? pLabel : trans.get("Info"), pTooltipText);
            if (pSecondaryTooltipText != null) {
                rt.addDescriptionSection(pSecondaryTooltipText);
            }
            if (new File(pIconPath).exists()) {
                try {
                    rt.setMainImage(ImageIO.read(new File(pIconPath)));
                } catch (Exception ignored) {
                }
            } else {
                try {
                    rt.setMainImage(ImageIO.read(RibbonConfigurator.class.getResource(pIconPath)));
                } catch (Exception ignored) {
                }
            }
            button.setActionRichTooltip(rt);
        }
        return button;
    }

    private static ResizableIcon getResizableIconFromResource(String resource) {
        try {
            return ImageWrapperResizableIcon.getIcon(DSWorkbenchMainFrame.class.getResource(resource), new Dimension(48, 48));
        } catch (Exception e) {
            return new EmptyResizableIcon(18);
        }
    }

    private static ResizableIcon getResizableIconFromFile(String resource) {
        try {
            return ImageWrapperResizableIcon.getIcon(new File(resource).toURI().toURL(), new Dimension(48, 48));
        } catch (Exception e) {
            return new EmptyResizableIcon(18);
        }
    }
}
