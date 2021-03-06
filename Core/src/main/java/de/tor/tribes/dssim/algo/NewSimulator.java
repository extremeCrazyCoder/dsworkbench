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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Torridity
 */
public class NewSimulator extends AbstractSimulator {
    private static Logger logger = LogManager.getLogger("NewSimulator");

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
        int ramCount = pOff.getAmountForUnit("ram");
        if(ramCount == -1) {
            ramCount = 0;
        }

        if (!isAttackerBelieve()) {
            //if attacker does not believe, ram fight at half power
            ramCount /= 2;
        }

        int wallAtFight = getWallLevel() - (int) Math.round(ramCount / (4 * Math.pow(1.09, getWallLevel())));
        if (wallAtFight < (int) Math.round((double) getWallLevel() / 2.0)) {
            wallAtFight = (int) Math.round((double) getWallLevel() / 2.0);
        }

        //enter three calculation rounds
        for (int i = 0; i < 3; i++) {
            //calculate strengths based on survivors of each round
            double[] offStrengths = calculateOffStrengths(result.getSurvivingOff());
            double[] defStrengths = calculateDefStrengths(result.getSurvivingDef(), offStrengths, wallAtFight, (i == 0));
            logger.debug("off {} / deff {} / troops {} ; {}", offStrengths, defStrengths, result.getSurvivingOff(), result.getSurvivingDef());
            //calculate losses
            double[] offLosses = calulateLosses(offStrengths, defStrengths);
            double[] defLosses = calulateLosses(defStrengths, offStrengths);

            //correct troops and repeat calculation
            correctTroops(offStrengths, offLosses, defLosses, result, (i == 0));
        }

        result.setWin(! result.getSurvivingDef().hasUnits());

        // <editor-fold defaultstate="collapsed" desc="Wall calculation">
        if (result.isWin() && ramCount > 0) {
            //calculate wall after fight
            //1.09
            //1.0900663842

            double maxDecrement = (double) ramCount * DataHolder.getSingleton().getUnitByPlainName("ram").getAttack() / (4 * Math.pow(1.09, getWallLevel()));
            double lostUnits = 0;
            double totalUnits = 0;

            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                if (!unit.isSpy()) {
                    totalUnits += getOff().getAmountForUnit(unit);
                    lostUnits += getOff().getAmountForUnit(unit) - result.getSurvivingOff().getAmountForUnit(unit);
                }
            }

