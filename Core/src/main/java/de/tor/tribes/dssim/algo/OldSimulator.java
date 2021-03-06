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
import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.ServerSettings;

/**
 * @author Charon
 */
public class OldSimulator extends AbstractSimulator {

    boolean DEBUG = false;

    @Override
    public SimulatorResult calculate(
            TroopAmountFixed pOff,
            TroopAmountFixed pDef,
            TechState pOffTech,
            TechState pDefTech,
            boolean pNightBonus,
            double pLuck,
            double pMoral,
            int pWallLevel,
            int pBuildingLevel,
            int pFarmLevel,
            boolean pAttackerBelieve,
            boolean pDefenderBelieve,
            boolean pCataChurch,
            boolean pCataWall) {
        setOff(pOff);
        setDef(pDef);
        setOffTech(pOffTech);
        setDefTech(pOffTech);
        setMoral(pMoral);
        setLuck(pLuck);
        setNightBonus(pNightBonus);
        setWallLevel(pWallLevel);
        setBuildingLevel(pBuildingLevel);
        setFarmLevel(pFarmLevel);
        setAttackerBelieve(pAttackerBelieve);
        setDefenderBelieve(pDefenderBelieve);
        setCataChurch(pCataChurch);
        setCataWall(pCataWall);
        SimulatorResult result = new SimulatorResult(getOff(), getDef());
        result.setBuildingBefore(pBuildingLevel);
        //obtain current strengths
        double[] offStrengths = calculateOffStrengthts();
        double[] defStrengths = calculateDefStrengths();

        //calculate infantry-cavalry-ratio
        double infantryRatio = 0.0;
        if (offStrengths[ID_INFANTRY] != 0 || offStrengths[ID_CAVALRY] != 0) {
            infantryRatio = offStrengths[ID_INFANTRY] / (offStrengths[ID_INFANTRY] + offStrengths[ID_CAVALRY]);
        }
        double cavaleryRatio = 1 - infantryRatio;
        //adept def based on according ratio
        double defStrength = infantryRatio * defStrengths[ID_INFANTRY] + cavaleryRatio * defStrengths[ID_CAVALRY];

        //calculate farm factor if farm limit exists
        if (ServerSettings.getSingleton().getFarmLimit() != 0) {
            double limit = getFarmLevel() * ServerSettings.getSingleton().getFarmLimit();
            double defFarmUsage = calculateDefFarmUsage();
            double factor = limit / defFarmUsage;
            if (factor > 1.0) {
                factor = 1.0;
            }
            defStrength = factor * defStrength;
        }

        //temporary lower wall for fight
        UnitHolder ramsElm = DataHolder.getSingleton().getUnitByPlainName("ram");
        double ramCount = getOff().getAmountForUnit(ramsElm);
        if(ramCount < 0) ramCount = 0;
        
        double ramAttPoint = 0;
        double wallAtFight = getWallLevel();
        if (ramCount > 0) {
            //rams are used, so calculate the fight level of the wall
            println("RamCount: " + ramCount);
            ramAttPoint = ramsElm.getAttack() * getOffTech().getFactor(ramsElm);
            println("RamAttPoints: " + ramAttPoint);
            double wallReduction = ramCount / (4 * Math.pow(1.09, getWallLevel()));
            if (wallReduction > (double) getWallLevel() / 2.0) {
                //min fight level is half of the actual wall
                wallReduction = (double) getWallLevel() / 2.0;
            }
            println("WallReduction: " + wallReduction);
            wallAtFight = Math.round(getWallLevel() - wallReduction);
            println("WallAtFight: " + wallAtFight);
        }

        //calculate full def including wall and base defense
        defStrength = (20 + 50 * wallAtFight) + (defStrength * Math.pow(1.037, wallAtFight));
        double offStrength = offStrengths[ID_INFANTRY] + offStrengths[ID_CAVALRY];

        //obtain loss ratio based on tech level
        double lossPowerValue = 1.5;
        if ((ServerSettings.getSingleton().getTechType() == ServerSettings.TECH_10)
                || (ServerSettings.getSingleton().getFarmLimit() != 0)) {
            lossPowerValue = 1.6;
        }

        //calculate loss factor for off and def
        double lossRatioOff = Math.pow((defStrength / offStrength), lossPowerValue);
        double lossRatioDef = Math.pow((offStrength / defStrength), lossPowerValue);
        println("LossOff: " + lossRatioOff);
        println("LossDef: " + lossRatioDef);

        //calculate wall after fight
        double wallAfter = getWallLevel();
        if (lossRatioOff > 1) {
            //attack lost
            double wallDemolish = Math.pow((offStrength / defStrength), lossPowerValue) * (ramAttPoint * ramCount) / (8 * Math.pow(1.09, getWallLevel()));
            println("Demo " + wallDemolish);
            wallAfter = Math.round(getWallLevel() - wallDemolish);
        } else {
            //attacker wins
            double wallDemolish = (2 - Math.pow((defStrength / offStrength), lossPowerValue)) * (ramAttPoint * ramCount) / (8 * Math.pow(1.09, getWallLevel()));
            println("Demo " + wallDemolish);
            wallAfter = Math.round(getWallLevel() - wallDemolish);
        }
        println("WallAfter: " + wallAfter);
        result.setWallLevel((wallAfter <= 0) ? 0 : (int) wallAfter);

        //calculate building destruction
        UnitHolder cataElm = DataHolder.getSingleton().getUnitByPlainName("catapult");
        int cataAmount = getOff().getAmountForUnit(cataElm);
        double buildingAfter = getBuildingLevel();
        if (isCataWall()) {
            setBuildingLevel(result.getWallLevel());
            result.setBuildingBefore(result.getWallLevel());
            buildingAfter = getBuildingLevel();
        }
        double buildingDemolish = 0;
        if (cataAmount > 0) {
            double cataAttPoints = cataElm.getAttack() * getOffTech().getFactor(cataElm);
            if (lossRatioOff > 1) {
                //attacker lost
                buildingDemolish = Math.pow((offStrength / defStrength), lossPowerValue) * (cataAttPoints * cataAmount) / (600 * Math.pow(1.09, getBuildingLevel()));
                println("DemoBuild " + buildingDemolish);
                buildingAfter = Math.round(getBuildingLevel() - buildingDemolish);
            } else {
                //attacker wins
                buildingDemolish = (2 - Math.pow((defStrength / offStrength), lossPowerValue)) * (cataAttPoints * cataAmount) / (600 * Math.pow(1.09, (getBuildingLevel())));
                println("DemoBuild " + buildingDemolish);
                buildingAfter = Math.round(getBuildingLevel() - buildingDemolish);
            }
            //set building level after destruction
            println("BuildingAfter: " + buildingAfter);
            result.setBuildingLevel((buildingAfter <= 0) ? 0 : (int) buildingAfter);
            if (pCataWall) {
                result.setWallLevel(result.getWallLevel() - (int) buildingDemolish);
            }
        } else {
            //no demolishion
            println("BuildingAfter: -unchanged-");
            result.setBuildingLevel(getBuildingLevel());
        }

        //substract lost units
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int offAmount = getOff().getAmountForUnit(unit);
            int defAmount = getDef().getAmountForUnit(unit);
            if (!unit.isSpy()) {
                //normal calculation for off losses
                int survivors = (int) Math.round(offAmount - lossRatioOff * offAmount);
                result.getSurvivingOff().setAmountForUnit(unit, (survivors < 0) ? 0 : survivors);
            } else {
                double spyRateTillDeath = 2.0;

                //special handling for spies
                int spyLosses = 0;
                //special spy calculation
                if (offAmount == 0) {
                    //no spy
                    spyLosses = 0;
                } else if ((defAmount + 1) / offAmount >= spyRateTillDeath) {
                    //no change
                    spyLosses = offAmount;
                } else {
                    spyLosses = (int) Math.round((double) offAmount * Math.pow((double) (defAmount + 1) / (double) offAmount / spyRateTillDeath, lossPowerValue));
                }
                result.getSurvivingOff().setAmountForUnit(unit, result.getSurvivingOff().getAmountForUnit(unit) - spyLosses);
            }


            //calculate def losses
            int survivors = (int) Math.round(defAmount - lossRatioDef * defAmount);
            result.getSurvivingDef().setAmountForUnit(unit, (survivors < 0) ? 0 : survivors);
        }

