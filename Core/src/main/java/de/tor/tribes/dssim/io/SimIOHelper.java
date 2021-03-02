/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.dssim.io;

import de.tor.tribes.dssim.types.AbstractUnitElement;
import de.tor.tribes.dssim.types.UnitHolder;
import de.tor.tribes.dssim.util.JDomUtils;
import de.tor.tribes.dssim.util.UnitManager;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 *
 * @author Charon
 */
public class SimIOHelper {
    private static final Logger logger = LogManager.getLogger("SimIOHelper");

    static {
        String userDir = System.getProperty("user.home");
        if (!new File(userDir + "/.astar").exists()) {
            new File(userDir + "/.astar").mkdir();
        }
    }

    public static String getDataDir() {
        return System.getProperty("user.home") + "/.astar";
    }

    public static List<String> getOffSetups() {
        List<String> result = new LinkedList<>();
        File[] files = new File(getDataDir()).listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("_off.xml");
            }
        });

        for (File f : files) {
            result.add(f.getName().replaceAll("_off.xml", ""));
        }

        return result;
    }

    public static List<String> getDefSetups() {
        List<String> result = new LinkedList<>();
        File[] files = new File(getDataDir()).listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("_def.xml");
            }
        });

        for (File f : files) {
            result.add(f.getName().replaceAll("_def.xml", ""));
        }

        return result;
    }

    public static void writeTroopSetup(List<AbstractUnitElement> pTroops, String pFile) throws Exception {
        logger.debug("Starting saving");
        
        Document doc = JDomUtils.createDocument();
        Element root = doc.getRootElement();
        
        for (AbstractUnitElement elem : pTroops) {
            Element unit = new Element("unit");
            unit.setAttribute(new Attribute("name", elem.getUnit().getPlainName()));
            unit.setAttribute(new Attribute("tech", Integer.toString(elem.getTech())));
            unit.setAttribute(new Attribute("count", Integer.toString(elem.getCount())));
            root.addContent(unit);
        }
        
        logger.debug("Writing file {}", pFile);
        JDomUtils.saveDocument(doc, pFile);
        logger.debug("Finished");
    }

    public static List<AbstractUnitElement> readTroopSetup(String pFile) throws Exception {
        List<AbstractUnitElement> result = new LinkedList<>();

        Document doc = JDomUtils.getDocument(new File(pFile));
        for (Element unit : (List<Element>) JDomUtils.getNodes(doc, "unit")) {
            try {
                String name = unit.getAttribute("name").getValue();
                UnitHolder unitHolder = UnitManager.getSingleton().getUnitByPlainName(name);
                if (unitHolder != null) {
                    int tech = unit.getAttribute("tech").getIntValue();
                    int count = unit.getAttribute("count").getIntValue();
                    result.add(new AbstractUnitElement(unitHolder, count, tech));
                } else {
                    //unit not available on selected server
                }
            } catch (Exception e) {
                //failed to read unit
            }
        }
        return result;
    }
}
