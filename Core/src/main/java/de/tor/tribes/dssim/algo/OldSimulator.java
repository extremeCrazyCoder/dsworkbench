/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.algo;

import de.tor.tribes.dssim.types.AbstractUnitElement;
import de.tor.tribes.dssim.types.KnightItem;
import de.tor.tribes.dssim.types.SimulatorResult;
import de.tor.tribes.dssim.types.UnitHolder;
import de.tor.tribes.dssim.util.ConfigManager;
import de.tor.tribes.dssim.util.UnitManager;
import java.util.HashMap;
import java.util.List;

/**
 * @author Charon
 */
public class OldSimulator extends AbstractSimulator {

    boolean DEBUG = false;

    @Override
    public SimulatorResult calculate(
            HashMap<UnitHolder, AbstractUnitElement> pOff,
            HashMap<UnitHolder, AbstractUnitElement> pDef,
            KnightItem pOffItem,
            List<KnightItem> pDefItems,
            boolean pNightBonus,
            double pLuck,
            double pMoral,
            int pWallLevel,
            int pBuildingLevel,
            int pFarmLevel,
            boolean pAttackerBelieve,
            boolean pDefenderBelieve,
            boolean pCataChurch,
            boolean pCataFarm,
            boolean pCataWall) {
        setOff(pOff);
        setDef(pDef);
        setMoral(pMoral);
        setLuck(pLuck);
        setNightBonus(pNightBonus);
        setWallLevel(pWallLevel);
        setBuildingLevel(pBuildingLevel);
        setFarmLevel(pFarmLevel);
        setAttackerBelieve(pAttackerBelieve);
        setDefenderBelieve(pDefenderBelieve);
        setCataFarm(pCataFarm);
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
        if (ConfigManager.getSingleton().getFarmLimit() != 0) {
            double limit = getFarmLevel() * ConfigManager.getSingleton().getFarmLimit();
            double defFarmUsage = calculateDefFarmUsage();
            double factor = limit / defFarmUsage;
            if (factor > 1.0) {
                factor = 1.0;
            }
            defStrength = factor * defStrength;
        }

        //temporary lower wall for fight
        AbstractUnitElement rams = getOff().get(UnitManager.getSingleton().getUnitByPlainName("ram"));
        double ramCount = 0;
        double ramAttPoint = 0;
        double wallAtFight = getWallLevel();
        if (rams != null && rams.getCount() != 0) {
            //rams are used, so calculate the fight level of the wall
            ramCount = rams.getCount();
            println("RamCount: " + ramCount);
            ramAttPoint = rams.getUnit().getAttack() * getTechFactor(rams.getTech());
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

        // <editor-fold defaultstate="collapsed" desc="Debug output">
        println("OffInf " + offStrengths[ID_INFANTRY]);
        println("OffCav " + offStrengths[ID_CAVALRY]);
        println("DefInf " + (defStrengths[ID_INFANTRY] * infantryRatio));
        println("DefCav " + (defStrengths[ID_CAVALRY] * cavaleryRatio));
        println("InfRatio " + infantryRatio);
        println("CavRatio " + cavaleryRatio);
        println("---------------");
        println("OffStrength " + offStrength);
        println("DefStrength " + defStrength);
        //</editor-fold>

        //obtain loss ratio based on tech level
        double lossPowerValue = 1.5;
        if ((ConfigManager.getSingleton().getTech() == ConfigManager.ID_TECH_10)
                || (ConfigManager.getSingleton().getFarmLimit() != 0)) {
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
        AbstractUnitElement cata = getOff().get(UnitManager.getSingleton().getUnitByPlainName("catapult"));
        double buildingAfter = getBuildingLevel();
        if (isCataWall()) {
            setBuildingLevel(result.getWallLevel());
            result.setBuildingBefore(result.getWallLevel());
            buildingAfter = getBuildingLevel();
        }
        double buildingDemolish = 0;
        if (cata != null && cata.getCount() != 0) {
            double cataAttPoints = cata.getUnit().getAttack() * getTechFactor(cata.getTech());
            if (lossRatioOff > 1) {
                //attacker lost
                if (isCataFarm()) {
                    /* double buildingDemolish = Math.pow((offStrength / defStrength), lossPowerValue) * (cataAttPoints * cata.getCount()) / (700 * Math.pow(1.09, getBuildingLevel()));
                    buildingDemolish = (buildingDemolish > 3.0) ? 3.0 : buildingDemolish;
                    println("DemoBuild " + buildingDemolish);
                    buildingAfter = Math.round(getBuildingLevel() - buildingDemolish);*/
                } else {
                    buildingDemolish = Math.pow((offStrength / defStrength), lossPowerValue) * (cataAttPoints * cata.getCount()) / (600 * Math.pow(1.09, getBuildingLevel()));
                    println("DemoBuild " + buildingDemolish);
                    buildingAfter = Math.round(getBuildingLevel() - buildingDemolish);
                }
            } else {
                //attacker wins
                if (isCataFarm()) {
                    /*   System.out.println("Cata farm");
                    double buildingDemolish = (1-Math.pow((defStrength / offStrength), lossPowerValue)) * (cataAttPoints * cata.getCount()) / (700 * Math.pow(1.09, (getBuildingLevel())));
                    buildingDemolish = (buildingDemolish > 3.0) ? 3.0 : buildingDemolish;
                    buildingAfter = Math.round(getBuildingLevel() - buildingDemolish);*/
                } else {
                    buildingDemolish = (2 - Math.pow((defStrength / offStrength), lossPowerValue)) * (cataAttPoints * cata.getCount()) / (600 * Math.pow(1.09, (getBuildingLevel())));
                    println("DemoBuild " + buildingDemolish);
                    buildingAfter = Math.round(getBuildingLevel() - buildingDemolish);
                }
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
        for (UnitHolder unit : UnitManager.getSingleton().getUnits()) {
            if (!isSpy(unit)) {
                //normal calculation for off losses
                int survivors = (int) Math.round(getOff().get(unit).getCount() - lossRatioOff * getOff().get(unit).getCount());
                result.getSurvivingOff().get(unit).setCount((survivors < 0) ? 0 : survivors);
            } else {
                double spyRateTillDeath = 2.0;
                if (ConfigManager.getSingleton().getSpyType() == 3) {
                    spyRateTillDeath = 1.0;
                }

                //special handling for spies
                int spyLosses = 0;
                //special spy calculation
                if (getOff().get(unit).getCount() == 0) {
                    //no spy
                    spyLosses = 0;
                } else if ((getDef().get(unit).getCount() + 1) / getOff().get(unit).getCount() >= spyRateTillDeath) {
                    //no change
                    spyLosses = getOff().get(unit).getCount();
                } else {
                    spyLosses = (int) Math.round((double) getOff().get(unit).getCount() * Math.pow((double) (getDef().get(unit).getCount() + 1) / (double) getOff().get(unit).getCount() / spyRateTillDeath, lossPowerValue));
                }
                result.getSurvivingOff().get(unit).setCount(result.getSurvivingOff().get(unit).getCount() - spyLosses);
            }


            //calculate def losses
            int survivors = (int) Math.round(getDef().get(unit).getCount() - lossRatioDef * getDef().get(unit).getCount());
            result.getSurvivingDef().get(unit).setCount((survivors < 0) ? 0 : survivors);
        }

        //calculate who has won
        result.setWin(true);
        for (UnitHolder u : UnitManager.getSingleton().getUnits()) {
            if (result.getSurvivingDef().get(u).getCount() > 0) {
                //at least one defender has survived
                result.setWin(false);
                break;
            }
        }
        return result;
    }

    private void println(String value) {
        if (DEBUG) {
            System.out.println(value);
        }
    }

    /**Calculate the overall strength of the current off divided into infantry and cavalry*/
    private double[] calculateOffStrengthts() {
        double[] result = new double[]{0.0, 0.0};
        for(UnitHolder unit: getOff().keySet()) {
            AbstractUnitElement unitElement = getOff().get(unit);
            if (isInfantry(unit)) {
                result[ID_INFANTRY] += unitElement.getCount() * unit.getAttack() * getTechFactor(unitElement.getTech());
            }
            if (isCavalery(unit)) {
                result[ID_CAVALRY] += unitElement.getCount() * unit.getAttack() * getTechFactor(unitElement.getTech());
            }
        }
        //integrate moral and luck
        double moral = getMoral() / 100;
        double luck = ((100 + getLuck()) / 100);
        result[ID_INFANTRY] = result[ID_INFANTRY] * moral * luck;
        result[ID_CAVALRY] = result[ID_CAVALRY] * moral * luck;
        return result;
    }

    /**Calculate the overall strength of the current def divided into infantry and cavalry*/
    private double[] calculateDefStrengths() {
        double[] result = new double[]{0.0, 0.0};
        for(UnitHolder unit: getDef().keySet()) {
            AbstractUnitElement unitElement = getDef().get(unit);
            result[ID_INFANTRY] += unitElement.getCount() * unit.getDefense() * getTechFactor(unitElement.getTech());
            result[ID_CAVALRY] += unitElement.getCount() * unit.getDefenseCavalry() * getTechFactor(unitElement.getTech());
        }

        result[ID_INFANTRY] = result[ID_INFANTRY] * ((isNightBonus()) ? 2.0 : 1.0);
        result[ID_CAVALRY] = result[ID_CAVALRY] * ((isNightBonus()) ? 2.0 : 1.0);
        return result;
    }

    //Calculate how many farm places are needed for the current def
    private double calculateDefFarmUsage() {
        int result = 0;
        for(UnitHolder unit: getDef().keySet()) {
            AbstractUnitElement unitElement = getDef().get(unit);
            result += unit.getPop() * unitElement.getCount();
        }
        return result;
    }

    /**Get the factors for the different tech levels*/
    private double getTechFactor(int pLevel) {
        if (ConfigManager.getSingleton().getTech() == ConfigManager.ID_TECH_3) {
            switch (pLevel) {
                case 2:
                    return 1.25;
                case 3:
                    return 1.4;
                default:
                    return 1;
            }
        } else if (ConfigManager.getSingleton().getTech() == ConfigManager.ID_TECH_10) {
            //use 10 level tech factor
            return Math.pow(1.04608, (pLevel - 1));
        }
        //for simple tech servers
        return 1.0;
    }
}
