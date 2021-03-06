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

import de.tor.tribes.util.ServerSettings;
import de.tor.tribes.util.translation.TranslationManager;
import de.tor.tribes.util.translation.Translator;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

/**
 *
 * @author Charon
 */
public class UnitHolder implements Serializable {
    private static Logger logger = LogManager.getLogger("UnitHolder");
    private Translator trans = TranslationManager.getTranslator("io.UnitHolder");
    
    private static final Map<String, Triple<Integer, Integer, Integer>> troopRes = new HashMap<>();
    private static final Map<String, Pair<Integer, Integer>> kill_old = new HashMap<>();
    private static final Map<String, Pair<Integer, Integer>> kill_new = new HashMap<>();
    static {
        troopRes.put("spear", new ImmutableTriple<>(50, 30, 10));
        troopRes.put("sword", new ImmutableTriple<>(30, 30, 70));
        troopRes.put("axe", new ImmutableTriple<>(60, 30, 40));
        troopRes.put("archer", new ImmutableTriple<>(100, 30, 60));
        troopRes.put("spy", new ImmutableTriple<>(50, 50, 20));
        troopRes.put("light", new ImmutableTriple<>(125, 100, 250));
        troopRes.put("marcher", new ImmutableTriple<>(250, 100, 150));
        troopRes.put("heavy", new ImmutableTriple<>(200, 150, 600));
        troopRes.put("ram", new ImmutableTriple<>(300, 200, 200));
        troopRes.put("catapult", new ImmutableTriple<>(320, 400, 100));
        troopRes.put("knight", new ImmutableTriple<>(20, 20, 40));
        troopRes.put("snob", new ImmutableTriple<>(40000, 50000, 50000));
        troopRes.put("milita", new ImmutableTriple<>(0, 0, 0));
        troopRes.put("unknown", new ImmutableTriple<>(0, 0, 0));
        
        kill_old.put("spear", new ImmutablePair<>(1, 1));
        kill_old.put("sword", new ImmutablePair<>(1, 1));
        kill_old.put("axe", new ImmutablePair<>(1, 1));
        kill_old.put("archer", new ImmutablePair<>(1, 1));
        kill_old.put("spy", new ImmutablePair<>(2, 2));
        kill_old.put("light", new ImmutablePair<>(4, 4));
        kill_old.put("marcher", new ImmutablePair<>(5, 5));
        kill_old.put("heavy", new ImmutablePair<>(6, 6));
        kill_old.put("ram", new ImmutablePair<>(5, 5));
        kill_old.put("catapult", new ImmutablePair<>(8, 8));
        kill_old.put("knight", new ImmutablePair<>(10, 10));
        kill_old.put("snob", new ImmutablePair<>(100, 100));
        kill_old.put("milita", new ImmutablePair<>(0, 0));
        kill_old.put("unknown", new ImmutablePair<>(0, 0));
        
        kill_new.put("spear", new ImmutablePair<>(4, 1));
        kill_new.put("sword", new ImmutablePair<>(5, 2));
        kill_new.put("axe", new ImmutablePair<>(1, 4));
        kill_new.put("archer", new ImmutablePair<>(5, 2));
        kill_new.put("spy", new ImmutablePair<>(1, 2));
        kill_new.put("light", new ImmutablePair<>(5, 13));
        kill_new.put("marcher", new ImmutablePair<>(6, 12));
        kill_new.put("heavy", new ImmutablePair<>(23, 15));
        kill_new.put("ram", new ImmutablePair<>(4, 8));
        kill_new.put("catapult", new ImmutablePair<>(12, 10));
        kill_new.put("knight", new ImmutablePair<>(40, 20));
        kill_new.put("snob", new ImmutablePair<>(200, 200));
        kill_new.put("milita", new ImmutablePair<>(4, 0));
        kill_new.put("unknown", new ImmutablePair<>(0, 0));
    }
    
    public static final Comparator<UnitHolder> RUNTIME_COMPARATOR = new RuntimeComparator();
    private static final long serialVersionUID = 10L;
    private String plainName = null;
    private String name = null;
    private double wood = 0;
    private double stone = 0;
    private double iron = 0;
    private double pop = 0;
    private double speed = 0;
    private double attack = 0;
    private double defense = 0;
    private double defenseCavalry = 0;
    private double defenseArcher = 0;
    private double carry = 0;
    private double buildTime = 0;
    private int bashOff = 0;
    private int bashDeff = 0;

    public UnitHolder() {
        name = "";
        plainName = "";
    }

