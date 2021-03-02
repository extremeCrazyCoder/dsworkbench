/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.types;

/**
 *
 * @author Charon
 */
public class AbstractUnitElement {

    private UnitHolder unit = null;
    private int count = 0;
    private int tech = 0;

    public AbstractUnitElement(UnitHolder pUnit, int pCount, int pTech){
        unit = pUnit;
        count = pCount;
        tech = pTech;
    }
    /**
     * @return the unit
     */
    public UnitHolder getUnit() {
        return unit;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(UnitHolder unit) {
        this.unit = unit;
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return the tech
     */
    public int getTech() {
        return tech;
    }

    /**
     * @param tech the tech to set
     */
    public void setTech(int tech) {
        this.tech = tech;
    }
}
