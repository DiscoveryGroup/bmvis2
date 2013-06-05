/*
 * Copyright 2012 University of Helsinki.
 *
 * This file is part of BMVis².
 *
 * BMVis² is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * BMVis² is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BMVis².  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package biomine.bmvis2;

import biomine.bmvis2.ui.JavaScriptConsole;

import java.util.*;

/**
 * @author ahinkka
 *         Categories: see allCats for all categories.
 */
public class Logging {
    public static final int LEVEL_DEBUG = 0;
    public static final int LEVEL_INFO = 1;
    public static final int LEVEL_WARNING = 2;
    public static final int LEVEL_ERROR = 3;

    private static int logLevel = -1;
    private static final String[] allCats = {
            "enduser", "ui", "js", "layout", "graph_reading",
            "graph_operation", "graph_drawing", "twophase", "pipeline",
            "kappasimplifier", "focus", "expand", "grouping", "test", "simplevisualgraph",
            "simplifier"};
    private static final Set<String> allCategories = new HashSet<String>(Arrays.asList(allCats));

    private static ArrayList<String> categories = null;

    private static String join(String delimiter, Object[] arr) {
        StringBuffer b = new StringBuffer();
        int iter = 0;
        while (true) {
            if (iter == arr.length)
                break;
            String s = arr[iter].toString();
            b.append(s);
            if (iter + 1 < arr.length)
                b.append(delimiter);
            iter++;
        }
        return b.toString();
    }

    static {
        Logging.categories = new ArrayList<String>(Arrays.asList("all"));
        System.err.println("INFO[logging] Level: " + Logging.logLevel);
        System.err.println("INFO[logging] Categories: " + join(", ", Logging.categories.toArray()));

    }

    public static void init(Collection<String> categories, Integer logLevel) {
        if (logLevel != null)
            Logging.logLevel = logLevel;
        if (categories != null)
            Logging.categories = new ArrayList<String>(categories);
        System.err.println("INFO[logging] Level: " + Logging.logLevel);
        System.err.println("INFO[logging] Categories: " + join(", ", Logging.categories.toArray()));
    }

    public static boolean log(int level, String category, String message) {
        if (Logging.logLevel > level)
            return false;
        if (!Logging.allCategories.contains(category)) {
            System.err.println("Unknown logging category: " + category);
            return false;
        }
        if (!Logging.categories.contains(category) && !Logging.categories.contains("all"))
            return false;

        if (category == null)
            category = "null";
        if (message == null)
            message = "null";

        if (level == LEVEL_DEBUG)
            System.err.println("DEBUG[" + category + "] " + message);
        else if (level == LEVEL_INFO)
            System.err.println("INFO[" + category + "] " + message);
        else if (level == LEVEL_WARNING)
            System.err.println("WARNING[" + category + "] " + message);
        else if (level == LEVEL_ERROR)
            System.err.println("ERROR[" + category + "] " + message);
        else
            return false;
        return true;
    }

    public static boolean debug(String category, String message) {
        return Logging.log(Logging.LEVEL_DEBUG, category, message);
    }

    public static boolean info(String category, String message) {
        return Logging.log(Logging.LEVEL_INFO, category, message);
    }

    public static boolean warning(String category, String message) {
        return Logging.log(Logging.LEVEL_WARNING, category, message);
    }

    public static boolean error(String category, String message) {
        return Logging.log(Logging.LEVEL_ERROR, category, message);
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

}