        //calculate who has won
        result.setWin(! result.getSurvivingDef().hasUnits());
        return result;
    }

    private void println(String value) {
        if (DEBUG) {
            System.out.println(value);
        }
    }

    /**Calculate the overall strength of the current off divided into infantry and cavalry*/
    private double[] calculateOffStrengthts() {
        double[] result = new double[2];
        result[ID_INFANTRY] = getOff().getOffInfantryValue(getOffTech());
        result[ID_CAVALRY] = getOff().getOffCavalryValue(getOffTech());
        
        //integrate moral and luck
        double moral = getMoral() / 100;
        double luck = ((100 + getLuck()) / 100);
        result[ID_INFANTRY] = result[ID_INFANTRY] * moral * luck;
        result[ID_CAVALRY] = result[ID_CAVALRY] * moral * luck;
        return result;
    }

    /**Calculate the overall strength of the current def divided into infantry and cavalry*/
    private double[] calculateDefStrengths() {
        double[] result = new double[2];
        result[ID_INFANTRY] = getDef().getDefInfantryValue(getDefTech());
        result[ID_CAVALRY] = getDef().getDefCavalryValue(getDefTech());

        result[ID_INFANTRY] = result[ID_INFANTRY] * ((isNightBonus()) ? 2.0 : 1.0);
        result[ID_CAVALRY] = result[ID_CAVALRY] * ((isNightBonus()) ? 2.0 : 1.0);
        return result;
    }

    //Calculate how many farm places are needed for the current def
    private double calculateDefFarmUsage() {
        return getDef().getTroopPopCount();
    }
}
