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
package de.tor.tribes.util.parser;

import de.tor.tribes.util.GlobalOptions;
import de.tor.tribes.util.SilentParserInterface;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author extremeCrazyCoder
 */
public class HQParser implements SilentParserInterface {
    private static Logger logger = LogManager.getLogger("HQParser");

    private static final String targetStr = "https://mkich.ds-ultimate.de/tools/datacollectionHQ/post";
    
    @Override
    public boolean parse(String pData) {
        HttpURLConnection connection = null;
        pData = "data=" + URLEncoder.encode(pData);
        
        try {
            URL targetURL = new URL(targetStr + "/" + GlobalOptions.getSelectedServer());
            //Create connection
            connection = (HttpURLConnection) targetURL.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", 
                "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", 
                Integer.toString(pData.getBytes().length));
            connection.setRequestProperty("Content-Language", "de-DE");  

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                connection.getOutputStream());
            
            wr.writeBytes(pData);
            wr.close();

            //Get Response  
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            boolean firstLine = true;
            while ((line = rd.readLine()) != null) {
                if(!firstLine)
                    response.append('\r');
                
                firstLine = false;
                response.append(line);
            }
            rd.close();
            
            if(response.toString().equals("1")) {
                return true;
            }
            if(response.toString().equals("0")) {
                return false;
            }
            logger.error("Parser code: " + response);
        } catch (Exception e) {
            logger.warn("Unable to send Report", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }
}
