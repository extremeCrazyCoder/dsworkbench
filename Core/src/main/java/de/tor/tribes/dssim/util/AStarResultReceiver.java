/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.util;

import java.awt.Point;

/**
 *
 * @author Torridity
 */
public interface AStarResultReceiver {

    public void fireNotifyOnResultEvent (Point pTarget, int pOffAmount);
}
