/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.util;

import de.tor.tribes.dssim.types.UnitHolder;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 *
 * @author Charon
 */
public class ImageManager {

    public final static int ICON_SPEAR = 0;
    public final static int ICON_SWORD = 1;
    public final static int ICON_AXE = 2;
    public final static int ICON_ARCHER = 3;
    public final static int ICON_SPY = 4;
    public final static int ICON_LKAV = 5;
    public final static int ICON_MARCHER = 6;
    public final static int ICON_HEAVY = 7;
    public final static int ICON_RAM = 8;
    public final static int ICON_CATA = 9;
    public final static int ICON_KNIGHT = 10;
    public final static int ICON_SNOB = 11;
    public final static int ICON_MILITIA = 12;
    private static final List<ImageIcon> UNIT_ICONS = new LinkedList<>();

    /**Load the icons of the units used for the animated unit movement on the MapPanel*/
    public static void loadUnitIcons() throws Exception {
        try {
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/spear.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/sword.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/axe.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/archer.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/spy.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/light.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/marcher.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/heavy.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/ram.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/cata.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/knight.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/snob.png")));
            UNIT_ICONS.add(new ImageIcon(ImageManager.class.getResource("/res/icons/militia.png")));
        } catch (Exception e) {
            throw new Exception("Failed to load unit icons");
        }
    }

    public static ImageIcon getUnitIcon(UnitHolder pUnit) {
        if (pUnit == null) {
            return null;
        }
        if (pUnit.getPlainName().equals("spear")) {
            return UNIT_ICONS.get(ICON_SPEAR);
        } else if (pUnit.getPlainName().equals("sword")) {
            return UNIT_ICONS.get(ICON_SWORD);
        } else if (pUnit.getPlainName().equals("axe")) {
            return UNIT_ICONS.get(ICON_AXE);
        } else if (pUnit.getPlainName().equals("archer")) {
            return UNIT_ICONS.get(ICON_ARCHER);
        } else if (pUnit.getPlainName().equals("spy")) {
            return UNIT_ICONS.get(ICON_SPY);
        } else if (pUnit.getPlainName().equals("light")) {
            return UNIT_ICONS.get(ICON_LKAV);
        } else if (pUnit.getPlainName().equals("marcher")) {
            return UNIT_ICONS.get(ICON_MARCHER);
        } else if (pUnit.getPlainName().equals("heavy")) {
            return UNIT_ICONS.get(ICON_HEAVY);
        } else if (pUnit.getPlainName().equals("ram")) {
            return UNIT_ICONS.get(ICON_RAM);
        } else if (pUnit.getPlainName().equals("catapult")) {
            return UNIT_ICONS.get(ICON_CATA);
        } else if (pUnit.getPlainName().equals("snob")) {
            return UNIT_ICONS.get(ICON_SNOB);
        } else if (pUnit.getPlainName().equals("knight")) {
            return UNIT_ICONS.get(ICON_KNIGHT);
        } else if (pUnit.getPlainName().equals("militia")) {
            return UNIT_ICONS.get(ICON_MILITIA);
        }
        //unknown unit
        return null;
    }
}
