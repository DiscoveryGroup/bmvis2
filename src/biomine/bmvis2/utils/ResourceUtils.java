package biomine.bmvis2.utils;

import biomine.bmvis2.Logging;
import biomine.bmvis2.Vis;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceUtils {
    private static SVGUniverse svgUniverse = new SVGUniverse();
    private static Map<String, SVGDiagram> svgDiagrams = new HashMap<String, SVGDiagram>();

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path  Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     * @author Greg Briggs
     */
    public static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
            * In case of a jar file, we can't actually find a directory.
            * Have to assume the same jar as clazz.
            */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }

    public static void printResourceFiles(Class c, String path) {
        try {
            String[] files = ResourceUtils.getResourceListing(c, path);
            System.err.println("Resources from class " + c.getName() + ", path: \"" + path +"\":");
            for (String file : files) {
                System.err.println(" - " + file);
            }

        } catch (Exception e) {
            System.err.println("Couldn't list resources for \"" + path + "\".");
        }
    }

    public static SVGDiagram getSVGDiagramByPath(String path) throws Exception {
        if (!ResourceUtils.svgDiagrams.containsKey(path)) {
            URL url = Vis.class.getResource(path);
            URI uri = url.toURI();
            ResourceUtils.svgUniverse.loadSVG(url);
            ResourceUtils.svgDiagrams.put(path, ResourceUtils.svgUniverse.getDiagram(uri));
        }
        return ResourceUtils.svgDiagrams.get(path);
    }

}
