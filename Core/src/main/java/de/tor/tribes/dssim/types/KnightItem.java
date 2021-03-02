/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.types;

import de.tor.tribes.dssim.util.ConfigManager;
import java.util.HashMap;

/**
 *
 * @author Jejkal
 */
public class KnightItem {

    public static final int ID_NO_ITEM = 0;
    public static final int ID_SPEAR = 1;
    public static final int ID_SWORD = 2;
    public static final int ID_AXE = 3;
    public static final int ID_ARCHER = 4;
    public static final int ID_SPY = 5;
    public static final int ID_LIGHT = 6;
    public static final int ID_MARCHER = 7;
    public static final int ID_HEAVY = 8;
    public static final int ID_RAM = 9;
    public static final int ID_CATA = 10;
    public static final int ID_SNOB = 11;
    private int itemID = ID_SPEAR;
    private static HashMap<Integer, String> nameMappings = new HashMap<>();


    static {
        nameMappings.put(ID_NO_ITEM, "Kein Gegenstand");
        nameMappings.put(ID_SPEAR, "Eidgenössische Hellebarde");
        nameMappings.put(ID_SWORD, "Ullrichs Langschwert");
        nameMappings.put(ID_AXE, "Thorgards Kriegsaxt");
        nameMappings.put(ID_ARCHER, "Edwards Langbogen");
        nameMappings.put(ID_SPY, "Kalids Fernrohr");
        nameMappings.put(ID_LIGHT, "Mieszkos Lanze");
        nameMappings.put(ID_MARCHER, "Kompositbogen des Khan");
        nameMappings.put(ID_HEAVY, "Baptistes Banner");
        nameMappings.put(ID_RAM, "Carols Morgenstern");
        nameMappings.put(ID_CATA, "Aletheias Leuchtfeuer");
        nameMappings.put(ID_SNOB, "Vascos Zepter");
    }

    public static KnightItem factoryKnightItem(int pId) {
        return new KnightItem(pId);
    }

    KnightItem(int pId) {
        itemID = pId;
    }

    public int getItemId() {
        return itemID;
    }

    public String getItemName() {
        return nameMappings.get(itemID);
    }

    public boolean affectsUnit(UnitHolder pUnit) {
        switch (itemID) {
            case ID_SPEAR: {
                if (pUnit.getPlainName().equals("spear")) {
                    return true;
                }
                return false;
            }
            case ID_SWORD: {
                if (pUnit.getPlainName().equals("sword")) {
                    return true;
                }
                return false;
            }
            case ID_AXE: {
                if (pUnit.getPlainName().equals("axe")) {
                    return true;
                }
                return false;
            }
            case ID_ARCHER: {
                if (pUnit.getPlainName().equals("archer")) {
                    return true;
                }
                return false;
            }
            case ID_SPY: {
                if (pUnit.getPlainName().equals("spy")) {
                    return true;
                }
                return false;
            }
            case ID_LIGHT: {
                if (pUnit.getPlainName().equals("light")) {
                    return true;
                }
                return false;
            }
            case ID_MARCHER: {
                if (pUnit.getPlainName().equals("marcher")) {
                    return true;
                }
                return false;
            }
            case ID_HEAVY: {
                if (pUnit.getPlainName().equals("heavy")) {
                    return true;
                }
                return false;
            }
            case ID_RAM: {
                if (pUnit.getPlainName().equals("ram")) {
                    return true;
                }
                return false;
            }
            case ID_CATA: {
                if (pUnit.getPlainName().equals("catapult")) {
                    return true;
                }
                return false;
            }
            case ID_SNOB: {
                if (pUnit.getPlainName().equals("snob")) {
                    return true;
                }
                return false;
            }
            default: {
                //no_item
                return false;
            }
        }
    }

    public double getOffFactor() {
        switch (itemID) {
            case ID_NO_ITEM: {
                return 1.0;
            }
            case ID_SWORD: {
                if (ConfigManager.getSingleton().getKnightNewItems() == 0) {
                    return 1.3;
                } else {
                    return 1.4;
                }
            }
            case ID_AXE: {
                if (ConfigManager.getSingleton().getKnightNewItems() == 0) {
                    return 1.3;
                } else {
                    return 1.4;
                }
            }
            case ID_SPY: {
                if (ConfigManager.getSingleton().getKnightNewItems() == 0) {
                    return 0;
                } else {
                    return 0;
                }
            }
            case ID_RAM: {
                //strength of ram does not change, only the damage
                return 1.0;
            }
            case ID_CATA: {
                 //strength of cata does not change, only the damage
                return 1.0;
            }

            default: {
                //snob, heavy, marcher, light, archer, spear
                return 1.3;
            }
        }
    }

    public double getDefFactor() {
        switch (itemID) {
            case ID_NO_ITEM: {
                return 1.0;
            }
            case ID_SWORD: {
                if (ConfigManager.getSingleton().getKnightNewItems() == 0) {
                    return 1.2;
                } else {
                    return 1.3;
                }
            }
            case ID_AXE: {
                if (ConfigManager.getSingleton().getKnightNewItems() == 0) {
                    return 1.2;
                } else {
                    return 1.3;
                }
            }
            case ID_SPY: {
                return 1;
            }
            case ID_RAM: {
                return 1;
            }
            case ID_CATA: {
                return 10;
            }
            default: {
                //snob, heavy, marcher, light, archer, spear
                return 1.2;
            }
        }
    }

    public String toString() {
        return getItemName();
    }
}
