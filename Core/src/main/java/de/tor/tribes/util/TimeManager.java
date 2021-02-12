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

import de.tor.tribes.io.ServerManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author extremeCrazyCoder
 */
public class TimeManager {
    private static Logger logger = LogManager.getLogger("TimeManager");
    
    private static List<DateFormat> timeZoneListeners = new ArrayList<>();
    
    public static void updateTimeZone(String server) {
        TimeZone zone = ServerManager.getServerTimeZone(server);
        if(zone == null) return;
        logger.debug("Setting time zone to {}", zone.getDisplayName());
        TimeZone.setDefault(zone);
        
        for(DateFormat format : timeZoneListeners) {
            format.setTimeZone(zone);
        }
    }
    
    public static void register(DateFormat toRegister) {
        if(timeZoneListeners.contains(toRegister)) return;
        timeZoneListeners.add(toRegister);
    }
    
    public static void unregister(DateFormat toRegister) {
        if(! timeZoneListeners.contains(toRegister)) return;
        timeZoneListeners.add(toRegister);
    }
    
    /**
     * needed to create SimpleDateFomat with correct timeZone
     */
    public static SimpleDateFormat getSimpleDateFormat(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf;
    }
}