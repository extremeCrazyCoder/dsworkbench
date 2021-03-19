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
package de.tor.tribes.types;

import de.tor.tribes.util.translation.TranslationManager;
import de.tor.tribes.util.translation.Translator;
import java.awt.geom.Ellipse2D;

/**
 *
 * @author extremeCrazyCoder
 */
public enum Layer {
    
    MARKERS,
    VILLAGES,
    VILLAGE_SYMBOLS,
    TROOP_DENSITY,
    NOTES_MARKER,
    ATTACKS,
    SUPPORTS,
    DRAWINGS,
    CHURCH_RADIUS,
    WATCHTOWER_RADIUS;

    private static Translator trans = TranslationManager.getTranslator("types.Layer");
    
    private Ellipse2D dragEllipse = null;

    private Layer()  {
    }

    public Ellipse2D getDragEllipse() {
        return dragEllipse;
    }

    public void setDragEllipse(Ellipse2D pEl) {
        dragEllipse = pEl;
    }

    public String getName() {
        return trans.get(this.toString());
    }
    
    public static String getDefaultLayerString() {
        String res = "";
        for (Layer l : values()) {
            res += l.toString() + ";";
        }
        return res;
    }
}