            double ratio = lostUnits / totalUnits;
            int wallDecrement = (int) Math.round(-1 * maxDecrement / 2 * ratio + maxDecrement);
            result.setWallLevel((getWallLevel() - wallDecrement < 0) ? 0 : getWallLevel() - wallDecrement);
        } else if (ramCount <= 0) {
            //no change
            result.setWallLevel(getWallLevel());
        } else {
            //lost scenario
            double lostUnits = 0;
            double totalUnits = 0;
            for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                totalUnits += getDef().getAmountForUnit(unit);
                lostUnits += getDef().getAmountForUnit(unit) - result.getSurvivingDef().getAmountForUnit(unit);
            }
            double ratio = lostUnits / totalUnits;
            int wallDecrement = (int) Math.round((ramCount * ratio) * 2 / (8 * Math.pow(1.09, getWallLevel())));
            result.setWallLevel((getWallLevel() - wallDecrement < 0) ? 0 : getWallLevel() - wallDecrement);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Building calculation">
        //demolish building
        double buildingAfter = getBuildingLevel();
        if (isCataWall()) {
            setBuildingLevel(result.getWallLevel());
            result.setBuildingBefore(result.getWallLevel());
        }
        UnitHolder cataUnit = DataHolder.getSingleton().getUnitByPlainName("catapult");
        int cataCount = getOff().getAmountForUnit(cataUnit);
        if (cataCount > 0) {
            if (!isAttackerBelieve()) {
                //if attacker does not believe, cata fight at half power
                cataCount /= 2;
            }
            int buildingDecrement = 0;
            if (!result.isWin()) {
                //attack lost
                double lostUnits = 0;
                double totalUnits = 0;
                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    totalUnits += getDef().getAmountForUnit(unit);
                    lostUnits += getDef().getAmountForUnit(unit) - result.getSurvivingDef().getAmountForUnit(unit);
                }
                double ratio = lostUnits / totalUnits;
                if (isCataChurch() && getBuildingLevel() <= 3) {
                    //cata is aiming at the church
                    buildingDecrement = (int) Math.round(getMaxChurchDestruction(cataCount * ratio) / 2);
                } else {
                    //cata is aiming elsewhere
                    buildingDecrement = (int) Math.round(((cataCount * cataUnit.getAttack()) / (600 * Math.pow(1.09, getBuildingLevel()))) * ratio);
                }
                buildingAfter -= buildingDecrement;
            } else {
                //attacker wins
                double lostUnits = 0;
                double totalUnits = 0;

                for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
                    totalUnits += getOff().getAmountForUnit(unit);
                    lostUnits += getOff().getAmountForUnit(unit) - result.getSurvivingOff().getAmountForUnit(unit);
                }

                double ratio = lostUnits / totalUnits;
                double maxDecrement = 0.0;

                if (isCataChurch() && getBuildingLevel() <= 3) {
                    //cata is aiming at the church
                    maxDecrement = getMaxChurchDestruction(cataCount);
                    buildingDecrement = (int) Math.round(-1 * maxDecrement / 2 * ratio + maxDecrement);
                } else {
                    //cata is aiming elsewhere
                    maxDecrement = cataCount * cataUnit.getAttack() / (300 * Math.pow(1.09, getBuildingLevel()));
                    buildingDecrement = (int) Math.round(-1 * maxDecrement / 2 * ratio + maxDecrement);
                }
                buildingAfter -= buildingDecrement;
            }
            result.setBuildingLevel((buildingAfter <= 0) ? 0 : (int) buildingAfter);
            if (pCataWall) {
                int wallResult = result.getWallLevel() - buildingDecrement;
                result.setWallLevel(wallResult <= 0 ? 0 : wallResult);
            }
        } else {
            //no demolishion
            result.setBuildingLevel(getBuildingLevel());
        }
        // </editor-fold>
        return result;
    }

    private double getMaxChurchDestruction(double pCataCount) {
        switch (getBuildingLevel()) {
            case 1: {
                return pCataCount / 800;
            }
            case 2: {
                return pCataCount / 333;
            }
            case 3: {
                return pCataCount / 240;
            }
        }
        return -1;
    }

    private double[] calculateOffStrengths(TroopAmountFixed pTroops) {
        double[] result = new double[3];
        result[ID_INFANTRY] = pTroops.getOffInfantryValue(getOffTech());
        result[ID_CAVALRY] = pTroops.getOffCavalryValue(getOffTech());
        result[ID_ARCHER] = pTroops.getOffArcherValue(getOffTech());
        double moral = getMoral() / 100;
        double luck = ((100 + getLuck()) / 100);
        double believeFactor = (isAttackerBelieve()) ? 1.0 : 0.5;

        result[ID_INFANTRY] = result[ID_INFANTRY] * moral * luck * believeFactor;
        result[ID_CAVALRY] = result[ID_CAVALRY] * moral * luck * believeFactor;
        result[ID_ARCHER] = result[ID_ARCHER] * moral * luck * believeFactor;
        return result;
    }

    private double[] calculateDefStrengths(TroopAmountFixed pTroops, double[] pOffStrengths, int pWallAtFight, boolean pUseBasicDefense) {
        double[] result = new double[3];
        double totalOff = 0;
        for (double d : pOffStrengths) {
            totalOff += d;
        }
        double infantryMulti = (totalOff == 0) ? 0 : pOffStrengths[ID_INFANTRY] / totalOff;
        double cavalryMulti = (totalOff == 0) ? 0 : pOffStrengths[ID_CAVALRY] / totalOff;
        double archerMulti = (totalOff == 0) ? 0 : pOffStrengths[ID_ARCHER] / totalOff;
        result[ID_INFANTRY] = pTroops.getDefInfantryValue(getDefTech());
        result[ID_CAVALRY] = pTroops.getDefCavalryValue(getDefTech());
        result[ID_ARCHER] = pTroops.getDefArcherValue(getDefTech());
        double believeFactor = (isDefenderBelieve()) ? 1.0 : 0.5;
        
        double farmFactor = 1.0;
        //calculate farm factor if farm limit exists
        if (ServerSettings.getSingleton().getFarmLimit() != 0) {
            double limit = getFarmLevel() * ServerSettings.getSingleton().getFarmLimit();
            double defFarmUsage = calculateDefFarmUsage();
            farmFactor = limit / defFarmUsage;
            if (farmFactor > 1.0) {
                farmFactor = 1.0;
            }
        }
            
        result[ID_INFANTRY] *= infantryMulti * believeFactor * farmFactor;
        result[ID_CAVALRY] *= cavalryMulti * believeFactor * farmFactor;
        result[ID_ARCHER] *= archerMulti * believeFactor * farmFactor;
        
        double nightBonus = (isNightBonus()) ? 2 : 1;
        double[] basicDefense = new double[]{0.0, 0.0, 0.0};
        if (pUseBasicDefense) {
            basicDefense[ID_INFANTRY] = (20.0 + (double) pWallAtFight * 50.0) * ((totalOff == 0) ? 0 : pOffStrengths[ID_INFANTRY] / totalOff);
            basicDefense[ID_CAVALRY] = (20.0 + (double) pWallAtFight * 50.0) * ((totalOff == 0) ? 0 : pOffStrengths[ID_CAVALRY] / totalOff);
            basicDefense[ID_ARCHER] = (20.0 + (double) pWallAtFight * 50.0) * ((totalOff == 0) ? 0 : pOffStrengths[ID_ARCHER] / totalOff);
        }
        
        result[ID_INFANTRY] = result[ID_INFANTRY] * nightBonus * Math.pow(1.037, pWallAtFight) + basicDefense[ID_INFANTRY];
        result[ID_CAVALRY] = result[ID_CAVALRY] * nightBonus * Math.pow(1.037, pWallAtFight) + basicDefense[ID_CAVALRY];
        result[ID_ARCHER] = result[ID_ARCHER] * nightBonus * Math.pow(1.037, pWallAtFight) + basicDefense[ID_ARCHER];

        return result;
    }

    private double[] calulateLosses(double[] pCalculateFor, double[] pOther) {
        double[] losses = new double[3];
        double lossFactor = 1.5;
        if (ServerSettings.getSingleton().getFarmLimit() != 0) {
            lossFactor = 1.6;
        }
        //calculate losses
        for (int i = 0; i <= ID_ARCHER; i++) {
            if (pCalculateFor[i] == 0 || pOther[i] == 0) {
                //one party was completely killed
                losses[i] = 0;
            } else {
                if (pCalculateFor[i] > pOther[i]) {
                    losses[i] = Math.pow(pOther[i] / pCalculateFor[i], lossFactor);
                } else {
                    //completely list
                    losses[i] = 1;
                }//end of complete loss
            }//end of nothing lost
        }//end of for loop

        return losses;
    }

    private void correctTroops(double[] pOffStrengths, double[] pOffLosses, double[] pDefLosses, SimulatorResult pResult, boolean pSpyRound) {
        //correct off
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int unitOffAmount = pResult.getSurvivingOff().getAmountForUnit(unit);
            int unitDefAmount = pResult.getSurvivingDef().getAmountForUnit(unit);
            if (unit.isInfantry() && !unit.isArcher()) {
                pResult.getSurvivingOff().setAmountForUnit(unit, (int) Math.round(unitOffAmount * (1 - pOffLosses[ID_INFANTRY])));
            } else if (unit.isCavalry() && !unit.isArcher()) {
                if (!unit.isSpy()) {
                    pResult.getSurvivingOff().setAmountForUnit(unit, (int) Math.round(unitOffAmount * (1 - pOffLosses[ID_CAVALRY])));
                } else {
                    //only correct spys in first round
                    if (pSpyRound) {
                        double spyLosses = 0;
                        double spyRateTillDeath = 2.0;
                        double lossFactor = 1.5;
                        if (ServerSettings.getSingleton().getFarmLimit() != 0) {
                            lossFactor = 1.6;
                        }

                        //special spy calculation
                        if (unitOffAmount == 0) {
                            //no spy
                            spyLosses = 0;
                        } else if ((double) unitDefAmount / (double) unitOffAmount >= spyRateTillDeath) {
                            //no change
                            spyLosses = unitOffAmount;
                        } else {
                            //increment Def by 1 and use lossRatio and spyRate depending on server
                            spyLosses = (double) unitOffAmount * Math.pow((double) (unitDefAmount + 1) / ((double) unitOffAmount * spyRateTillDeath), lossFactor);
                        }
                        pResult.getSurvivingOff().setAmountForUnit(unit, (int) Math.round((double) unitOffAmount - spyLosses));
                    }
                }
            } else {
                pResult.getSurvivingOff().setAmountForUnit(unit, (int) Math.round((double) unitOffAmount - ((double) unitOffAmount * pOffLosses[ID_ARCHER])));
            }
        }
        double totalOff = 0;
        for (double d : pOffStrengths) {
            totalOff += d;
        }
        //correct def
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int unitDefCount = pResult.getSurvivingDef().getAmountForUnit(unit);
            double decreaseFactor = pOffStrengths[ID_INFANTRY] * pDefLosses[ID_INFANTRY] + pOffStrengths[ID_CAVALRY] * pDefLosses[ID_CAVALRY] + pOffStrengths[ID_ARCHER] * pDefLosses[ID_ARCHER];
            int survive = (int) Math.round((double) unitDefCount - ((double) unitDefCount / ((totalOff == 0) ? 1 : totalOff) * decreaseFactor));
            pResult.getSurvivingDef().setAmountForUnit(unit, survive);
        }
    }
    //Calculate how many farm places are needed for the current def

    private double calculateDefFarmUsage() {
        return getDef().getTroopPopCount();
    }
}
