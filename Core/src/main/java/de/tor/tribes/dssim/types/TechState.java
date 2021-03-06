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

import de.tor.tribes.io.DataHolder;
import de.tor.tribes.io.UnitHolder;
import de.tor.tribes.util.ServerSettings;
import java.util.HashMap;

/**
 *
 * @author extremeCrazyCoder
 */
public class TechState {
    public static final int MAXED = -2;
    HashMap<UnitHolder, Integer> techLevels;
    
    public TechState() {
        this(0);
    }
    
    public TechState(int pTechLevel) {
        if(pTechLevel == MAXED) {
            if (ServerSettings.getSingleton().getTechType() == ServerSettings.TECH_3) {
                pTechLevel = 3;
            } else if (ServerSettings.getSingleton().getTechType() == ServerSettings.TECH_10) {
                pTechLevel = 10;
            } else {
                pTechLevel = 1;
            }
        }
        techLevels = new HashMap<>();
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            techLevels.put(unit, pTechLevel);
        }
    }
    
    public void fill(int pTechLevel) {
        for(UnitHolder unit: DataHolder.getSingleton().getUnits()) {
            techLevels.put(unit, pTechLevel);
        }
    }

    public void setTechLevel(UnitHolder pUnit, int pTechLevel) {
        techLevels.put(pUnit, pTechLevel);
    }

    public int getTechLevel(UnitHolder pUnit) {
        Integer ret = techLevels.get(pUnit);
        if(ret == null) {
            return 0;
        }
        return ret;
    }

    public double getFactor(UnitHolder unit) {
        Integer techLevel = techLevels.get(unit);
        if(techLevel == null) techLevel = 0;
        return getTechFactor(techLevel);
    }

    /**Get the factors for the different tech levels*/
    private double getTechFactor(int pLevel) {
        if (ServerSettings.getSingleton().getTechType() == ServerSettings.TECH_3) {
            switch (pLevel) {
                case 2:
                    return 1.25;
                case 3:
                    return 1.4;
                default:
                    return 1;
            }
        } else if (ServerSettings.getSingleton().getTechType() == ServerSettings.TECH_10) {
            //use 10 level tech factor
            return Math.pow(1.04608, (pLevel - 1));
        }
        //for simple tech servers
        return 1.0;
    }
}
