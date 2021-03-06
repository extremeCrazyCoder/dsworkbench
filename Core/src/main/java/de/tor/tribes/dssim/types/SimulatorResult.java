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
package de.tor.tribes.dssim.types;

import de.tor.tribes.io.TroopAmountFixed;

/**
 *
 * @author Jejkal
 */
public class SimulatorResult {

    private boolean win = false;
    private int nukes = 1;
    private TroopAmountFixed offBefore = null;
    private TroopAmountFixed defBefore = null;
    private TroopAmountFixed survivingOff = null;
    private TroopAmountFixed survivingDef = null;
    private int wallLevel = 0;
    private int buildingLevel = 0;
    private int wallBefore = 0;
    private int buildingBefore = 0;
    private boolean cataAtWall = false;

    public SimulatorResult() {
        survivingOff = new TroopAmountFixed(0);
        survivingDef = new TroopAmountFixed(0);
    }

    public SimulatorResult(TroopAmountFixed pOff, TroopAmountFixed pDef) {
        survivingOff = pOff.clone();
        survivingDef = pDef.clone();
    }

    public void setOffBefore(TroopAmountFixed pOff) {
        offBefore = pOff;
    }

    public void setDefBefore(TroopAmountFixed pDef) {
        defBefore = pDef;
    }

    public TroopAmountFixed getOffBefore() {
        return offBefore;
    }

    public TroopAmountFixed getDefBefore() {
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
    public TroopAmountFixed getSurvivingOff() {
        return survivingOff;
    }

    /**
     * @param survivingOff the survivingOff to set
     */
    public void setSurvivingOff(TroopAmountFixed survivingOff) {
        this.survivingOff = survivingOff;
    }

    /**
     * @return the survivingDef
     */
    public TroopAmountFixed getSurvivingDef() {
        return survivingDef;
    }

    /**
     * @param survivingDef the survivingDef to set
     */
    public void setSurvivingDef(TroopAmountFixed survivingDef) {
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
