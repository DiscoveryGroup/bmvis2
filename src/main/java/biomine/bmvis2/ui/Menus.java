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

package biomine.bmvis2.ui;

import java.awt.Component;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import javax.swing.*;

import biomine.bmgraph.BMEdge;
import biomine.bmgraph.BMEntity;
import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMNode;
import biomine.bmvis2.*;
import biomine.bmvis2.help.Help;
import biomine.bmvis2.pipeline.*;
import biomine.bmvis2.pipeline.GraphOperation.GraphOperationException;
import biomine.bmvis2.pipeline.operations.ManualGroupOperation;
import biomine.bmvis2.pipeline.sources.GraphSource;
import biomine.bmvis2.pipeline.sources.QueryGraphSource;
import biomine.bmvis2.utils.FileFilters;

/**
 * Menu hunting ground.
 *
 * @author alhartik
 * @author ahinkka
 */
public final class Menus {
    private Vis vis;
    private Pipeline pipeline;

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    private static String fixType(String str, String type) {
        Pattern p = Pattern.compile(".*\\." + Pattern.quote(type),
                Pattern.CASE_INSENSITIVE);

        if (p.matcher(str).matches())
            return str;
        return str + "." + type;
    }

    private Menus(Pipeline pipe) {
        if (pipe != null)
            vis = pipe.getVis();
        Logging.debug("ui", "Setting pipeline.");
        setPipeline(pipe);
    }

    public Menus(Vis pVis) {
        this.vis = pVis;
        this.setPipeline(null);
    }

    private static final String NODEINFO_PREFIX = "<html><small style=\"color: #666666\">";
    private static final String NODEINFO_INFIX = "</small><small style=\"color: #006600\"> ";
    private static final String NODEINFO_SUFFIX = "</small></html>";

    /*
    private static final LinkedList<String> DISPLAY_EDGE_ATTRIBUTES = new LinkedList<String>();
    private static final LinkedList<String> DISPLAY_NODE_ATTRIBUTES = new LinkedList<String>();

    static {
        DISPLAY_EDGE_ATTRIBUTES.add(BMGraphAttributes.GOODNESS_KEY);
        DISPLAY_EDGE_ATTRIBUTES.add(BMGraphAttributes.TTNR_KEY);
        DISPLAY_EDGE_ATTRIBUTES.add(BMGraphAttributes.KTNR_KEY);
        DISPLAY_EDGE_ATTRIBUTES.add(BMGraphAttributes.SOURCE_DB_NAME_KEY);
        DISPLAY_EDGE_ATTRIBUTES.add(BMGraphAttributes.SOURCE_DB_VERSION_KEY);

        DISPLAY_NODE_ATTRIBUTES.add(BMGraphAttributes.TTNR_KEY);
        DISPLAY_NODE_ATTRIBUTES.add(BMGraphAttributes.KTNR_KEY);
    }
    */


