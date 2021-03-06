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
package de.tor.tribes.util;

import de.tor.tribes.control.ManageableType;
import de.tor.tribes.dssim.algo.NewSimulator;
import de.tor.tribes.dssim.types.SimulatorResult;
import de.tor.tribes.dssim.types.TechState;
import de.tor.tribes.io.TroopAmountFixed;
import de.tor.tribes.types.DefenseInformation;
import de.tor.tribes.types.SOSRequest;
import de.tor.tribes.types.TargetInformation;
import de.tor.tribes.types.ext.Village;
import de.tor.tribes.util.sos.SOSManager;
import de.tor.tribes.util.troops.TroopsManager;
import de.tor.tribes.util.troops.VillageTroopsHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Torridity
 */
public class DefenseAnalyzer extends Thread {
    private static Logger logger = LogManager.getLogger("DefenseAnalyzer");

    private TroopAmountFixed standardOff = null;
    private TroopAmountFixed standardDefSplit = null;
    private int maxRuns = 0;
    private double maxLossRatio = 0;
    private boolean running = false;
    private boolean aborted = false;
    private boolean reAnalyze = false;
    private DefenseAnalyzerListener listener = null;

    public DefenseAnalyzer(DefenseAnalyzerListener pListener,
            TroopAmountFixed pStandardOff,
            TroopAmountFixed pStandardDefSplit,
            int pMaxRuns, double pMaxLossRatio, boolean pReAnalyze) {
        if (pListener == null) {
            throw new IllegalArgumentException("pListener must not be null");
        }
        setName("DefenseAnalyzer");
        listener = pListener;
        standardOff = pStandardOff;
        standardDefSplit = pStandardDefSplit;
        maxRuns = pMaxRuns;
        maxLossRatio = pMaxLossRatio;
        reAnalyze = pReAnalyze;
        setDaemon(true);
    }

    public interface DefenseAnalyzerListener {
        void fireProceedEvent(double pStatus);
        void fireFinishedEvent();
    }

    public boolean isRunning() {
        return running;
    }

    public void abort() {
        aborted = true;
    }

    @Override
    public void run() {
        running = true;
        logger.debug("Stating analyzing of Data");
        updateStatus();
        logger.debug("Finished analyzing of Data");
        running = false;
        listener.fireFinishedEvent();
    }

    private void updateStatus() {
        TechState maxTech = new TechState(TechState.MAXED);
        int targetCount = SOSManager.getSingleton().getOverallTargetCount();
        int currentTarget = 0;
        for (ManageableType e : SOSManager.getSingleton().getAllElements()) {
            SOSRequest request = (SOSRequest) e;
            logger.debug("Stating analyzing of Player: {}", request.getDefender());
            for(Village target: request.getTargets()) {
                logger.debug("Stating analyzing of Village: {}", target.getCoordAsString());
                if (aborted) {
                    return;
                }
                currentTarget++;
                listener.fireProceedEvent((double) currentTarget / (double) targetCount);
                TargetInformation targetInfo = request.getTargetInformation(target);
                DefenseInformation info = request.getDefenseInformation(target);
                int attCount = targetInfo.getOffs();
                boolean noAttack = (attCount == 0);
                
                try {
                    if (reAnalyze || !info.isAnalyzed()) {//re-analyze info
                        TroopAmountFixed off = standardOff;
                        TroopAmountFixed def = getDefense(targetInfo, info, info.getSupports().length);

                        int pop = def.getTroopPopCount();

                        NewSimulator sim = new NewSimulator();

                        SimulatorResult result = sim.calculate(off, def, maxTech, maxTech, false, 0, 100.0, 20, 0, 30, true, true, false, false);
                        int cleanAfter = 0;
                        for (int i = 1; i < attCount; i++) {
                            if (result.isWin()) {
                                cleanAfter = i + 1;
                                break;
                            }
                            result = sim.calculate(off, result.getSurvivingDef(), maxTech, maxTech, false, 0, 100.0, result.getWallLevel(), 0, 30, true, true, false, false);
                        }


                        double lossPercent = 0;
                        if (!noAttack) {
                            if (!result.isWin()) {
                                int survive = result.getSurvivingDef().getTroopPopCount();
                                lossPercent = 100 - (100.0 * survive / (double) pop);
                                if (Math.max(75.0, lossPercent) == lossPercent) {
                                    info.setDefenseStatus(DefenseInformation.DEFENSE_STATUS.DANGEROUS);
                                    info.setLossRation(lossPercent);
                                } else if (Math.max(30.0, lossPercent) == 30.0) {
                                    info.setDefenseStatus(DefenseInformation.DEFENSE_STATUS.SAVE);
                                    info.setLossRation(lossPercent);
                                } else {
                                    info.setDefenseStatus(DefenseInformation.DEFENSE_STATUS.FINE);
                                    info.setLossRation(lossPercent);
                                }
                            } else {
                                info.setDefenseStatus(DefenseInformation.DEFENSE_STATUS.DANGEROUS);
                                info.setLossRation(100.0);
                                info.setCleanAfter(cleanAfter);
                            }
                        } else {
                            info.setDefenseStatus(DefenseInformation.DEFENSE_STATUS.SAVE);
                            info.setLossRation(0.0);
                        }
                        calculateNeededSupports(info, targetInfo);
                        info.setAnalyzed(true);
                    }
                } catch(Exception except) {
                    logger.warn("Problems during simutlation", except);
                }
            }
        }
    }

