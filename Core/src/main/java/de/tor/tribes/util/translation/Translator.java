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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author extremeCrazyCoder
 */
public class Translator {
    private static Logger logger = LogManager.getLogger("Translator");
    
    private TranslationManager manager;
    private String namepace;

    Translator(TranslationManager pManager, String pNamespace) {
        namepace = pNamespace;
        manager = pManager;
    }
    
    /**
     * Returns the translation of the given variable for the configured namespace
     */
    public String get(String variable) {
        return manager.getString(namepace + "." + variable);
    }
    
    /**
     * Returns the translation of the given variable without namespace
     */
    public String getRaw(String variable) {
        return manager.getString(variable);
    }
}