    public UnitHolder(Element pElement) throws Exception {
        try {
            setPlainName(pElement.getName());
            this.pop = Double.parseDouble(pElement.getChild("pop").getText());
            this.speed = Double.parseDouble(pElement.getChild("speed").getText());
            this.attack = Double.parseDouble(pElement.getChild("attack").getText());
            this.defense = Double.parseDouble(pElement.getChild("defense").getText());
            this.defenseCavalry = Double.parseDouble(pElement.getChild("defense_cavalry").getText());
            this.defenseArcher = Double.parseDouble(pElement.getChild("defense_archer").getText());
            this.carry = Double.parseDouble(pElement.getChild("carry").getText());
            this.buildTime = Double.parseDouble(pElement.getChild("build_time").getText());
            Triple<Integer, Integer, Integer> resCost = troopRes.get(this.plainName);
            if(resCost != null) {
                this.wood = resCost.getLeft();
                this.stone = resCost.getMiddle();
                this.iron = resCost.getRight();
            } else {
                logger.error("No Troop res found for {}!", this.plainName);
            }
            
            Map<String, Pair<Integer, Integer>> effBash = effectiveBash();
            if(effBash != null) {
                Pair<Integer, Integer> bash = effBash.get(this.plainName);
                if(resCost != null) {
                    this.bashOff = bash.getLeft();
                    this.bashDeff = bash.getRight();
                } else {
                    logger.error("No Troop bash found for {}!", this.plainName);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error during loading of unit '" + pElement.getName() + "'", e);
        }
    }
    
    protected void setPlainName(String pPlainName) {
        this.plainName = pPlainName;
        try {
            this.name = trans.get(pPlainName);
        } catch (Exception e) {
            logger.error("Unit not found", e);
            this.name = trans.get("unknown") + " (" + pPlainName + ")";
        }
    }

    public String getPlainName() {
        return plainName;
    }

    public String getName() {
        return name;
    }

    public double getWood() {
        return wood;
    }

    public double getStone() {
        return stone;
    }

    public double getIron() {
        return iron;
    }

    public double getPop() {
        return pop;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAttack() {
        return attack;
    }

    public double getDefense() {
        return defense;
    }

    public double getDefenseCavalry() {
        return defenseCavalry;
    }

    public double getDefenseArcher() {
        return defenseArcher;
    }

    public double getCarry() {
        return carry;
    }

    public int getBashAttacker() {
        return bashOff;
    }

    public int getBashDefender() {
        return bashDeff;
    }

    public boolean isFarmUnit() {
        return carry > 0 || (getPlainName() != null && getPlainName().equals("spy"));
    }

    public boolean isInfantry() {
        String plain = getPlainName();
        return plain != null && (plain.equals("spear") || plain.equals("sword") || plain.equals("archer") || plain.equals("axe"));
    }
    
    public boolean isArcher() {
        String plain = getPlainName();
        return plain != null && (plain.equals("archer") || plain.equals("marcher"));
    }

    public boolean isCavalry() {
        String plain = getPlainName();
        return plain != null && (plain.equals("spy") || plain.equals("light") || plain.equals("marcher") || plain.equals("heavy"));
    }

    public boolean isOther() {
        return !isInfantry() && !isCavalry();
    }

    public boolean isDefense() {
        String plain = getPlainName();
        return plain != null && (plain.equals("spear") || plain.equals("sword") || plain.equals("archer") || plain.equals("spy") || plain.equals("heavy") || plain.equals("catapult") || plain.equals("knight"));
    }

    public boolean isOffense() {
        String plain = getPlainName();
        return plain != null && (plain.equals("axe") || plain.equals("spy") || plain.equals("light") || plain.equals("marcher") || plain.equals("ram") || plain.equals("catapult"));
    }

    public boolean isSpecial() {
        return (!isDefense() && !isOffense());
    }

    public boolean isSpy() {
        String plain = getPlainName();
        return plain != null && plain.equals("spy");
    }

    public boolean isSnob() {
        String plain = getPlainName();
        return plain != null && plain.equals("snob");
    }

    public boolean isRetimeUnit() {
        String plain = getPlainName();
        return plain != null && !plain.equals("spy") && !plain.equals("snob") && !plain.equals("militia");
    }

    public double getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(double buildTime) {
        this.buildTime = buildTime;
    }

    public String toBBCode() {
        return "[unit]" + getPlainName() + "[/unit]";
    }

    @Override
    public String toString() {
        return getName();// + "(" + getSpeed() + " Minuten/Feld)";
    }
    
    @Override
    public boolean equals(Object other) {
        if(other instanceof UnitHolder) {
            UnitHolder otherU = (UnitHolder) other;
            return hashCode() == other.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.plainName);
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.pop) ^ (Double.doubleToLongBits(this.pop) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.speed) ^ (Double.doubleToLongBits(this.speed) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.attack) ^ (Double.doubleToLongBits(this.attack) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.defense) ^ (Double.doubleToLongBits(this.defense) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.defenseCavalry) ^ (Double.doubleToLongBits(this.defenseCavalry) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.defenseArcher) ^ (Double.doubleToLongBits(this.defenseArcher) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.carry) ^ (Double.doubleToLongBits(this.carry) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.buildTime) ^ (Double.doubleToLongBits(this.buildTime) >>> 32));
        return hash;
    }

    private static class RuntimeComparator implements Comparator<UnitHolder>, java.io.Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability

        private static final long serialVersionUID = 8575799808933029326L;

        @Override
        public int compare(UnitHolder s1, UnitHolder s2) {
            return new Double(s1.getSpeed()).compareTo(s2.getSpeed());
        }
    }
    
    private Map<String, Pair<Integer, Integer>> effectiveBash() {
        if(ServerSettings.getSingleton().getKillRanking() == ServerSettings.OLD_KILL_RANKING) {
            return kill_old;
        }
        if(ServerSettings.getSingleton().getKillRanking() == ServerSettings.NEW_KILL_RANKING) {
            return kill_new;
        }
        return null;
    }
}