    /**
     * Builds the second button menu for a node/edge.
     */
    public JPopupMenu get2ndButtonMenu(final LayoutItem item, final GraphVisualizer visualizer) {
        final boolean isNode = (item instanceof VisualNode);
        final boolean isGroupNode = (item instanceof VisualGroupNode);
        final boolean isEdge = (item instanceof VisualEdge);

        final VisualGraph visualGraph = item.getGraph();
        final BMEntity bme = visualGraph.getBMEntity(item);
        // Popup menu: View in browser
        JPopupMenu menu = new JPopupMenu();


        // View in browser
        if (isNode && !isGroupNode) {
            VisualNode vn = (VisualNode) item;
            if (vn.getBMNode() != null) {
                final BMNode node = vn.getBMNode();
                menu.add(new JMenuItem(new AbstractAction("View in browser") {
                    public void actionPerformed(ActionEvent e) {
                        Logging.info("enduser", "Opened url " + item);
                        vis.viewInBrowser(node);
                    }
                }));
            }
            menu.addSeparator();
        }


        // Layout
        boolean wouldUnPin = item.isPositionFixed();
        JMenu layoutMenu = new JMenu("Layout");
        if (wouldUnPin) {
            layoutMenu.add(new AbstractAction("Unpin") {
                public void actionPerformed(ActionEvent e) {
                    item.setHighlight(false);
                    item.setPositionFixed(false);
                    visualizer.getGraphArea().activateLayoutManager();
                }
            });

        } else {
            String type = isEdge ? "edge" : "node";
            layoutMenu.add(new AbstractAction("Pin " + type) {
                public void actionPerformed(ActionEvent e) {
                    item.setPositionFixed(true);
                }
            });
        }

        // Logic to prevent showing unpin selected if all are unpinned
        boolean allUnPinned = true;
        for (LayoutItem it : visualGraph.getSelected()) {
            if (it.isPositionFixed()) {
                allUnPinned = false;
                break;
            }
        }

        if (!allUnPinned) {
            layoutMenu.add(new AbstractAction("Unpin selected") {
                public void actionPerformed(ActionEvent e) {
                    for (LayoutItem it : visualGraph.getSelected()) {
                        it.setHighlight(false);
                        it.setPositionFixed(false);
                    }
                    visualizer.getGraphArea().activateLayoutManager();
                }
            });
        }

        // Logic to prevent showing "pin selected" if all selected are pinned
        boolean allSelectedPinned = true;
        for (LayoutItem it : visualGraph.getSelected()) {
            if (!it.isPositionFixed()) {
                allSelectedPinned = false;
                break;
            }
        }
        if (!allSelectedPinned) {
            layoutMenu.add(new AbstractAction("Pin selected nodes") {
                public void actionPerformed(ActionEvent e) {
                    for (LayoutItem it : visualGraph.getSelected())
                        it.setPositionFixed(true);
                }
            });
        }
        menu.add(layoutMenu);

        // Group actions
        if (isGroupNode) {
            final VisualGroupNode groupNode = (VisualGroupNode) item;
            JMenu groupMenu = new JMenu("Group");

            groupMenu.add(new AbstractAction("Open group") {
                public void actionPerformed(ActionEvent e) {
                    groupNode.setOpen(true);
                    groupNode.setHighlight(false);
                    visualizer.getGraphArea().activateLayoutManager();
                }
            });
            groupMenu.add(new AbstractAction("Close group") {
                public void actionPerformed(ActionEvent e) {
                    groupNode.setOpen(false);
                    groupNode.setHighlight(false);
                    visualizer.getGraphArea().activateLayoutManager();
                }
            });

            if (groupNode.getParent() != null)
                groupMenu.add(new AbstractAction("Destroy group") {
                    public void actionPerformed(ActionEvent e) {
                        visualGraph.destroyGroup(groupNode);

                    }
                });
            // opening subgraps in tabs is currently not supported
            // groupMenu.add(new AbstractAction("Open group in another tab") {
            // public void actionPerformed(ActionEvent e) {
            // Vis.getInstance() .openTab(new VisualGraph(groupNode), "groupview");
            // }
            // });
            menu.add(groupMenu);
        }

        if (isNode) {
            final VisualNode vn = (VisualNode) item;

            if (vn.getParent() != null && vn.getParent().getParent() != null) {
                JMenu groupMenu = new JMenu("Enclosing group");

                groupMenu.add(new AbstractAction("Select enclosing group members") {
                    public void actionPerformed(ActionEvent e) {
                        visualGraph.clearSelected();

                        for (VisualNode n : vn.getParent().getChildren()) {
                            visualGraph.addSelected(n);
                        }
                        visualGraph.selectionChanged();
                    }
                });

                // Nodes that are part of the root group cannot be manipulated
                groupMenu.add(new AbstractAction("Collapse enclosing group") {
                    public void actionPerformed(ActionEvent e) {
                        vn.getParent().setOpen(false);
                        vn.setHighlight(false);
                        visualizer.getGraphArea().activateLayoutManager();
                    }
                });

                groupMenu.add(new AbstractAction("Remove from enclosing group") {
                    public void actionPerformed(ActionEvent e) {
                        vn.setParent(vn.getParent().getParent());
                    }
                });

                groupMenu.addSeparator();
                groupMenu.add(new JLabel("<html><small><strong>Enclosing group</strong> refers to the group<br> the focused edge immediately belongs to.</small></html>"));

                menu.add(groupMenu);
            }

            if (visualGraph.getSelected().size() == 1) {
                for (final VisualNode group : visualGraph.getSelected()) {
                    if (group instanceof VisualGroupNode && vn != group) {
                        menu.add(new AbstractAction("Add to selected group") {
                            public void actionPerformed(ActionEvent e) {
                                vn.setParent((VisualGroupNode) group);
                            }
                        });
                    }
                }
            }

            if (vn.isSelected()) {
                menu.add(new AbstractAction("Remove from selection") {
                    public void actionPerformed(ActionEvent e) {
                        visualGraph.removeSelected(vn);
                        // bmvis.setLayoutEnabled(bmvis.enableAutomaticLayout);
                    }
                });
            } else {
                menu.add(new AbstractAction("Add to selection") {
                    public void actionPerformed(ActionEvent e) {
                        visualGraph.addSelected(vn);
                    }
                });
            }

            /* JMenu unSafeManipulationMenu = new JMenu("Non-permanent manipulations");
       if (visualGraph.getSelected().size() > 1) {
           unSafeManipulationMenu.add(new AbstractAction("Make group from selection") {
               public void actionPerformed(ActionEvent e) {
                   visualGraph.groupSelected();
               }
           });
       }

       // See DeleteElementsOperation
       unSafeManipulationMenu.add(new AbstractAction("Delete node") {
           public void actionPerformed(ActionEvent e) {
               // pipeline.getDeleteOperation().deleteNode(vn);
               visualGraph.deleteNode(vn);
           }
       });
       if (!vis.useSimpleUI())
           menu.add(unSafeManipulationMenu); */

            menu.addSeparator();
            if (!visualGraph.getNodesOfInterest().containsKey(vn)) {
                menu.add(new AbstractAction(
                        "+1 interest") {
                    public void actionPerformed(ActionEvent e) {
                        pipeline.getControls().addInterestNode(vn.getName());
                        pipeline.addNodeOfInterest(vn, +1.0);
                        pipeline.settingsChanged(true);
                    }
                });
                menu.add(new AbstractAction(
                        "-1 interest") {
                    public void actionPerformed(ActionEvent e) {
                        pipeline.getControls().removeInterestNode(vn.getName());
                        pipeline.addNodeOfInterest(vn, -1.0);
                        pipeline.settingsChanged(true);
                    }
                });
            } else {
                menu.add(new AbstractAction("Neutral interest") {
                    public void actionPerformed(ActionEvent e) {
                        pipeline.removeNodeOfInterest(vn);
                        pipeline.settingsChanged(true);
                    }
                });
            }
            menu.addSeparator();

            if (pipeline.isExpandable(vn.getBMNode())) {
                menu.add(new AbstractAction("Expand") {
                    public void actionPerformed(ActionEvent arg0) {
                        try {
                            GraphSource tmp = pipeline.expand(vn);
                            if (tmp != null)
                                pipeline.addSource(tmp);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                menu.addSeparator();
            }

            // Attributes
            JMenuItem clipboardMessage = new JMenuItem("Click item below to copy text to clipboard");
            clipboardMessage.setEnabled(false);
            menu.add(clipboardMessage);


            if (isNode && !isGroupNode) {
                final BMNode node = (BMNode) bme;

                final String nodeId = node.getId();
                final String shortName = node.get("ShortName");
                final String primaryName = node.get("PrimaryName");
                final String organismKey = node.get(BMGraphAttributes.ORGANISM_KEY);

                menu.add(new JMenuItem(new AbstractAction("<html><small>" + nodeId + "</small></html>") {
                    public void actionPerformed(ActionEvent e) {
                        vis.setClipboard(nodeId);
                    }
                }));

                if (shortName != null && !shortName.equalsIgnoreCase(nodeId)) {
                    // Logging.debug("enduser", "Adding ShortName");
                    menu.add(new JMenuItem(new AbstractAction("<html><small>" + shortName + "</small></html>") {
                        public void actionPerformed(ActionEvent e) {
                            vis.setClipboard(shortName);
                        }
                    }));
                }

                if (primaryName != null && !primaryName.equalsIgnoreCase(nodeId)) {
                    // Logging.debug("enduser", "Adding PrimaryName");
                    menu.add(new JMenuItem(new AbstractAction("<html><small>" + primaryName + "</small></html>") {
                        public void actionPerformed(ActionEvent e) {
                            vis.setClipboard(primaryName);
                        }
                    }));
                }

                String typeContents = NODEINFO_PREFIX + "type:" + NODEINFO_INFIX + node.getType() +
                        (organismKey == null ? "" : " (" + organismKey + ")") + NODEINFO_SUFFIX;
                // typeContents = "type: " + node.getType();
                menu.add(new JMenuItem(new AbstractAction(typeContents) {
                    public void actionPerformed(ActionEvent e) {
                        vis.setClipboard(node.getType());
                    }
                }));

                for (String attribute : node.getAttributes().keySet()) {
                    if (LabeledItem.nonVisibleAttributes.contains(attribute.toLowerCase()))
                        continue;
                    String tmpValue = node.get(attribute);
                    if (tmpValue != null && tmpValue.length() > 0) {
                        final String value = tmpValue;
                        // Logging.debug("enduser", "Adding attribute " + attribute + ": " + value);
                        String itemContents = NODEINFO_PREFIX + attribute.replace('_', ' ') + ":" + NODEINFO_INFIX +
                                value + NODEINFO_SUFFIX;
                        // itemContents = attribute.replace("_", " ") + ": " + value;
                        // Logging.debug("enduser", itemContents);
                        menu.add(new JMenuItem(new AbstractAction(itemContents) {
                            public void actionPerformed(ActionEvent e) {
                                vis.setClipboard(value);
                            }
                        }));
                    }
                }
            }
        } else if (isEdge)

        {
            final BMEdge edge = (BMEdge) bme;
            final VisualEdge visualEdge = (VisualEdge) item;
            if (edge != null) {
                final String edgePointIds = edge.getTo().getId() + "\n"
                        + edge.getFrom().getId();

                String s;
                // DEBUG: Known bug (or feature?) - group as endpoint is
                // copied by group id, not group member ids
                menu.add(new JMenuItem(new AbstractAction("<html><small>" + edgePointIds.replace("\n", " ") +
                        "</small></html>") {
                    public void actionPerformed(ActionEvent e) {
                        vis.setClipboard(edgePointIds);
                    }
                }));

                /* JMenu unSafeManipulationMenu = new JMenu("Non-permanent manipulations");
           // See DeleteElementsOperation
           unSafeManipulationMenu.add(new AbstractAction("Delete edge") {
               public void actionPerformed(ActionEvent e) {
                   visualGraph.deleteEdge(visualEdge);
                   // pipeline.getDeleteOperation.deleteEdge(visualEdge);
               }
           });
           if (!vis.useSimpleUI())
               menu.add(unSafeManipulationMenu); */


                String typeContents = NODEINFO_PREFIX + "type:" + NODEINFO_INFIX + edge.getLinktype() +
                        NODEINFO_SUFFIX;
                menu.add(new JMenuItem(new AbstractAction(typeContents) {
                    public void actionPerformed(ActionEvent e) {
                        vis.setClipboard(edge.getLinktype());
                    }
                }));

                if (visualEdge.getWeight(VisualEdge.WeightType.PROBABILISTIC) != null) {
                    String probabilisticWeightContents = NODEINFO_PREFIX + "prob weight:" + NODEINFO_INFIX + visualEdge.getWeight(VisualEdge.WeightType.PROBABILISTIC).value +
                            NODEINFO_SUFFIX;
                    menu.add(new JMenuItem(new AbstractAction(probabilisticWeightContents) {
                        public void actionPerformed(ActionEvent e) {
                            vis.setClipboard(Double.toString(visualEdge.getWeight(VisualEdge.WeightType.PROBABILISTIC).value));
                        }
                    }));
                }

                if (visualEdge.getWeight(VisualEdge.WeightType.WEIGHT) != null) {
                    String weightWeightContents = NODEINFO_PREFIX + "other weight:" + NODEINFO_INFIX + visualEdge.getWeight(VisualEdge.WeightType.WEIGHT).value +
                            NODEINFO_SUFFIX;
                    menu.add(new JMenuItem(new AbstractAction(weightWeightContents) {
                        public void actionPerformed(ActionEvent e) {
                            vis.setClipboard(Double.toString(visualEdge.getWeight(VisualEdge.WeightType.WEIGHT).value));
                        }
                    }));
                }

                for (String attribute : edge.getAttributes().keySet()) {
                    if (LabeledItem.nonVisibleAttributes.contains(attribute.toLowerCase()))
                        continue;
                    s = edge.get(attribute);
                    if (s != null && s.length() > 0) {
                        final String value = s;
                        menu.add(new JMenuItem(new AbstractAction(
                                NODEINFO_PREFIX + attribute.replace('_', ' ')
                                        + ":" + NODEINFO_INFIX + s
                                        + NODEINFO_SUFFIX) {
                            public void actionPerformed(ActionEvent e) {
                                vis.setClipboard(value);
                            }
                        }));
                    }
                }
            }
        }

        /* menu.addSeparator();
    menu.add("Visualized coordinates: (" + (int) item.getPos().x + "," +
    (int) item.getPos().y + ")").setEnabled(false); */

        return menu;
    }

    public JFileChooser createFileChooser() {
        JFileChooser choose = new JFileChooser();
        if (Vis.lastOpenedPath != null)
            choose.setCurrentDirectory(new File(Vis.lastOpenedPath));
        return choose;
    }

    private void exportToBMG(final JComponent menu, final VisualGraph vg) {
        JFileChooser choose;
        if (vg.getFileName() != null)
            choose = new JFileChooser(vg.getFileName());
        else
            choose = new JFileChooser();

        if (Vis.lastOpenedPath != null)
            choose.setCurrentDirectory(new File(Vis.lastOpenedPath));

        choose.setFileFilter(FileFilters.FILTER_GRAPH);
        choose.showSaveDialog(vis);
        File f = choose.getSelectedFile();
        if (f == null)
            return;
        try {
            vg.save(f);
            JOptionPane.showInternalMessageDialog(vis,
                    "Graph exported as BMGraph!");
        } catch (IOException e) {
            Logging.error("enduser", e.getMessage());
            System.err.println(e);
            JOptionPane.showMessageDialog(vis, e.toString());
        }
    }

    private void exportToJSON(final JComponent menu, final Pipeline pipeline) {
        JFileChooser choose;
        if (pipeline.getLoadedFile() != null)
            choose = new JFileChooser(pipeline.getLoadedFile());
        else
            choose = new JFileChooser();

        if (Vis.lastOpenedPath != null)
            choose.setCurrentDirectory(new File(Vis.lastOpenedPath));

        choose.setFileFilter(FileFilters.FILTER_JSON);
        choose.showSaveDialog(vis);
        File f = choose.getSelectedFile();
        if (f == null)
            return;

        f = new File(fixType(f.getPath(), "json"));
        pipeline.saveOperations(f);
    }

    private boolean confirmExportJSON(JComponent menu, final Pipeline pipeline) {
        if (pipeline == null)
            return true;
        switch (JOptionPane.showConfirmDialog(vis,
                "Do you wish to save operations in the currently active tab?",
                "Save graph?", JOptionPane.YES_NO_CANCEL_OPTION)) {
            case JOptionPane.YES_OPTION:
                exportToJSON(menu, pipeline);
                return true;
            case JOptionPane.NO_OPTION:
                return true;
            case JOptionPane.CANCEL_OPTION:
                return false;
            default:
                return false;
        }
    }

    private JMenu createGroupMenu(final GraphVisualizer visualizer) {
        JMenu groupMenu = new JMenu("Group");

        groupMenu.add(new JMenuItem(new AbstractAction("Open selected groups") {
            public void actionPerformed(ActionEvent e) {
                final VisualGraph visualGraph = pipeline.getCurrentGraph();
                for (VisualNode n : visualGraph.getSelected()) {
                    if (n instanceof VisualGroupNode) {
                        VisualGroupNode vgn = (VisualGroupNode) n;
                        vgn.setHighlight(false);
                        vgn.setOpen(true);
                        if (visualizer != null)
                            visualizer.getGraphArea().activateLayoutManager();
                    }
                }
            }
        }));
        groupMenu.add(new JMenuItem(
                new AbstractAction("Close selected groups") {
                    public void actionPerformed(ActionEvent e) {
                        final VisualGraph visualGraph = pipeline
                                .getCurrentGraph();
                        for (VisualNode n : visualGraph.getSelected()) {
                            if (n instanceof VisualGroupNode) {
                                VisualGroupNode vgn = (VisualGroupNode) n;
                                vgn.setOpen(false);
                                vgn.setHighlight(false);
                                visualizer.getGraphArea().activateLayoutManager();
                            }
                        }
                    }
                }));
        groupMenu.addSeparator();

        groupMenu.add(new JMenuItem(new AbstractAction("Open all groups") {
            public void actionPerformed(ActionEvent e) {
                final VisualGraph visualGraph = pipeline.getCurrentGraph();
                for (VisualNode n : visualGraph.getAllNodes())
                    if (n instanceof VisualGroupNode) {
                        ((VisualGroupNode) n).setOpen(true);
                        n.setHighlight(false);
                        visualizer.getGraphArea().activateLayoutManager();
                    }
            }
        }));
        groupMenu.add(new JMenuItem(new AbstractAction("Close all groups") {
            public void actionPerformed(ActionEvent e) {
                final VisualGraph visualGraph = pipeline.getCurrentGraph();
                for (VisualNode n : visualGraph.getAllNodes())
                    if (n instanceof VisualGroupNode && n.getParent() != null) {
                        ((VisualGroupNode) n).setOpen(false);
                        n.setHighlight(false);
                        visualizer.getGraphArea().activateLayoutManager();
                    }
            }
        }));
        return groupMenu;
    }

    private JMenu createEditMenu(final Pipeline pipeline) {
        final VisualGraph visualGraph = pipeline.getCurrentGraph();

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem(new AbstractAction(
                "Make group from selection") {
            public void actionPerformed(ActionEvent e) {
                if (visualGraph.getSelected().isEmpty())
                    return;
                pipeline.addStructuralOp(new ManualGroupOperation(visualGraph
                        .getSelected()));
            }
        }));

        editMenu.add(new JMenuItem(new AbstractAction(
                "Query more nodes between selected nodes") {
            public void actionPerformed(ActionEvent e) {
                BMGraph sourceGraph = null;
                Set<BMNode> bmNodes = new HashSet<BMNode>();
                for (VisualNode vn : visualGraph.getSelected())
                    bmNodes.add(vn.getBMNode());

                try {
                    for (GraphSource src : pipeline.getCurrentSources()) {
                        boolean contains = true;
                        for (BMNode n : bmNodes)
                            if (src.getBMGraph().getNode(n) == null) {
                                contains = false;
                                break;
                            }
                        if (contains)
                            sourceGraph = src.getBMGraph();
                    }
                } catch (GraphOperationException e1) {
                    e1.printStackTrace();
                }
                String db = null;
                if (sourceGraph != null)
                    db = sourceGraph.getDatabaseArray()[1];
                Logging.debug("graph_operation", "db = " + db);
                GraphSource src = QueryGraphSource.createFromDialog(
                        visualGraph.getSelected(), db);
                if (src != null)
                    pipeline.addSource(src);
            }
        }));

        return editMenu;
    }


    /**
     * Right-click on the canvas.
     * - Pin all nodes
     * - Release all nodes
     * - Pin selected nodes
     * - Release selected nodes
     * - edit menu items
     */
    public JPopupMenu getCanvasMenu(final GraphVisualizer visualizer) {
        JPopupMenu m = new JPopupMenu();
        final VisualGraph vg = pipeline.getCurrentGraph();

        JMenuItem pinAll = m.add("Pin all nodes");
        pinAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                vg.pinAll();
            }
        });

        pinAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Event.CTRL_MASK));
        m.add(pinAll);

        JMenuItem releaseAll = m.add("Unpin all");
        releaseAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                vg.releaseAll();
            }
        });

        releaseAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                Event.CTRL_MASK));
        m.add(releaseAll);

        m.addSeparator();

        /* m.add(new AbstractAction("Pin selected") {
            public void actionPerformed(ActionEvent e) {
                for (LayoutItem it : vg.getSelected())
                    it.setPositionFixed(true);
            }
        });

        m.add(new AbstractAction("Unpin selected") {
            public void actionPerformed(ActionEvent e) {
                for (LayoutItem it : vg.getSelected()) {
                    it.setHighlight(false);
                    it.setPositionFixed(false);
                }
                visualizer.getGraphArea().activateLayoutManager();
            }
        });

        m.addSeparator(); */

        JMenu groupMenu = createGroupMenu(visualizer);
        m.add(groupMenu);

        m.add(createEditMenu(pipeline));

        return m;
    }

    /**
     * Builds the top-most menu of the BMVis window.
     */
    public void buildMenuBar(final JComponent menu,
                             Collection<JavaScriptConsole> consoles) {
        JMenu fileMenu = new JMenu("File");
        Logging.debug("ui", "buildMenuBar vis=" + vis);
        if (!vis.isApplet()) {
            JMenuItem importBMG = fileMenu
                    .add("Import BMGraph file to a new tab");
            importBMG.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    JFileChooser choose = new JFileChooser();

                    if (Vis.lastOpenedPath != null)
                        choose
                                .setCurrentDirectory(new File(
                                        Vis.lastOpenedPath));

                    choose.setFileFilter(FileFilters.FILTER_GRAPH);
                    choose.showOpenDialog(menu.getParent());
                    final File f = choose.getSelectedFile();

                    if (f == null)
                        return;

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            // GraphArea area = graphArea;
                            vis.openTab(f.getAbsolutePath());
                        }
                    });

                }
            });
            importBMG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                    Event.CTRL_MASK));

            JMenuItem importJSON = fileMenu
                    .add("Import JSON list of operations to a new tab");
            importJSON.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    JFileChooser choose = new JFileChooser();

                    if (Vis.lastOpenedPath != null)
                        choose
                                .setCurrentDirectory(new File(
                                        Vis.lastOpenedPath));
                    choose.setFileFilter(FileFilters.FILTER_JSON);
                    int z = choose.showOpenDialog(vis);
                    if (z == JFileChooser.APPROVE_OPTION) {
                        File f = choose.getSelectedFile();
                        GraphTab tab = vis.openEmptyTab(f.getName());
                        tab.getPipeline().loadOperations(f);
                    }
                }
            });

            fileMenu.addSeparator();
        }

        if (pipeline != null) {
            if (!vis.isApplet()) {
                JMenuItem close = fileMenu.add("Close tab");
                close.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        if (confirmExportJSON(menu, pipeline)) {
                            vis.closeCurrentTab();
                        }
                    }
                });
                close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                        Event.CTRL_MASK));
                fileMenu.addSeparator();
            }

            JMenuItem exportBMG = fileMenu.add("Export view to BMGraph");
            exportBMG.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    exportToBMG(menu, pipeline.getCurrentGraph());
                }
            });
            exportBMG.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    Event.CTRL_MASK));
            fileMenu.add(exportBMG);

            fileMenu.add(new AbstractAction("Export view to PNG") {
                public void actionPerformed(ActionEvent arg0) {
                    GraphArea graphArea = pipeline.getVisualizer().getGraphArea();
                    if (graphArea != null) {
                        JFileChooser choose = new JFileChooser();

                        if (Vis.lastOpenedPath != null)
                            choose.setCurrentDirectory(new File(
                                    Vis.lastOpenedPath));

                        choose.setFileFilter(FileFilters.FILTER_PNG);
                        choose.showOpenDialog(menu.getParent());
                        File file = choose.getSelectedFile();
                        if (file == null)
                            return;
                        String path = file.getAbsolutePath();
                        path = fixType(path, "png");
                        try {
                            graphArea.exportToPNG(new File(path));
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(vis, e.getMessage());
                        }
                    }
                }
            });

            fileMenu.add(new AbstractAction(
                    "Export view to JSON list of operations") {
                public void actionPerformed(ActionEvent arg0) {
                    JFileChooser choose = new JFileChooser();
                    if (Vis.lastOpenedPath != null)
                        choose
                                .setCurrentDirectory(new File(
                                        Vis.lastOpenedPath));

                    if (pipeline.getLoadedFile() != null)
                        choose.setSelectedFile(pipeline.getLoadedFile());

                    choose.setFileFilter(FileFilters.FILTER_JSON);
                    int ok = choose.showSaveDialog(vis);
                    if (ok == JFileChooser.APPROVE_OPTION) {
                        pipeline.saveOperations(choose.getSelectedFile());
                    }
                }
            });
            if (!vis.isApplet())
                fileMenu.addSeparator();
        }

        JMenuItem quit = new JMenuItem(new AbstractAction("Quit") {
            public void actionPerformed(ActionEvent e) {
                if (confirmCloseAll())
                    System.exit(0);
            }
        });
        quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                Event.CTRL_MASK));

        if (vis == null || !vis.isApplet())
            fileMenu.add(quit);
        menu.add(fileMenu);

        if (pipeline != null) {

            JMenu selectMenu = new JMenu("Select");
            JMenuItem selectAll = new JMenuItem(
                    new AbstractAction("Select all") {
                        public void actionPerformed(ActionEvent e) {
                            for (VisualNode n : pipeline.getCurrentGraph()
                                    .getAllNodes()) {
                                n.setSelected(true);
                            }
                        }

                    });
            selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    Event.CTRL_MASK));
            selectMenu.add(selectAll);

            JMenuItem invertSelection = new JMenuItem(new AbstractAction(
                    "Invert selection") {
                public void actionPerformed(ActionEvent e) {
                    VisualGraph vg = pipeline.getCurrentGraph();
                    HashSet<VisualNode> ns = new HashSet<VisualNode>();
                    ns.addAll(vg.getNodes());
                    ns.removeAll(vg.getSelected());
                    vg.clearSelected();
                    for (VisualNode n : ns)
                        vg.addSelected(n);
                    vg.selectionChanged();
                }
            });
            invertSelection.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_I, Event.SHIFT_MASK));
            selectMenu.add(invertSelection);

            selectMenu.add(new AbstractAction("Make group from selection") {
                public void actionPerformed(ActionEvent e) {
                    VisualGraph vg = pipeline.getCurrentGraph();
                    if (vg.getSelected().size() == 0)
                        return;

                    pipeline.addStructuralOp(new ManualGroupOperation(vg
                            .getSelected()));
                }
            });
            menu.add(selectMenu);

            menu.add(createGroupMenu(null));
            menu.add(createEditMenu(pipeline));

        }

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(new AbstractAction("Show help") {
            public void actionPerformed(ActionEvent arg0) {
                Help.showHelp(vis);
            }
        });
        menu.add(helpMenu);

        this.assignMnemonicsRecursively(new MenuTreeNode(null), menu);
    }

    public boolean confirmCloseAll() {
        int ok = JOptionPane.showConfirmDialog(vis,
                "Are you sure you want to close " + vis.getNumberOfTabs()
                        + " tabs without saving?");
        return ok == JOptionPane.YES_OPTION;
    }

    public class MenuTreeNode {
        MenuTreeNode parent;
        Set<MenuTreeNode> children;
        Character mnemonic;
        String name;

        MenuTreeNode(MenuTreeNode parent) {
            this.parent = parent;
            if (this.parent != null)
                this.parent.addChild(this);
            this.children = new HashSet<MenuTreeNode>();
            this.name = ">";
            this.mnemonic = null;
        }

        public MenuTreeNode(MenuTreeNode parent, String name) {
            this.parent = parent;
            if (this.parent != null)
                this.parent.addChild(this);
            this.children = new HashSet<MenuTreeNode>();
            this.name = name;
            this.mnemonic = null;
        }

        void addChild(MenuTreeNode child) {
            this.children.add(child);
        }

        MenuTreeNode getParent() {
            return this.parent;
        }

        Set<MenuTreeNode> getChildren() {
            return this.children;
        }

        Set<MenuTreeNode> getSiblings() {
            if (this.getParent() == null)
                return new HashSet<MenuTreeNode>();
            Set<MenuTreeNode> s = new HashSet<MenuTreeNode>(this.getParent()
                    .getChildren());
            s.remove(this);
            return s;
        }

        public String toString() {
            List<String> path = new ArrayList<String>();

            MenuTreeNode iter = this;
            while (iter != null) {
                path.add(iter.name);
                iter = iter.getParent();
            }
            Collections.reverse(path);

            String ret = "";
            for (String i : path) {
                if (ret.length() > 0)
                    ret = ret + "-";
                ret = ret + i;
            }
            return ret;
        }

        public boolean equals(Object other) {
            if (other instanceof MenuTreeNode)
                return this.toString().equals(other.toString());
            else
                return false;
        }

        public int hashCode() {
            return this.toString().hashCode();
        }

    }

    private static void getSetMnemonicKey(String name, MenuTreeNode node) {
        Set<Character> reserved = new HashSet<Character>();

        MenuTreeNode n = node;
        while (n != null) {
            if (n.mnemonic != null)
                reserved.add(Character.toLowerCase(n.mnemonic));
            for (MenuTreeNode s : node.getSiblings())
                reserved.add(Character.toLowerCase(s.mnemonic));
            n = n.getParent();
        }

        // d("Reserved: " + reserved);

        for (Character i : name.toCharArray()) {
            if (!Character.isLetter(i))
                continue;
            if (reserved.contains(Character.toLowerCase(i)))
                continue;
            node.mnemonic = Character.toLowerCase(i);

            return;
        }

        for (Character i : "abcdefghijklmnopqrstuvwxyz".toCharArray()) {
            if (reserved.contains(i))
                continue;
            assert node != null;
            node.mnemonic = i;
            return;
        }
    }

    /**
     * Mappings includes an indexed way of accessing
     */
    private void assignMnemonicsRecursively(MenuTreeNode parent, JComponent menu) {
        // d(parent.toString());
        if (menu instanceof JMenu) {
            JMenu m = (JMenu) menu;
            for (int i = 0; i < m.getItemCount(); i++) {
                if (m.getItem(i) == null)
                    continue;
                JMenuItem jmi = m.getItem(i);

                MenuTreeNode node = new MenuTreeNode(parent, jmi.getText());

                getSetMnemonicKey(jmi.getText(), node);
                jmi.setMnemonic(node.mnemonic);
                assignMnemonicsRecursively(node, jmi);
            }
        } else {
            for (Component c : menu.getComponents()) {
                if (c == null)
                    continue;

                if (c instanceof JMenu) {
                    JMenu jm = (JMenu) c;
                    MenuTreeNode node = new MenuTreeNode(parent, jm.getText());
                    getSetMnemonicKey(jm.getText(), node);
                    jm.setMnemonic(node.mnemonic);
                    assignMnemonicsRecursively(node, jm);
                }
            }
        }
    }

    private static WeakHashMap<Pipeline, Menus> instanceMap = new WeakHashMap<Pipeline, Menus>();

    public static Menus getInstance(Pipeline pipe) {
        if (instanceMap.get(pipe) == null) {
            instanceMap.put(pipe, new Menus(pipe));
        }
        return instanceMap.get(pipe);
    }

    private static WeakHashMap<Vis, Menus> defaultInstanceMap = new WeakHashMap<Vis, Menus>();

    public static Menus getDefaultMenuInstance(Vis vis) {
        if (defaultInstanceMap.get(vis) == null) {
            defaultInstanceMap.put(vis, new Menus(vis));

        }
        return defaultInstanceMap.get(vis);
    }
}
