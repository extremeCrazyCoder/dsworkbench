/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.util;

import de.tor.tribes.dssim.types.EventListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.lorecraft.phparser.SerializedPhpParser;

/**
 *
 * @author Jejkal
 */
public class ConfigManager {
    
    private static Logger logger = LogManager.getLogger("SimConfigManager");

    public final static int ID_TECH_10 = 0;
    public final static int ID_TECH_3 = 1;
    public final static int ID_SIMPLE_TECH = 2;
    public final static int ID_NO_KNIGHT = 0;
    public final static int ID_KNIGHT_WITHOUT_ITEMS = 1;
    public final static int ID_KNIGHT_WITH_ITEMS = 2;
    private static ConfigManager SINGLETON = null;
    private int tech = 2;
    private int farmLimit = 0;
    private int knightType = 0;
    private int knightNewItems = 0;
    private int church = 0;
    private int spyType = 10;
    private Map<String, String> servers = new LinkedHashMap<>();
    private Proxy webProxy = Proxy.NO_PROXY;
    private List<EventListener> toNotify = new ArrayList<>();

    public static synchronized ConfigManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new ConfigManager();
        }
        return SINGLETON;
    }

    public void setWebPoxy(Proxy webProxy) {
        this.webProxy = webProxy;
    }

    public Proxy getWebProxy() {
        return webProxy;
    }

    public void loadServers() throws Exception {
        loadServers(webProxy);
    }

    public void loadServers(Proxy webProxy) throws Exception {
        URLConnection con = new URL("http://www.die-staemme.de/backend/get_servers.php").openConnection(webProxy);
        InputStream isr = con.getInputStream();
        int bytes = 0;
        byte[] data = new byte[1024];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while (bytes != -1) {
            if (bytes != -1) {
                result.write(data, 0, bytes);
            }

            bytes = isr.read(data);
        }
        SerializedPhpParser serializedPhpParser = new SerializedPhpParser(result.toString());
        Object obj = serializedPhpParser.parse();
        System.out.println("LOAD " + servers);
        servers = (LinkedHashMap<String, String>) obj;
        
        if(logger.isDebugEnabled()) {
            StringBuilder log = new StringBuilder();
            log.append("Found:");
            for(String s:servers.keySet()) {
                log.append(" ").append(s);
            }
            logger.debug(log);
        }
        
        fireAllEvents();
    }

    public String[] getServers() {
        return servers.keySet().toArray(new String[]{});
    }

    public void setServers(Map<String, String> pServers) {
        servers.clear();
        for (String id: pServers.keySet()) {
            servers.put(id, pServers.get(id));
        }
        fireAllEvents();
    }

    public String getServerURL(String pServerId) {
        return servers.get(pServerId);
    }

    public void parseConfig(String pServerID) throws Exception {
        try {
            URLConnection con = new URL(getServerURL(pServerID) + "/interface.php?func=get_config").openConnection();
            Document d = JDomUtils.getDocument(con.getInputStream());
            try {
                setTech(Integer.parseInt(JDomUtils.getNodeValue(d, "game/tech")));
            } catch (Exception ignore) {
            }
            try {
                setFarmLimit(Integer.parseInt(JDomUtils.getNodeValue(d, "game/farm_limit")));
            } catch (Exception ignore) {
            }
            try {
                setKnightType(Integer.parseInt(JDomUtils.getNodeValue(d, "game/knight")));
            } catch (Exception ignore) {
            }
            try {
                setKnightNewItems(Integer.parseInt(JDomUtils.getNodeValue(d, "game/knight_new_items")));
            } catch (Exception ignore) {
            }
            try {
                setChurch(Integer.parseInt(JDomUtils.getNodeValue(d, "game/church")));
            } catch (Exception ignore) {
            }
            try {
                setSpyType(Integer.parseInt(JDomUtils.getNodeValue(d, "game/spy")));
            } catch (Exception ignore) {
            }
        } catch (Exception outer) {
            throw new Exception("Failed to load config for server '" + pServerID + "'", outer);
        }
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

    /**
     * @return the knightNewItems
     */
    public int getKnightNewItems() {
        return knightNewItems;
    }

    /**
     * @param knightNewItems the knightNewItems to set
     */
    public void setKnightNewItems(int knightNewItems) {
        this.knightNewItems = knightNewItems;
    }

    /**
     * @return the knightType
     */
    public int getKnightType() {
        return knightType;
    }

    /**
     * @param knightType the knightType to set
     */
    public void setKnightType(int knightType) {
        this.knightType = knightType;
    }

    /**
     * @return the farmLimit
     */
    public int getFarmLimit() {
        return farmLimit;
    }

    /**
     * @param farmLimit the farmLimit to set
     */
    public void setFarmLimit(int farmLimit) {
        this.farmLimit = farmLimit;
    }

    /**
     * @return the church
     */
    public boolean isChurch() {
        return (church == 1);
    }

    /**
     * @param church the church to set
     */
    public void setChurch(int church) {
        this.church = church;
    }

    /**
     * @return the spyType
     */
    public int getSpyType() {
        return spyType;
    }

    /**
     * @param spyType the spyType to set
     */
    public void setSpyType(int spyType) {
        this.spyType = spyType;
    }

    public void addListener(EventListener pListener) {
        toNotify.add(pListener);
    }
    
    public void removeListener(EventListener pListener) {
        toNotify.remove(pListener);
    }
    
    public void clearListeners() {
        toNotify.clear();
    }
    
    private void fireAllEvents() {
        for (EventListener c : toNotify) {
            c.fireEvent();
        }
    }
}
