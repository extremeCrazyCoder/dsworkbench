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
package de.tor.tribes.io;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.dssim.types.TechState;
import de.tor.tribes.types.ext.Village;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This Class holds many functions for calculations with troops
 * 
 * @author extremeCrazyCoder
 */

public abstract class TroopAmount extends ManageableType implements Cloneable {
    private static final Logger logger = LogManager.getLogger("TroopAmount");
    
    public abstract void addAmount(TroopAmount pTroops);
    public abstract void removeAmount(TroopAmount pRemove);
    public abstract void multiplyWith(double factor);
    protected abstract int getInternalAmountForUnit(UnitHolder pUnit, Village pVillage);

    public int getOffValue(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getAttack() * amount;
            }
        }

        return result;
    }

    public int getRealOffValue(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if ((unit.getPlainName().equals("axe")
                    || unit.getPlainName().equals("light")
                    || unit.getPlainName().equals("marcher")
                    || unit.getPlainName().equals("heavy")
                    || unit.getPlainName().equals("ram")
                    || unit.getPlainName().equals("catapult")) && amount > 0) {
                result += unit.getAttack() * amount;
            }
        }

        return result;
    }

    public int getOffInfantryValue(Village pVillage, TechState pTech) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if (unit.isInfantry() && !unit.isArcher()) {
                result += unit.getAttack() * amount * (pTech == null ? 1 : pTech.getFactor(unit));
            }
        }

        return result;
    }

    public int getOffCavalryValue(Village pVillage, TechState pTech) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if (unit.isCavalry() && !unit.isArcher()) {
                result += unit.getAttack() * amount * (pTech == null ? 1 : pTech.getFactor(unit));
            }
        }

        return result;
    }

    public int getOffArcherValue(Village pVillage, TechState pTech) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if (unit.isArcher()) {
                result += unit.getAttack() * amount * (pTech == null ? 1 : pTech.getFactor(unit));
            }
        }

        return result;
    }

    public int getOffInfantryValue(Village pVillage) {
        return getOffInfantryValue(pVillage, null);
    }

    public int getOffCavalryValue(Village pVillage) {
        return getOffCavalryValue(pVillage, null);
    }

    public int getOffArcherValue(Village pVillage) {
        return getOffArcherValue(pVillage, null);
    }

    public int getDefInfantryValue(Village pVillage, TechState pTech) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getDefense() * amount * (pTech == null ? 1 : pTech.getFactor(unit));
            }
        }

        return result;
    }

    public int getDefArcherValue(Village pVillage, TechState pTech) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getDefenseArcher() * amount * (pTech == null ? 1 : pTech.getFactor(unit));
            }
        }

        return result;
    }

    public int getDefCavalryValue(Village pVillage, TechState pTech) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getDefenseCavalry() * amount * (pTech == null ? 1 : pTech.getFactor(unit));
            }
        }

        return result;
    }

    public int getDefInfantryValue(Village pVillage) {
        return getDefInfantryValue(pVillage, null);
    }

    public int getDefArcherValue(Village pVillage) {
        return getDefArcherValue(pVillage, null);
    }

    public int getDefCavalryValue(Village pVillage) {
        return getDefCavalryValue(pVillage, null);
    }

    public int getTroopPopCount(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getPop() * amount;
            }
        }

        return result;
    }

    public int getTroopWoodCost(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getWood() * amount;
            }
        }

        return result;
    }

    public int getTroopStoneCost(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getStone() * amount;
            }
        }

        return result;
    }

    public int getTroopIronCost(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getIron() * amount;
            }
        }

        return result;
    }
    
    /**
     * Can be used to get the Bash that an Attacker would get for bashing this many defenders
     */
    public int getTroopAttackerBash(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getBashAttacker() * amount;
            }
        }

        return result;
    }
    
    /**
     * Can be used to get the Bash that an Defender would get for bashing this many attackers
     */
    public int getTroopDefenderBash(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getBashDefender() * amount;
            }
        }

        return result;
    }

    /**
     * Just the sum of all Units
     * ignores entries with -1
     * @return 
     */
    public int getTroopSum(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += amount;
            }
        }

        return result;
    }
    
    public int getFarmCapacity(Village pVillage) {
        int result = 0;
        for (UnitHolder unit : DataHolder.getSingleton().getUnits()) {
            int amount = this.getInternalAmountForUnit(unit, pVillage);
            if(amount > 0) {
                result += unit.getCarry() * amount;
            }
        }

        return result;
    }
    
    /**
     * @param pVillage to send the troops from (only for dynamic)
     * @return Speed of slowest Unit
     */
    public double getSpeed(Village pVillage) {
        return getSlowestUnit(pVillage).getSpeed();
    }

    public UnitHolder getSlowestUnit(Village pVillage) {
        UnitHolder slowest = null;
        for(UnitHolder unit: this.getContainedUnits(pVillage)) {
            if(slowest == null || slowest.getSpeed() < unit.getSpeed()) {
                slowest = unit;
            }
        }
        return slowest;
    }
    
    public abstract List<UnitHolder> getContainedUnits();
    public abstract List<UnitHolder> getContainedUnits(Village pVillage);
}
