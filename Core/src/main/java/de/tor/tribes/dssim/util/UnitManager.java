/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.util;

import de.tor.tribes.dssim.types.UnitHolder;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 *
 * @author Charon
 */
public class UnitManager {
    private static Logger logger = LogManager.getLogger("SimUnitManager");

    private static List<UnitHolder> units = new LinkedList<>();
    private static UnitManager SINGLETON = null;

    public static synchronized UnitManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new UnitManager();
        }
        return SINGLETON;
    }

    /**
     * Parse the list of units
     */
    public void parseUnits(String pServerID) throws Exception {
        units.clear();
        try {
            // Document d = JaxenUtils.getDocument(UnitManager.class.getResourceAsStream("/res/servers/units_" + pServerID + ".xml"));
            URLConnection con = new URL(ConfigManager.getSingleton().getServerURL(pServerID) + "/interface.php?func=get_unit_info").openConnection();
            Document d = JDomUtils.getDocument(con.getInputStream());
            List<Element> l = JDomUtils.getNodes(d, null);
            for (Element e : l) {
                try {
                    units.add(new UnitHolder(e));
                } catch (Exception inner) {
                    inner.printStackTrace();
                }
            }
        } catch (Exception outer) {
            throw new Exception("Failed to load units for server '" + pServerID + "'", outer);
        }
    }

    public void setUnits(String pSettingsPath) throws Exception {
        units.clear();
        try {
            Document d = JDomUtils.getDocument(new File(pSettingsPath));
            List<Element> l = JDomUtils.getNodes(d, null);
            for (Element e : l) {
                try {
                    units.add(new UnitHolder(e));
                } catch (Exception inner) {
                    inner.printStackTrace();
                }
            }
        } catch (Exception outer) {
            throw new Exception("Failed to load units from file " + pSettingsPath, outer);
        }
    }

    /**
     * Get a unit by its name
     */
    public UnitHolder getUnitByPlainName(String pName) {
        for (UnitHolder u : units) {
            if (u.getPlainName().equals(pName)) {
                return u;
            }
        }
        return null;
    }

    public UnitHolder[] getUnits() {
        return units.toArray(new UnitHolder[]{});
    }
}
