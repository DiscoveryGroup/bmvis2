package biomine.bmvis2;

import java.applet.AppletContext;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.pipeline.sources.FileGraphSource;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;
import biomine.bmgraph.BMNode;
import biomine.bmgraph.DatabaseLinks;
import biomine.bmvis2.VisualGraph.GraphReadingException;
import biomine.bmvis2.ui.GraphTab;
import biomine.bmvis2.ui.JavaScriptConsole;
import biomine.bmvis2.ui.Menus;
import biomine.bmvis2.ui.TabbedPaneHiddenBar;
import biomine.bmvis2.utils.StreamToString;

/**
 * Program entry point.
 *
 * @author alhartik
 * @author ahinkka
 */
public class Vis extends JApplet {
    // http://launch4j.sourceforge.net/
    // http://stackoverflow.com/questions/1204580/swing-application-drag-drop-to-the-desktop-folder

    public static final String PROGRAM_NAME = "bmvis2";
    public static String lastOpenedPath = null;
    private TabbedPaneHiddenBar tabs;
    private VisFrame visFrame;
    private static AppletContext appletContext;

    private static String[] args;
    private static HashMap<String, Integer> titleIndex = null;

    private HashMap<String, String> properties = new HashMap<String, String>();


    private FocusListener focusListener = new FocusListener() {
        public void focusGained(FocusEvent focusEvent) {
            Logging.debug("focus", Vis.this.getClass() + " gained focus!");
        }

        public void focusLost(FocusEvent focusEvent) {
            Logging.debug("focus", Vis.this.getClass() + " lost focus!");
        }
    };

    @Override
    public String[][] getParameterInfo() {
        String[][] ret = {{"simple", "true/false",
                "enable simple interface, default is true for applets, false otherwise"}};
        return ret;
    }

    public String getProperty(String key) {
        if (properties.containsKey(key))
            return properties.get(key);
        if (isApplet())
            return getParameter(key);
        return null;
    }

    public boolean useSimpleUI() {
        String simple = getProperty("simple");
        boolean ret = false;
        if (simple != null && simple.equals("true")) {
            ret = true;
            Logging.info("ui", "Using simple ui.");
        }
        return ret;
    }

    public int getNumberOfTabs() {
        return tabs.getComponentCount();
    }

    public void viewInBrowser(BMNode node) {
        if (node == null)
            return;

        String url = node.get(BMGraphAttributes.URL_KEY);

        if (url == null) {
            url = DatabaseLinks.getURL(node.getType(), node.splitId()[0], node
                    .splitId()[1]);
        }
        if (url != null)
            openURL(url);
    }

