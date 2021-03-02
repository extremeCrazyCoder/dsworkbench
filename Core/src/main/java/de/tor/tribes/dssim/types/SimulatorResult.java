/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.types;

import de.tor.tribes.dssim.util.UnitManager;
import java.util.HashMap;

/**
 *
 * @author Jejkal
 */
public class SimulatorResult {

    private boolean win = false;
    private int nukes = 1;
    private HashMap<UnitHolder, AbstractUnitElement> offBefore = null;
    private HashMap<UnitHolder, AbstractUnitElement> defBefore = null;
    private HashMap<UnitHolder, AbstractUnitElement> survivingOff = null;
    private HashMap<UnitHolder, AbstractUnitElement> survivingDef = null;
    private int wallLevel = 0;
    private int buildingLevel = 0;
    private int wallBefore = 0;
    private int buildingBefore = 0;
    private boolean cataAtWall = false;

    public SimulatorResult() {
        survivingOff = new HashMap<>();
        survivingDef = new HashMap<>();
        for (UnitHolder unit : UnitManager.getSingleton().getUnits()) {
            AbstractUnitElement copy = new AbstractUnitElement(unit, 0, 1);
            survivingOff.put(unit, copy);
            copy = new AbstractUnitElement(unit, 0, 1);
            survivingDef.put(unit, copy);
        }
    }

    public SimulatorResult(HashMap<UnitHolder, AbstractUnitElement> pOff, HashMap<UnitHolder, AbstractUnitElement> pDef) {
        survivingOff = new HashMap<>();
        survivingDef = new HashMap<>();
        for (UnitHolder unit : UnitManager.getSingleton().getUnits()) {
            AbstractUnitElement element = pOff.get(unit);
            AbstractUnitElement copy = new AbstractUnitElement(unit, element.getCount(), element.getTech());
            survivingOff.put(unit, copy);
            element = pDef.get(unit);
            copy = new AbstractUnitElement(unit, element.getCount(), element.getTech());
            survivingDef.put(unit, copy);
        }
    }

    public void setOffBefore(HashMap<UnitHolder, AbstractUnitElement> pOff) {
        offBefore = pOff;
    }

    public void setDefBefore(HashMap<UnitHolder, AbstractUnitElement> pDef) {
        defBefore = pDef;
    }

    public HashMap<UnitHolder, AbstractUnitElement> getOffBefore() {
        return offBefore;
    }

    public HashMap<UnitHolder, AbstractUnitElement> getDefBefore() {
        return defBefore;
    }

    public void setNukes(int pNukes) {
        nukes = pNukes;
    }

    public int getNukes() {
        return nukes;
    }

    /**
     * @return the win
     */
    public boolean isWin() {
        return win;
    }

    /**
     * @param win the win to set
     */
    public void setWin(boolean win) {
        this.win = win;
    }

    /**
     * @return the survivingOff
     */
    public HashMap<UnitHolder, AbstractUnitElement> getSurvivingOff() {
        return survivingOff;
    }

    /**
     * @param survivingOff the survivingOff to set
     */
    public void setSurvivingOff(HashMap<UnitHolder, AbstractUnitElement> survivingOff) {
        this.survivingOff = survivingOff;
    }

    /**
     * @return the survivingDef
     */
    public HashMap<UnitHolder, AbstractUnitElement> getSurvivingDef() {
        return survivingDef;
    }

    /**
     * @param survivingDef the survivingDef to set
     */
    public void setSurvivingDef(HashMap<UnitHolder, AbstractUnitElement> survivingDef) {
        this.survivingDef = survivingDef;
    }

    /**
     * @return the wallLevel
     */
    public int getWallLevel() {
        return wallLevel;
    }

    /**
     * @param wallLevel the wallLevel to set
     */
    public void setWallLevel(int wallLevel) {
        this.wallLevel = wallLevel;
    }

    /**
     * @return the buildingLevel
     */
    public int getBuildingLevel() {
        return buildingLevel;
    }

    /**
     * @param buildingLevel the buildingLevel to set
     */
    public void setBuildingLevel(int buildingLevel) {
        this.buildingLevel = buildingLevel;
    }

    /**
     * @return the wallBefore
     */
    public int getWallBefore() {
        return wallBefore;
    }

    /**
     * @param wallBefore the wallBefore to set
     */
    public void setWallBefore(int wallBefore) {
        this.wallBefore = wallBefore;
    }

    /**
     * @return the buildingBefore
     */
    public int getBuildingBefore() {
        return buildingBefore;
    }

    /**
     * @param buildingBefore the buildingBefore to set
     */
    public void setBuildingBefore(int buildingBefore) {
        this.buildingBefore = buildingBefore;
    }

    /**
     * @return the cataAtWall
     */
    public boolean isCataAtWall() {
        return cataAtWall;
    }

    /**
     * @param cataAtWall the cataAtWall to set
     */
    public void setCataAtWall(boolean cataAtWall) {
        this.cataAtWall = cataAtWall;
    }
}
