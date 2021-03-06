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
package de.tor.tribes.dssim.algo;

import de.tor.tribes.dssim.types.SimulatorResult;
import de.tor.tribes.dssim.types.TechState;
import de.tor.tribes.dssim.ui.DSWorkbenchSimulatorFrame;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.util.ServerSettings;

/**
 *
 * @author Charon
 */
public abstract class AbstractSimulator {

    public final int ID_INFANTRY = 0;
    public final int ID_CAVALRY = 1;
    public final int ID_ARCHER = 2;
    private TroopAmountFixed off = null;
    private TroopAmountFixed def = null;
    private TechState offTech = null;
    private TechState defTech = null;
    private boolean nightBonus = false;
    private double luck = 0.0;
    private double moral = 100;
    private int wallLevel = 0;
    private int buildingLevel = 0;
    private int farmLevel = 0;
    private boolean attackerBelieve = false;
    private boolean defenderBelieve = false;
    private boolean cataChurch = false;
    private boolean cataWall = false;

    public abstract SimulatorResult calculate(TroopAmountFixed pOff, TroopAmountFixed pDef, TechState pOffTech, TechState pDefTech, boolean pNightBonus, double pLuck, double pMoral, int pWallLevel, int pBuildingLevel, int pFarmLevel, boolean pAttackerBelieve, boolean pDefenderBelieve, boolean pCataChurch, boolean pCataWall);

    public SimulatorResult bunkerBuster(TroopAmountFixed pOff, TroopAmountFixed pDef, TechState pOffTech, TechState pDefTech, boolean pNightBonus, double pLuck, double pMoral, int pWallLevel, int pBuildingLevel, int pFarmLevel, boolean pAttackerBelieve, boolean pDefenderBelieve, boolean pCataChurch, boolean pCataWall) {
        SimulatorResult result = calculate(pOff, pDef, pOffTech, pDefTech, pNightBonus, pLuck, pMoral, pWallLevel, pBuildingLevel, pFarmLevel, pAttackerBelieve, pDefenderBelieve, pCataChurch, pCataWall);
        DSWorkbenchSimulatorFrame.getSingleton().addResultExternally(result);
        setFarmLevel(pFarmLevel);
        setCataChurch(pCataChurch);
        setCataWall(pCataWall);
       
        int cnt = 1;
        while (!result.isWin() && cnt <= 1000) {
            result = calculate(pOff, result.getSurvivingDef(), pOffTech, pDefTech, pNightBonus, pLuck, pMoral, result.getWallLevel(), result.getBuildingLevel(), pFarmLevel, pAttackerBelieve, pDefenderBelieve, pCataChurch, pCataWall);
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

    public void setOff(TroopAmountFixed pOff) {
        off = pOff;
    }

    public TroopAmountFixed getOff() {
        return off;
    }

    public void setDef(TroopAmountFixed pDef) {
        def = pDef;
    }

    public TroopAmountFixed getDef() {
        return def;
    }

    public void setOffTech(TechState pOffTech) {
        offTech = pOffTech;
    }

    public TechState getOffTech() {
        return offTech;
    }

    public void setDefTech(TechState pDefTech) {
        defTech = pDefTech;
    }

    public TechState getDefTech() {
        return defTech;
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
        if (ServerSettings.getSingleton().isChurch()) {
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
        if (ServerSettings.getSingleton().isChurch()) {
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
}
