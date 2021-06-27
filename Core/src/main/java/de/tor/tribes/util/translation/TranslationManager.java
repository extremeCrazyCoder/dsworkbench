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
package de.tor.tribes.util.translation;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author extremeCrazyCoder
 */
public class TranslationManager {

    private static Logger logger = LogManager.getLogger("TranslationManager");
    private static TranslationManager SINGLETON = null;
    public static final String DEFAULT_LANGUAGE = "Deutsch";
    private static boolean workbenchBoot = false;

    public static void setWorkbenchBoot(boolean b) {
        workbenchBoot = b;
    }

    private PropertiesConfiguration language;
    
    public static synchronized TranslationManager getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new TranslationManager();
        }
        return SINGLETON;
    }
    
    private TranslationManager() {
    }
    
    public static Translator getTranslator(String pNamespace) {
        return new Translator(getSingleton(), pNamespace);
    }
    
    public void setLanguage(String newLanguage) {
        try {
            int newLang = findLanguageIndex(newLanguage);
            File file = new File("./templates/UI Lang/" + fileMapping[newLang]);
            language = new PropertiesConfiguration();
            language.read(new FileReader(file));
        } catch (IOException | ConfigurationException ex) {
            if(!newLanguage.equals(DEFAULT_LANGUAGE)) {
                logger.error("Can't read Translations falling back to Default ({})", DEFAULT_LANGUAGE, ex);
                setLanguage(DEFAULT_LANGUAGE);
            } else {
                logger.fatal("Can't read Translations for default {}", newLanguage, ex);
                throw new RuntimeException(ex);
            }
        }
        
        String locale = getString("locale");
        Locale all[] = java.util.Locale.getAvailableLocales();
        boolean found = false;
        for(Locale cur : all) {
            logger.debug("Running through locales {}", cur.getLanguage());
            if(cur.getLanguage().equals(locale)) {
                logger.debug("Found {}", cur);
                java.util.Locale.setDefault(cur);
                found = true;
                break;
            }
        }
        
        if(!found) {
            throw new RuntimeException("Locale not found");
        }
    }
    
    public String getString(String key) {
        if(language == null) {
            if(workbenchBoot) {
                throw new RuntimeException("Cannot read variables before a language has been set");
            } else {
                return "";
            }
        }
        //logger.trace("Fetching {}", key);
        Object obj = language.getProperty(key);
        if(obj instanceof String) return (String) obj;
        if(obj == null) {
            logger.fatal("'" + key + "' existiert nicht");
            throw new RuntimeException("'" + key + "' existiert nicht");
        }
        return obj.toString();
    }
    
    private static String[] languages = null;
    private static String[] fileMapping = null;
    private static String[] icons = null;
    public static String[] getLanguages() {
        if(languages == null) {
            buldLanguageCache();
            
        }
        return languages;
    }
    
    public static String[] getLanguageIcons() {
        if(icons == null) {
            buldLanguageCache();
            
        }
        return icons;
    }
    
    public static void buldLanguageCache() {
        List<String> languages = new ArrayList<>();
        List<String> icons = new ArrayList<>();
        List<String> fileMapping = new ArrayList<>();
        File parent = new File("./templates/UI Lang/");
        for(String filename : parent.list()) {
            if(filename.endsWith(".lang")) {
                String langPart = filename.substring(0, filename.length() - 5);
                String[] splited = langPart.split("_", 2);
                if(splited.length != 2) {
                    throw new IllegalArgumentException(".lang file without _ seperating icon vs name\n" + filename);
                }
                
                languages.add(splited[1]);
                icons.add(splited[0]);
                fileMapping.add(filename);
            }
        }
        TranslationManager.languages =  languages.toArray(new String[languages.size()]);
        TranslationManager.icons =  icons.toArray(new String[icons.size()]);
        TranslationManager.fileMapping =  fileMapping.toArray(new String[fileMapping.size()]);
    }

    public static int findLanguageIndex(String targetLang) {
        String[] langs = getLanguages();
        
        for(int i = 0; i < langs.length; i++) {
            if(langs[i].equals(targetLang)) {
                return i;
            }
        }
        return -1;
    }
}
