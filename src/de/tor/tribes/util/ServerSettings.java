/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util;

import de.tor.tribes.util.xml.JaxenUtils;
import java.io.File;
import org.apache.log4j.Logger;
import org.jdom.Document;

/**
 *
 * @author Charon
 */
public class ServerSettings {

    private static Logger logger = Logger.getLogger("ServerSettings");
    private String SERVER_ID = "de26";
    private int COORD = 2;
    private boolean MAP_NEW = true;
    private int BONUS_NEW = 0;
    private int SNOB_RANGE = 70;
    private static ServerSettings SINGLETON = null;

    public static synchronized ServerSettings getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ServerSettings();
        }
        return SINGLETON;
    }

    public boolean loadSettings(String pServerID) {
        try {
            setServerID(pServerID);
            String serverPath = Constants.SERVER_DIR + "/" + SERVER_ID + "/settings.xml";
            logger.debug("Parse server settings from '" + serverPath + "'");
            Document d = JaxenUtils.getDocument(new File(serverPath));
            logger.debug(" - reading map system");
            try {
                setCoordType(Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/sector")));
            } catch (Exception inner) {
                COORD = 2;
            }
            logger.debug(" - reading map type");
            try {
                setNewMap((Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/map_new")) == 1));
            } catch (Exception inner) {
                MAP_NEW = true;
            }
            logger.debug(" - reading bonus type");
            try {
                setNewBonus(Integer.parseInt(JaxenUtils.getNodeValue(d, "//coord/bonus_new")));
            } catch (Exception inner) {
                BONUS_NEW = 0;
            }
            logger.debug(" - reading snob distance");
            try {
                setSnobRange(Integer.parseInt(JaxenUtils.getNodeValue(d, "//snob/max_dist")));
            } catch (Exception inner) {
                SNOB_RANGE = 70;
            }
        } catch (Exception e) {
            logger.error("Failed to load server settings", e);
            return false;
        }
        logger.debug("Successfully read settings for server '" + SERVER_ID + "'");
        return true;
    }

    public void setServerID(String pServerID) {
        SERVER_ID = pServerID;
    }

    public String getServerID() {
        return SERVER_ID;
    }

    public void setCoordType(int pCoordType) {
        COORD = pCoordType;
    }

    public int getCoordType() {
        return COORD;
    }

    public void setNewMap(boolean pNewMap) {
        MAP_NEW = pNewMap;
    }

    public boolean isNewMap() {
        return MAP_NEW;
    }

    public void setNewBonus(int pNewBonus) {
        BONUS_NEW = pNewBonus;
    }

    public int getNewBonus() {
        return BONUS_NEW;
    }

    public void setSnobRange(int pSnobRange) {
        SNOB_RANGE = pSnobRange;
    }

    public int getSnobRange() {
        return SNOB_RANGE;
    }
}