    private TroopAmountFixed getDefense(TargetInformation pTargetInfo, DefenseInformation pInfo, int pAdditionalSplits) {
        int supportCount = pInfo.getSupports().length;
        TroopAmountFixed result;
        VillageTroopsHolder holder = TroopsManager.getSingleton().getTroopsForVillage(pTargetInfo.getTarget(), TroopsManager.TROOP_TYPE.IN_VILLAGE);
        if (holder != null) {
            result = holder.getTroops().clone();
        } else {
            result = pTargetInfo.getTroops().clone();
        }
        TroopAmountFixed stdDeffMul = standardDefSplit.clone();
        stdDeffMul.multiplyWith(supportCount + pAdditionalSplits);
        result.addAmount(stdDeffMul);
        return result;
    }

    private void calculateNeededSupports(DefenseInformation pInfo, TargetInformation pTargetInfo) {
        TechState maxTech = new TechState(TechState.MAXED);
        try {
            NewSimulator sim = new NewSimulator();
            int attCount = pTargetInfo.getOffs();
            //no atts for this target...don't know why...
            if (attCount == 0) {
                return;
            }

            int factor = pInfo.getSupports().length;
            SimulatorResult result = null;
            while (true) {
                if (aborted) {
                    return;
                }
                TroopAmountFixed off = standardOff;
                TroopAmountFixed def = getDefense(pTargetInfo, pInfo, factor);
                int troops = def.getTroopPopCount();

                result = sim.calculate(off, def, maxTech, maxTech, false, 0, 100.0, pTargetInfo.getWallLevel(), 0, 30, true, true, false, false);
                for (int i = 1; i < attCount; i++) {
                    if (result.isWin()) {
                        break;
                    }
                    result = sim.calculate(off, result.getSurvivingDef(), maxTech, maxTech, false, 0, 100.0, result.getWallLevel(), 0, 30, true, true, false, false);
                }

                int survive = result.getSurvivingDef().getTroopPopCount();
                double lossesPercent = 100 - (100.0 * ((double) survive) / troops);
                if (!result.isWin() && lossesPercent < maxLossRatio) {
                    pInfo.setNeededSupports(factor);
                    //  pInfo.setDefenseStatus(DefenseInformation.DEFENSE_STATUS.SAVE);
                    pInfo.setLossRation(lossesPercent);
                    break;
                } else {
                    factor++;
                }
                if (factor > maxRuns) {
                    if (lossesPercent < 100) {
                        pInfo.setNeededSupports(factor);
                        //  pInfo.setDefenseStatus(DefenseInformation.DEFENSE_STATUS.FINE);
                        pInfo.setLossRation(lossesPercent);
                    } else {
                        pInfo.setNeededSupports(factor);
                        // pInfo.setDefenseStatus(DefenseInformation.DEFENSE_STATUS.DANGEROUS);
                        pInfo.setLossRation(100.0);
                    }
                    //break due to max iterations
                    break;
                }
            }
        } catch(Exception e) {
            logger.warn("Problems during simulation", e);
        }
    }
}
