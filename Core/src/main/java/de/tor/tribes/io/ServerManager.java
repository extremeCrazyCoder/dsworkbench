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

import de.tor.tribes.util.Constants;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lorecraft.phparser.SerializedPhpParser;

/**
 * @author Charon
 */
public class ServerManager {

    private static Logger logger = LogManager.getLogger("ServerManager");
    //Server id (e.g. de181) -> Pair(Server_URL, Timezone>
    private static Map<String, ImmutablePair<String, TimeZone>> SERVERS = null;

    private static boolean SERVERS_UPDATED = false;

    static {
        SERVERS = new HashMap<>();
        File serverDir = new File(Constants.SERVER_DIR);
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }
    }
    
    public static void loadServerList(Proxy pProxy) {
        SERVERS = new HashMap<>();
        logger.debug("Reading servers from servers.txt");

        BufferedReader r = null;
        try {
            logger.debug("Available Timezones {}", Arrays.asList(TimeZone.getAvailableIDs()));
            if (new File("servers.txt").exists()) {
                r = new BufferedReader(new FileReader("servers.txt"));
                int cnt = 0, regCnt = 0;
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.startsWith("#") || line.trim().length() <= 1) {//commented or empty line
                        continue;
                    }
                    String type = line.substring(0, 2);
                    String[] splited = line.substring(2).split(";");
                    if(type.equals("r\\")) {
                        //region
                        TimeZone zone = null;
                        for(String id : TimeZone.getAvailableIDs()) {
                            if(id.equals(splited[1]))
                                zone = TimeZone.getTimeZone(id);
                        }
                        if(zone == null) {
                            logger.error("Skipping region {} due to wrong time zone: {}",  splited[0], splited[1]);
                            continue;
                        }
                        
                        logger.debug("Loading servers for region {} with timezone {}", splited[0], splited[1]);
                        Map<String, String> serversRegion = loadServerList(splited[0], pProxy);
                        regCnt++;
                        
                        for (String id : serversRegion.keySet()) {
                            logger.debug("Adding server with id " + id + " and URL " + serversRegion.get(id));
                            SERVERS.put(id, new ImmutablePair(serversRegion.get(id), zone));
                            cnt++;
                        }
                    } else if(type.equals("s\\")) {
                        //single server
                        TimeZone zone = null;
                        for(String id : TimeZone.getAvailableIDs()) {
                            if(id.equals(splited[2]))
                                zone = TimeZone.getTimeZone(id);
                        }
                        if(zone == null) {
                            logger.error("Skipping server {} due to wrong time zone: {}",  splited[0], splited[2]);
                            continue;
                        }
                        
                        logger.debug("Adding server with id {} and URL {} and Timezone {}", splited[0], splited[1], splited[2]);
                        SERVERS.put(splited[0], new ImmutablePair(splited[1], zone));
                        cnt++;
                    }
                }
                logger.info("Read " + regCnt + " regions and " + cnt + " servers");
            }
        } catch (Throwable e) {
            logger.error("Failed to read servers. Skipping it.", e);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public static Map<String, String> loadServerList(String pServerBaseUrl, Proxy pProxy) throws Exception {
        URLConnection con;
        if (pProxy == null) {
            con = new URL(pServerBaseUrl + "/backend/get_servers.php").openConnection();
        } else {
            con = new URL(pServerBaseUrl + "/backend/get_servers.php").openConnection(pProxy);
        }
        InputStream isr = con.getInputStream();
        int bytes = 0;
        byte[] data = new byte[1024];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while ((bytes = isr.read(data)) != -1) {
            result.write(data, 0, bytes);
        }
        SerializedPhpParser serializedPhpParser = new SerializedPhpParser(result.toString());
        return (Map<String, String>) serializedPhpParser.parse();
    }

    /**
     * Get the listof locally stored servers
     */
    public static String[] getLocalServers() {
        List<String> servers = new ArrayList<>();
        for (File serverDir : new File(Constants.SERVER_DIR).listFiles()) {
            if (serverDir.isDirectory()) {
                servers.add(serverDir.getName());
            }
        }
        return servers.toArray(new String[0]);
    }
    
    public static String[] getServerIDs() {
        String[] servers = SERVERS.keySet().toArray(new String[]{});
        Arrays.sort(servers, String.CASE_INSENSITIVE_ORDER);
        return servers;
    }

    public static String getServerURL(String pServerID) {
        if(SERVERS.get(pServerID) == null) {
            return null;
        }
        return SERVERS.get(pServerID).getLeft();
    }

    public static TimeZone getServerTimeZone(String pServerID) {
        if(SERVERS.get(pServerID) == null) {
            return null;
        }
        return SERVERS.get(pServerID).getRight();
    }
}