    private void openURL(String url) {
        if (url == null)
            return;
        if (appletContext != null) {
            try {
                appletContext.showDocument(new URL(url), "_blank");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to open URL: "
                        + url + "\n" + e.getMessage(), "Error opening URL",
                        JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        String os = System.getProperty("os.name");
        if (os.startsWith("Mac OS")) {
            try {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[]{String.class});
                if (openURL != null)
                    openURL.invoke(null);
            } catch (Exception e) {
                Logging.error("ui", "Error opening URL: " + e.getMessage());
            }
            return;
        }
        if (os.startsWith("Windows")) {
            try {
                Runtime.getRuntime().exec(
                        "rundll32 url.dll,FileProtocolHandler " + url);
            } catch (Exception e) {
                Logging.error("ui", "Error opening URL: " + e.getMessage());
            }
            return;
        }
        // DEBUG: Assuming Firefox for everything not OS X or Windows
        try {
            Runtime.getRuntime().exec(
                    new String[]{"firefox", "-remote",
                            "openurl(" + url + ", new-tab)"});
        } catch (Exception e) {
            Logging.error("ui", "Error opening URL: " + e.getMessage());
        }
    }

    VisualGraph getGraphFromFile(String file) throws FileNotFoundException,
            GraphReadingException {
        return new VisualGraph(file);
    }

    public JFrame getFrame() {
        return visFrame;
    }

    protected class VisFrame extends JFrame {
        public VisFrame(String title) {
            super(title);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            int winWidth = 800;
            int winHeight = 600;

            // Determine proper window size
            try {
                GraphicsDevice dev = GraphicsEnvironment
                        .getLocalGraphicsEnvironment().getDefaultScreenDevice();

                DisplayMode m = dev.getDisplayMode();
                int displayWidth = m.getWidth();
                int displayHeight = m.getHeight();

                winWidth = displayWidth - 300;
                winHeight = displayHeight - 200;
            } catch (Exception e) {
            }
            if (winWidth < 800 || winHeight < 600)
                setSize(800, 600);
            else
                this.setSize(winWidth, winHeight);

            // Note to self and future developers: before adding content into the frame it shouldn't be set visible!
            // this.setVisible(true);
        }
    }

    public boolean isApplet() {
        if (this.appletContext != null)
            return true;
        return false;
    }

    public JMenuBar getDefaultMenu() {
        JMenuBar bar = new JMenuBar();
        Menus m = Menus.getDefaultMenuInstance(this);
        m.buildMenuBar(bar, new ArrayList<JavaScriptConsole>());
        return bar;
    }

    private static String assignTabTitle(String fileName) {
        String[] parts = fileName.split(Pattern.quote(System
                .getProperty("file.separator")));
        String baseName = parts[parts.length - 1];

        if (Vis.titleIndex == null)
            Vis.titleIndex = new HashMap<String, Integer>();

        if (!Vis.titleIndex.containsKey(baseName))
            Vis.titleIndex.put(baseName, 1);

        int index = Vis.titleIndex.get(baseName);

        if (index == 1) {
            Vis.titleIndex.put(baseName, index + 1);
            return baseName;
        }

        Vis.titleIndex.put(baseName, index + 1);
        return baseName + " [" + index + "]";
    }

    public GraphTab openEmptyTab(String name) {
        GraphTab newTab = new GraphTab(this);
        this.tabs.addTab(assignTabTitle(name), newTab);
        this.tabs.setSelectedComponent(newTab);
        this.setJMenuBar(newTab.getMenuBar());
        this.updateMenuBars();

        return newTab;
    }

    public void openJSONTab(final String json, final String title) {
        GraphTab nt = openEmptyTab(title);
        nt.getPipeline().loadOperations(json);
    }

    @Override
    public void setJMenuBar(JMenuBar bar) {
        if (useSimpleUI())
            return;
        super.setJMenuBar(bar);
    }

    public GraphTab openTab(String file) {
        try {
            Logging.info("graph_reading", "Reading graph from file " + file.toString() + "...");
            VisualGraph vg = getGraphFromFile(file);
            Logging.info("graph_reading", "Read graph.");
            GraphTab nt = openEmptyTab(file);
            nt.addFile(file);
            Vis.lastOpenedPath = file;
            updateMenuBars();
            return nt;

        } catch (FileNotFoundException e) {
            String m = e.getMessage();
            Logging.error("graph_reading", m);
            JOptionPane.showMessageDialog(this, m);
        } catch (GraphReadingException e) {
            String m = e.getMessage();
            Logging.error("graph_reading", m);
            JOptionPane.showMessageDialog(this, m);
        }
        return null;
    }

    private void openTab(FileGraphSource fileGraphSource) {
        GraphTab nt = openEmptyTab(fileGraphSource.getTitle());
        nt.addGraphSource(fileGraphSource);
    }

    public void closeTab(GraphTab t) {
        tabs.remove(t);
        System.gc();
        updateMenuBars();
        Logging.info("ui", "closeTab()");
    }

    public void closeCurrentTab() {
        this.closeTab((GraphTab) this.tabs.getSelectedComponent());
    }

    private static void printCmdLineHelp(Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter
                .printHelp(PROGRAM_NAME + " [options] input-bmg-file(s)", opts);
    }

    private void updateMenuBars() {
        GraphTab tab = (GraphTab) tabs.getSelectedComponent();
        if (tab == null) {
            Logging.info("ui", "No graphs open, creating the default menu.");
            setJMenuBar(getDefaultMenu());
        } else {
            setJMenuBar(tab.getMenuBar());
        }
        this.validate();
        this.repaint();
    }

    private void createGUI(String title) {
//        try {
//            System.setProperty("com.apple.macosx.AntiAliasedTextOn", "false");
//            System.setProperty("com.apple.macosx.AntiAliasedGraphicsOn",
//                    "false");
//        } catch (Throwable t) {
//            // Ignore property exceptions
//        }
//        try {
//            System.setProperty("sun.java2d.opengl", "true");
//        } catch (Throwable t) {
//            // Ignore property exceptions
//        }

        this.setLayout(new GridLayout());
        this.tabs = new TabbedPaneHiddenBar();
        this.getContentPane().add(tabs);
        this.tabs.setVisible(true);

        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                GraphTab tab = (GraphTab) tabs.getSelectedComponent();
                if (tab == null) {
                    Logging.info("ui", "No graphs open, creating the default menu.");
                    Vis.this.setJMenuBar(getDefaultMenu());
                } else {
                    Vis.this.setJMenuBar(tab.getMenuBar());
                }
            }
        });

        if (appletContext == null) {
            Options opts = new Options();
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = null;

            opts.addOption("h", "help", false, "Show help.");
            opts.addOption("s", "simple", false,
                    "Show simple UI without side panes");
            opts.addOption("logcategories", true,
                    "Logging categories delimited by spaces, all for all");
            opts.addOption("loglevel", true,
                    "Logging level: 0 for debug, 1 for info, 2 for warning, 3 for error");
            opts.addOption("d", "debug", false, "Shorthand for logcategories all, loglevel -1");


            try {
                cmd = parser.parse(opts, args);
            } catch (ParseException e) {
                System.err.println(e.getLocalizedMessage());
                System.err.println();
                Vis.printCmdLineHelp(opts);
                System.exit(1);
            }

            if (cmd.hasOption("-h") || cmd.getArgList().size() == 0) {
                Vis.printCmdLineHelp(opts);
                System.exit(0);
            }

            if (cmd.hasOption("-s"))
                this.properties.put("simple", "true");

            if (cmd.hasOption("-d")) {
                Logging.init(new ArrayList<String>(Arrays.asList("all")), -1);
            } else {
                ArrayList<String> logCategories = null;
                Integer logLevel = null;
                if (cmd.hasOption("logcategories"))
                    logCategories = new ArrayList<String>(Arrays.asList(cmd.getOptionValue("logcategories").split(",")));
                if (cmd.hasOption("loglevel"))
                    logLevel = Integer.parseInt(cmd.getOptionValue("loglevel"));

                if (logCategories != null || logLevel != null)
                    Logging.init(logCategories, logLevel);
            }

            VisFrame frame = new VisFrame("BMVIS II");
            this.visFrame = frame;
            frame.add(this);

            boolean noneOpened = true;
            for (Object arg : cmd.getArgList()) {
                if (this.openTab((String) arg) != null)
                    noneOpened = false;
            }

            if (noneOpened && cmd.getArgList().size() > 0) {
                String message = "No files could be opened! Exiting.";
                Logging.error("graph_reading", message);
                JOptionPane.showMessageDialog(this, message);
                System.exit(1);
            }

            this.visFrame.setVisible(true);

        } else {
            // Applet operation goes here...
            this.setVisible(true);

            try {
                if (getParameter("graph") != null) {
                    String graphFile = getParameter("graph");

                    URL u = new URL(getCodeBase(), graphFile);
                    InputStream graphStream = u.openConnection()
                            .getInputStream();
                    BMGraph bm = BMGraphUtils.readBMGraph(graphStream);
                    graphStream.close();
                    this.openTab(new FileGraphSource(bm));
                }
                if (getParameter("json") != null) {
                    String jsonFile = getParameter("json");
                    URL u = new URL(getCodeBase(), jsonFile);
                    InputStream jsonStream = u.openConnection()
                            .getInputStream();

                    openJSONTab(StreamToString
                            .convertStreamToString(jsonStream), jsonFile);
                }
            } catch (IOException e) {
                Logging.error("graph_reading", e.toString());
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    /**
     * Common initialization method called by Applet's overridden init() and
     * our private CLI init(String[] args).
     */
    private void initWindow() {
        try {
            // Set cross-platform Java L&F (also called "Metal")

            // UIManager.setLookAndFeel(
            //         UIManager.getCrossPlatformLookAndFeelClassName());

            // System default
            // UIManager.setLookAndFeel(
            //        UIManager.getSystemLookAndFeelClassName());
            String os = System.getProperty("os.name");
            if (os.startsWith("Mac OS")) {
                // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } else if (os.startsWith("Windows")) {
                // UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } else {
                Logging.info("ui", "Unknown operating system: " + os + ", not setting a specific Look&Feel.");
            }
            // MOTIF!
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (Exception e) {
            // handle exception
        }

        String[] logCategories = {"enduser", "graph_reading", "expand", "js"};
        Logging.init(Arrays.asList(logCategories), Logging.LEVEL_DEBUG);
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Vis.this.createGUI("BMVIS II");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Logging.error("ui", "Failed to create GUI!");
            System.exit(1);
        }

        this.addFocusListener(focusListener);
    }

    /**
     * Public constructor to be used in Applet context.
     *
     * @see java.applet.Applet#init()
     */
    public void init() {
        Vis.appletContext = this.getAppletContext();

        // Set default parameters
        if (getParameter("simple") == null)
            if (isApplet())
                properties.put("simple", "true");// default for applets
            else
                properties.put("simple", "false");

        this.initWindow();
    }

    public void setClipboard(String str) {
        assert str != null : "Null str";
        AppletContext appletContext = null;
        try {
            appletContext = getAppletContext();
        } catch (Exception e) {
        }

        Clipboard clipboard;
        StringSelection data = new StringSelection(str);

        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (clipboard != null) {
                clipboard.setContents(data, null);
            }
        } catch (java.security.AccessControlException e) {
            if (appletContext != null) {
                try {
                    str = str.replace('\'', '"');
                    appletContext.showDocument(new URL(
                            "javascript:copyToClipboard(encodeURIComponent('"
                                    + str + "'))"));
                } catch (Exception e2) {
                    JOptionPane.showMessageDialog(null, e2.getMessage(),
                            "Copying to clipboard failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Copying to clipboard failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Private constructor to be used from command line.
     *
     * @param args
     */
    private void init(String[] args) {
        this.args = args;
        this.appletContext = null;

        initWindow();
    }

    public static void main(final String[] args) {
        (new Vis()).init(args);
    }

    public GraphTab getCurrentTab() {
        GraphTab tab = (GraphTab) tabs.getSelectedComponent();
        return tab;
    }

    /**
     * Functions designed to be called from Javascript (use Runnables and are asynchronous)
     */

    public void jsOpenJSONTab(final String json, final String title) {
        Logging.info("js", "jsOpenJSONTab called with params: " + json + ", " + title);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                openJSONTab(json, title);
            }
        });
    }

    public void setSelectedNodes(final String[] selectedIds) {
        Logging.info("js", "setSelectedNodes called with params: " + selectedIds);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GraphTab tab = (GraphTab) tabs.getSelectedComponent();
                Collection<VisualNode> nodes = tab.getVisualGraph().getNodes();

                HashSet<String> names = new HashSet<String>(Arrays.asList(selectedIds));
                tab.getVisualGraph().clearSelected();
                for (VisualNode node : nodes) {
                    String id = node.getBMNode().getType() + "_" + node.getBMNode().getId();
                    if (names.contains(id))
                        node.setSelected(true);
                }
                tab.getVisualGraph().selectionChanged();
            }
        });
    }

    public void zoomToSelected() {
        Logging.info("js", "zoomToSelected called!");
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                GraphTab tab = (GraphTab) tabs.getSelectedComponent();
                tab.getVisualizer().getGraphArea().zoomTo(tab.getVisualGraph().getSelected());
            }
        });
    }

    public void zoomToNodes(final String[] nameStrings) {
        Logging.info("js", "zoomToNodes called with params: " + nameStrings);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GraphTab tab = (GraphTab) tabs.getSelectedComponent();
                Collection<VisualNode> nodes = tab.getVisualGraph().getNodes();

                HashSet<String> names = new HashSet<String>(Arrays.asList(nameStrings));

                tab.getVisualGraph().clearSelected();
                ArrayList<VisualNode> zoomNodes = new ArrayList<VisualNode>();
                for (VisualNode vn : nodes) {
                    String id = vn.getBMNode().getType() + "_" + vn.getBMNode().getId();
                    if (names.contains(id))
                        zoomNodes.add(vn);
                }
                tab.getVisualGraph().selectionChanged();
                tab.getVisualizer().getGraphArea().zoomTo(zoomNodes);
            }
        });
    }

    public void setInterestNodes(final String[] nameStrings) {
        Logging.info("js", "setInterestNodes called with params: " + nameStrings);

        for (String s : nameStrings) {
            if (s.contains("_")) {
                Logging.info("js", "setInterestNodes param cannot be in the canonical type_name format:");
                Logging.info("js", "  cut out the preceding type information and just give the node id (" + s + ").");
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Set<String> interestNodes = new HashSet<String>(Arrays.asList(nameStrings));

                getCurrentTab().getPipeline().getControls().setInterestNodes(interestNodes);
            }
        });
    }

    public void addInterestNode(final String name) {
        Logging.info("js", "addInterestNode called with params: " + name);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getCurrentTab().getPipeline().getControls().addInterestNode(name);
            }
        });
    }

    public void removeInterestNode(final String name) {
        Logging.info("js", "removeInterestNode called with params: " + name);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getCurrentTab().getPipeline().getControls().removeInterestNode(name);
            }
        });
    }

    public String[] getInterestNodes() {
        Logging.info("js", "getInterestNodes called!");
        ArrayList<String> nodes = new ArrayList<String>(getCurrentTab().getPipeline().getControls().getInterestNodes());

        if (nodes == null) {
            Logging.info("js", "getInterestNodes: current graph controls don't implement this interface.");
            return null;
        }

        String[] ret = new String[nodes.size()];

        for (int i = 0; i < nodes.size(); i++)
            ret[i] = nodes.get(i);

        return ret;
    }

    public void jsCloseCurrentTab() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Vis.this.closeCurrentTab();
            }
        });
    }
}
