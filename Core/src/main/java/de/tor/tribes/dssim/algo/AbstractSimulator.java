/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.algo;

import de.tor.tribes.dssim.types.AbstractUnitElement;
import de.tor.tribes.dssim.types.KnightItem;
import de.tor.tribes.dssim.types.SimulatorResult;
import de.tor.tribes.dssim.types.UnitHolder;
import de.tor.tribes.dssim.ui.DSWorkbenchSimulatorFrame;
import de.tor.tribes.dssim.util.ConfigManager;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Charon
 */
public abstract class AbstractSimulator {

    public final int ID_INFANTRY = 0;
    public final int ID_CAVALRY = 1;
    public final int ID_ARCHER = 2;
    private HashMap<UnitHolder, AbstractUnitElement> off = null;
    private HashMap<UnitHolder, AbstractUnitElement> def = null;
    private boolean nightBonus = false;
    private double luck = 0.0;
    private double moral = 100;
    private int wallLevel = 0;
    private int buildingLevel = 0;
    private int farmLevel = 0;
    private boolean attackerBelieve = false;
    private boolean defenderBelieve = false;
    private boolean cataChurch = false;
    private boolean cataFarm = false;
    private boolean cataWall = false;

    public abstract SimulatorResult calculate(HashMap<UnitHolder, AbstractUnitElement> pOff, HashMap<UnitHolder, AbstractUnitElement> pDef, KnightItem pOffItem, List<KnightItem> pDefItems, boolean pNightBonus, double pLuck, double pMoral, int pWallLevel, int pBuildingLevel, int pFarmLevel, boolean pAttackerBelieve, boolean pDefenderBelieve, boolean pCataChurch, boolean pCataFarm, boolean pCataWall);

    public SimulatorResult bunkerBuster(HashMap<UnitHolder, AbstractUnitElement> pOff, HashMap<UnitHolder, AbstractUnitElement> pDef, KnightItem pOffItem, List<KnightItem> pDefItems, boolean pNightBonus, double pLuck, double pMoral, int pWallLevel, int pBuildingLevel, int pFarmLevel, boolean pAttackerBelieve, boolean pDefenderBelieve, boolean pCataChurch, boolean pCataFarm, boolean pCataWall) {
        SimulatorResult result = calculate(pOff, pDef, pOffItem, pDefItems, pNightBonus, pLuck, pMoral, pWallLevel, pBuildingLevel, pFarmLevel, pAttackerBelieve, pDefenderBelieve, pCataChurch, pCataFarm, pCataWall);
        DSWorkbenchSimulatorFrame.getSingleton().addResultExternally(result);
        setFarmLevel(pFarmLevel);
        setCataChurch(pCataChurch);
        setCataFarm(pCataFarm);
        setCataWall(pCataWall);
       
        int cnt = 1;
        while (!result.isWin() && cnt <= 1000) {
            result = calculate(pOff, result.getSurvivingDef(), pOffItem, pDefItems, pNightBonus, pLuck, pMoral, result.getWallLevel(), result.getBuildingLevel(), pFarmLevel, pAttackerBelieve, pDefenderBelieve, pCataChurch, pCataFarm, pCataWall);
           /* if (pCataWall) {
                int wallDecrement = result.getBuildingBefore() - result.getBuildingLevel();
                result.setWallLevel(result.getWallBefore() - wallDecrement);
            }*/
            if (!result.isWin()) {
                DSWorkbenchSimulatorFrame.getSingleton().addResultExternally(result);
            }
            cnt++;
        }
        if (cnt > 1000) {
            result.setNukes(Integer.MAX_VALUE);
        } else {
            result.setNukes(cnt);
        }
        return result;
    }

    public boolean isInfantry(UnitHolder pUnit) {
        return !isCavalery(pUnit);
    }

    public boolean isCavalery(UnitHolder pUnit) {
        return (pUnit.getPlainName().equals("spy")
                || pUnit.getPlainName().equals("light")
                || pUnit.getPlainName().equals("marcher")
                || pUnit.getPlainName().equals("heavy")
                || pUnit.getPlainName().equals("knight"));
    }

    public boolean isArcher(UnitHolder pUnit) {
        return (pUnit.getPlainName().equals("archer")
                || pUnit.getPlainName().equals("marcher"));
    }

    public boolean isSpy(UnitHolder pUnit) {
        return (pUnit.getPlainName().equals("spy"));
    }

    public boolean isKnight(UnitHolder pUnit) {
        return (pUnit.getPlainName().equals("knight"));
    }

    public void setOff(HashMap<UnitHolder, AbstractUnitElement> pOff) {
        off = pOff;
    }

    public HashMap<UnitHolder, AbstractUnitElement> getOff() {
        return off;
    }

    public void setDef(HashMap<UnitHolder, AbstractUnitElement> pDef) {
        def = pDef;
    }

    public HashMap<UnitHolder, AbstractUnitElement> getDef() {
        return def;
    }

    /**
     * @return the nightBonus
     */
    public boolean isNightBonus() {
        return nightBonus;
    }

    /**
     * @param nightBonus the nightBonus to set
     */
    public void setNightBonus(boolean nightBonus) {
        this.nightBonus = nightBonus;
    }

    /**
     * @return the luck
     */
    public double getLuck() {
        return luck;
    }

    /**
     * @param luck the luck to set
     */
    public void setLuck(double luck) {
        this.luck = luck;
    }

    /**
     * @return the moral
     */
    public double getMoral() {
        return moral;
    }

    /**
     * @param moral the moral to set
     */
    public void setMoral(double moral) {
        this.moral = moral;
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
     * @return the farmLevel
     */
    public int getFarmLevel() {
        return farmLevel;
    }

    /**
     * @param farmLevel the farmLevel to set
     */
    public void setFarmLevel(int farmLevel) {
        this.farmLevel = farmLevel;
    }

    /**
     * @return the attackerBelieve
     */
    public boolean isAttackerBelieve() {
        if (ConfigManager.getSingleton().isChurch()) {
            return attackerBelieve;
        } else {
            //if no church is used take care that the believe factor would be 1 later
            return true;
        }
    }

    /**
     * @param attackerBelieve the attackerBelieve to set
     */
    public void setAttackerBelieve(boolean attackerBelieve) {
        this.attackerBelieve = attackerBelieve;
    }

    /**
     * @return the defenderBelieve
     */
    public boolean isDefenderBelieve() {
        if (ConfigManager.getSingleton().isChurch()) {
            return defenderBelieve;
        } else {
            //if no church is used take care that the believe factor would be 1 later
            return true;
        }
    }

    /**
     * @param defenderBelieve the defenderBelieve to set
     */
    public void setDefenderBelieve(boolean defenderBelieve) {
        this.defenderBelieve = defenderBelieve;
    }

    /**
     * @return the cataChurch
     */
    public boolean isCataChurch() {
        return cataChurch;
    }

    /**
     * @param cataChurch the cataChurch to set
     */
    public void setCataChurch(boolean cataChurch) {
        this.cataChurch = cataChurch;
    }

    /**
     * @return the cataChurch
     */
    public boolean isCataWall() {
        return cataWall;
    }

    /**
     * @param cataChurch the cataChurch to set
     */
    public void setCataWall(boolean cataWall) {
        this.cataWall = cataWall;
    }

    /**
     * @return the cataFarm
     */
    public boolean isCataFarm() {
        return cataFarm;
    }

    /**
     * @param cataFarm the cataFarm to set
     */
    public void setCataFarm(boolean cataFarm) {
        this.cataFarm = cataFarm;
    }
}
