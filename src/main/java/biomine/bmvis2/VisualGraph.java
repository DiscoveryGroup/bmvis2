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

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import javax.swing.SwingUtilities;

import biomine.bmgraph.BMEdge;
import biomine.bmgraph.BMEntity;
import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;
import biomine.bmgraph.BMNode;
import biomine.bmgraph.read.BMGraphReader;
import biomine.bmvis2.color.NodeColoring;

/**
 * Main graph representation for BMVis2. Designed to be useful in visualisation,
 * not for algorithms. Use SimpleGraph when implementing nontrivial algorithms.
 * <p/>
 * This graph implementation keeps a list of observers so UI elements can update
 * themselves by registering to graph change events.
 *
 * @author alhartik
 */
public class VisualGraph {
    public static VisualGraph EMPTY = new VisualGraph(); // special static instance to signify empty graph

    private String fileName; // name of the original graph file

    private boolean showNodeTypes = true; // show node types in nodes or not
    private String goodnessAttribute = BMGraphAttributes.GOODNESS_KEY;
    private String weightAttribute = BMGraphAttributes.WEIGHT_KEY;
    private String flowAttribute = BMGraphAttributes.FLOW_KEY;

    private Collection<VisualNode> hiddenNodes = new HashSet<VisualNode>();
    private Collection<VisualEdge> hiddenEdges = new HashSet<VisualEdge>();

    // Special print mode is used to not draw thumbtacks on nodes that are pinned.
    private boolean printMode = false;

    public enum HighlightMode {
        NEIGHBORS, GROUP
    }

    /**
     * Changes in structure will cause visibility version to increase.
     * <p/>
     * VisualGraph also contains getCombinedVersion() which returns combination
     * of all versions.  This changes when any version part changes.
     */
    public static enum Change {
        STRUCTURE, POINTS_OF_INTEREST, VISIBILITY, COLORS
    }

    /**
     * Version changes when graph is changed. This way we don't have to create
     * graphobserver when we just want to check if graph has changed.
     */
    private long[] version = new long[Change.values().length];
    private Map<BMNode, VisualNode> bmNodeToVNode;
    private Map<BMEdge, VisualEdge> bmEdgeToVEdge;

    private Map<VisualNode, BMNode> vNodeToBMNode;
    private Map<VisualEdge, BMEdge> vEdgeToBMEdge;
    private Set<GraphObserver> observers;

    private NodeColoring nodeColoring;

    private Set<VisualNode> selected;
    private Map<VisualNode, Double> pointsOfInterest = new HashMap<VisualNode, Double>();

    public double getInterestingness(VisualNode n) {
        Double ret = pointsOfInterest.get(n);
        if (ret == null)
            return 0.0;
        return ret;
    }

    public Map<VisualNode, Double> getNodesOfInterest() {
        return pointsOfInterest;
    }

    public void clearNodesOfInterest() {
        pointsOfInterest.clear();
        graphChanged(Change.POINTS_OF_INTEREST);
    }

    public void addNodeOfInterest(VisualNode in, double x) {
        if (x == 0.0) {
            removeNodeOfInterest(in);
            return;
        }
        if (new Double(x).equals(pointsOfInterest.get(in)))
            return;

        pointsOfInterest.put(in, x);
        graphChanged(Change.POINTS_OF_INTEREST);
    }

    public void removeNodeOfInterest(VisualNode in) {
        pointsOfInterest.remove(in);
        this.graphChanged(Change.POINTS_OF_INTEREST);
    }

    private VisualGroupNode rootNode;
    private HighlightMode highlightMode = HighlightMode.NEIGHBORS;
    boolean initialized = false;

    private void addNodeEdge(VisualNode n, VisualEdge e) {
        /*
           * if (nodeEdges.get(n) == null) { nodeEdges.put(n, new
           * HashSet<VisualEdge>()); } nodeEdges.get(n).add(e);
           */
        n.addEdge(e);
    }

    private void addGroup(VisualGroupNode group, VisualGroupNode parent,
                          HashMap<VisualNode, VisualNode> origToNew) {

        for (VisualNode on : group.getChildren()) {
            if (!(on instanceof VisualGroupNode)) {
                VisualNode newNode = null;
                if (on.getBMNode() != null) {
                    newNode = new VisualNode(this, on, parent);
                    bmNodeToVNode.put(on.getBMNode(), newNode);
                    vNodeToBMNode.put(newNode, on.getBMNode());

                } else {
                    newNode = new VisualNode(parent, on.getName());
                }
                origToNew.put(on, newNode);

            } else if (on instanceof VisualGroupNodeAutoEdges) {
                VisualGroupNodeAutoEdges nn = new VisualGroupNodeAutoEdges(
                        parent);
                origToNew.put(on, nn);
                addGroup((VisualGroupNode) on, nn, origToNew);
            } else if (on.getClass() == VisualGroupNode.class) {
                VisualGroupNode nn = new VisualGroupNode(parent);
                origToNew.put(on, nn);
                addGroup((VisualGroupNode) on, nn, origToNew);
            } else {
                assert false : "copying of nontrivial group nodes is not supported";
            }
        }
    }

    /**
     * Constructor used for copying.
     */
    public VisualGraph(VisualGroupNode group, Set<GraphObserver> observers) {
        this.showNodeTypes = group.getGraph().getShowNodeTypes();
        this.observers = new HashSet<GraphObserver>(observers);

        this.bmNodeToVNode = new HashMap<BMNode, VisualNode>();
        this.bmEdgeToVEdge = new HashMap<BMEdge, VisualEdge>();
        this.vNodeToBMNode = new HashMap<VisualNode, BMNode>();
        this.vEdgeToBMEdge = new HashMap<VisualEdge, BMEdge>();

        this.selected = new HashSet<VisualNode>();

        this.rootNode = new VisualGroupNode(this, null);

        HashMap<VisualNode, VisualNode> origToNew = new HashMap<VisualNode, VisualNode>();
        addGroup(group, rootNode, origToNew);
        // System.out.println("total " + origToNew.size() + " nodes");
        for (VisualNode on : group.getDescendants()) {
            VisualNode nn = origToNew.get(on);
            nn.setPos(on.getPos());
            for (VisualEdge oe : on.getEdges()) {
                VisualNode from = origToNew.get(oe.getFrom());
                VisualNode to = origToNew.get(oe.getTo());

                if (from == null || to == null) {
                    continue;
                }

                VisualEdge e = new VisualEdge(from, to, oe.getGoodness(),
                        oe.isSymmetric(), oe.getType());
                e.setPos(oe.getPos());
                if (from.getEdges().contains(e))
                    continue;

                BMEdge bme = (BMEdge) on.getGraph().getBMEntity(oe);
                if (bme != null) {
                    bmEdgeToVEdge.put(bme, e);
                    vEdgeToBMEdge.put(e, bme);
                }
                from.addEdge(e);
            }
        }

        postInit();
    }

    /**
     * Constructs empty graph.
     */
    public VisualGraph() {
        observers = new HashSet<GraphObserver>();

        bmNodeToVNode = new HashMap<BMNode, VisualNode>();
        bmEdgeToVEdge = new HashMap<BMEdge, VisualEdge>();
        vNodeToBMNode = new HashMap<VisualNode, BMNode>();
        vEdgeToBMEdge = new HashMap<VisualEdge, BMEdge>();

        selected = new HashSet<VisualNode>();

        rootNode = new VisualGroupNode(this, null);
        postInit();
    }

    public class GraphReadingException extends Exception {
        public GraphReadingException(String msg) {
            super(msg);
        }
    }

    private void initFromFile(String file) throws
            FileNotFoundException, GraphReadingException {
        File f = new File(file);
        if (!f.exists())
            throw new FileNotFoundException("File " + file + " not found.");
        fileName = f.getAbsolutePath();

        final StringBuffer errorBuffer = new StringBuffer();
        BMGraphReader reader = new BMGraphReader(
                new BMGraphReader.ErrorCallback() {

                    public void readerWarning(String message, String file,
                                              int line, int column) {
                    }

                    public void readerError(String message, String file,
                                            int line, int column) {
                        errorBuffer.append(file + ": line " + line + ", col " + column
                                + " " + message);
                    }
                });
        // BMGraph graph = BMGraphUtils.readBMGraph(file);
        reader.parseFile(f);
        BMGraph graph = reader.getGraph();

        if (errorBuffer.length() != 0) {
            throw new GraphReadingException(errorBuffer.toString() + "\n\nGraph couldn't be read.");
        }

        this.initFromBMGraph(graph);
    }

    public VisualGraph(String file, String goodnessAttribute) throws FileNotFoundException,
            GraphReadingException {
        this.goodnessAttribute = goodnessAttribute;
        this.initFromFile(file);
    }

    public VisualGraph(String file) throws FileNotFoundException, GraphReadingException {
        this.initFromFile(file);
    }

    public VisualGraph(BMGraph b) {
        this.initFromBMGraph(b);
    }

    public VisualGraph(BMGraph b, String goodnessAttribute) {
        this.goodnessAttribute = goodnessAttribute;
        this.initFromBMGraph(b);
    }

    private void initFromBMGraph(BMGraph bmGraph) {
        this.observers = new HashSet<GraphObserver>();

        this.rootNode = new VisualGroupNode(this, null, "Root");

        bmNodeToVNode = new HashMap<BMNode, VisualNode>();
        bmEdgeToVEdge = new HashMap<BMEdge, VisualEdge>();
        vNodeToBMNode = new HashMap<VisualNode, BMNode>();
        vEdgeToBMEdge = new HashMap<VisualEdge, BMEdge>();

        selected = new HashSet<VisualNode>();
        this.addBMGraph(bmGraph);

        this.postInit();
    }

    public void addBMGraph(BMGraph bmGraph) {
        initialized = false;
        try {
            // First preserve the information of each group's members in the groups List
            // This is for legacy group definitions.  They are handled at the end of this method.
            ArrayList<ArrayList<BMNode>> legacyGroups = extractLegacyGroups(bmGraph);

            bmGraph.ungroupAll();
            extractNonGroupNodes(bmGraph);

            Map<BMNode, VisualGroupNode> groupNodes = extractGroupNodes(bmGraph);

            HashSet<BMEdge> specialEdges = new HashSet<BMEdge>();
            extractGroupEdges(bmGraph, groupNodes, specialEdges);
            extractEdges(bmGraph, specialEdges);
            extractNonAutoGroupEdges(bmGraph);

            // Finally add legacy groups
            for (BMNode bmGroupNode : bmGraph.getGroupNodes()) {
                ArrayList<VisualNode> groupMembers = new ArrayList<VisualNode>();
                for (BMNode member : bmGraph.getMembersFor(bmGroupNode))
                    groupMembers.add(getNode(member));

                try {
                    this.makeGroup(groupMembers);
                } catch (GroupingFailedException e) {
                    Logging.error("graph_operation", "Conflicting groupings in graphs. not grouping");
                }
            }

            for (ArrayList<BMNode> group : legacyGroups) {
                ArrayList<VisualNode> groupMembers = new ArrayList<VisualNode>();
                for (BMNode m : group)
                    groupMembers.add(getNode(m));
                this.makeGroup(groupMembers);
            }

            this.postInit();
        } finally {
            //initialized = false;
        }

    }

    private void extractNonAutoGroupEdges(BMGraph bmGraph) {
        // Add group edges to non-auto group nodes
        for (BMEdge e : bmGraph.getEdges()) {
            if (e.getLinktype().equals("groupedge")) {
                VisualNode from = getNode(e.getFrom());
                VisualNode to = getNode(e.getTo());
                VisualGroupNode vgn = null;
                VisualNode other = null;
                if (from instanceof VisualGroupNode) {
                    vgn = (VisualGroupNode) from;
                    other = to;
                } else if (to instanceof VisualGroupNode) {
                    vgn = (VisualGroupNode) to;
                    other = from;
                } else {

                    continue;
                }
                String goodStr = e.get(goodnessAttribute);
                double goodness = goodStr == null ? 1 : Double
                        .parseDouble(goodStr);
                //vgn
                vgn.addConnection(other, goodness);
                //GroupEdgeUtils.makeGroupEdge(vgn, other, goodness);
            }
        }
    }

    private void extractEdges(BMGraph bmGraph, HashSet<BMEdge> specialEdges) {
        for (BMEdge e : bmGraph.getEdges()) {
            if (bmEdgeToVEdge.containsKey(e))
                continue;

            if (specialEdges.contains(e))
                continue;

            VisualNode from = getNode(e.getFrom());
            VisualNode to = getNode(e.getTo());
            if (from instanceof VisualGroupNode
                    || to instanceof VisualGroupNode)
                continue;

            String goodnessStr = e.get(this.goodnessAttribute);
            double goodness = goodnessStr == null ? 1 : Double.parseDouble(goodnessStr);

            VisualEdge ve = new VisualEdge(this, from, to, goodness,
                    e.isSymmetric(), e.getLinktype());

            String weightStr = e.get(this.weightAttribute);
            if (weightStr != null)
                ve.setWeight(VisualEdge.WeightType.WEIGHT, Double.parseDouble(weightStr));

            bmEdgeToVEdge.put(e, ve);
            vEdgeToBMEdge.put(ve, e);
            addNodeEdge(from, ve);
            addNodeEdge(to, ve);
        }
    }

    private void extractGroupEdges(BMGraph bmGraph, Map<BMNode, VisualGroupNode> groupNodes, HashSet<BMEdge> specialEdges) {
        for (BMEdge e : bmGraph.getEdges()) {
            if (e.getLinktype().matches("has_member|has_name|belongs_to|names")) {
                if (e.getLinktype().matches("belongs_to|has_name")) {
                    VisualNode n = this.getNode(e.getFrom());
                    VisualGroupNode vgn = groupNodes.get(e.getTo());
                    if (vgn == null)
                        continue;
                    n.setParent(vgn);
                } else {
                    VisualNode n = getNode(e.getTo());
                    VisualGroupNode vgn = groupNodes.get(e.getFrom());
                    if (vgn == null)
                        continue;
                    n.setParent(vgn);
                }
                specialEdges.add(e);
            }
        }
    }

    private Map<BMNode, VisualGroupNode> extractGroupNodes(BMGraph bmGraph) {
        Map<BMNode, VisualGroupNode> ret = new HashMap<BMNode, VisualGroupNode>();
        for (BMNode n : bmGraph.getNodes()) {
            if (n.getType().equals("Group")) {
                if (bmNodeToVNode.containsKey(n))
                    continue;

                String autoEdges = n.get("autoedges");
                VisualGroupNode vgn;
                if (autoEdges == null || autoEdges.equals("1")) {
                    vgn = new VisualGroupNodeAutoEdges(this, rootNode);
                    vgn.setName(n.getId());
                } else {
                    vgn = new VisualGroupNode(rootNode);
                    vgn.setName(n.getId());
                }
                String open = n.get("open");
                boolean groupOpen = true;
                if (open == null || open.equals("0"))
                    groupOpen = false;

                vgn.setOpen(groupOpen);
                ret.put(n, vgn);
                bmNodeToVNode.put(n, vgn);
            }
        }

        return ret;
    }

    private void extractNonGroupNodes(BMGraph bmGraph) {
        for (BMNode bmNode : bmGraph.getNodes()) {
            if (bmNode.getType().equals("Group"))
                continue;

            if (bmNodeToVNode.containsKey(bmNode))
                continue;

            VisualNode vNode = new VisualNode(this, bmNode, rootNode);

            bmNodeToVNode.put(bmNode, vNode);
            vNodeToBMNode.put(vNode, bmNode);
        }
    }

    private ArrayList<ArrayList<BMNode>> extractLegacyGroups(BMGraph bmGraph) {
        ArrayList<ArrayList<BMNode>> ret = new ArrayList<ArrayList<BMNode>>();
        for (BMNode bmGroupNode : bmGraph.getGroupNodes()) {
            ArrayList<BMNode> groupMembers = new ArrayList<BMNode>();
            for (BMNode member : bmGraph.getMembersFor(bmGroupNode)) {
                groupMembers.add(member);
            }
            ret.add(groupMembers);
        }


        return ret;
    }

    private void postInit() {
        Set<String> nodeTypes = new HashSet<String>();
        for (VisualNode n : getAllNodes()) {
            BMNode bmNode = n.getBMNode();
            if (bmNode != null && bmNode.getType() != null)
                nodeTypes.add(bmNode.getType());

            if (bmNode != null) {
                if (bmNode.getAttributes().containsKey("queryset")
                        || bmNode.getAttributes().containsKey("queryname")) {
                    addNodeOfInterest(n, 1);
                }
            } else {
                assert (vNodeToBMNode.get(n) == null);
            }
        }

        Logging.debug("graph_drawing", "Number of node types in graph: " + nodeTypes.size());
        if (nodeTypes.contains("Group"))
            nodeTypes.remove("Group");

        if (nodeTypes.size() < 2)
            this.showNodeTypes = false;
        else
            this.showNodeTypes = true;

        // Make sure correct basic labels (label and type) are set for nodes
        for (VisualNode node : this.getAllNodes())
            node.refreshLabels();

        this.graphChanged(Change.POINTS_OF_INTEREST);
        this.graphChanged(Change.STRUCTURE);
        updateGroupEdges();
        initialized = true;

        if (this.getAllEdges().size() > 0) {
            boolean allOne = true;
            for (VisualEdge e : this.getAllEdges()) {
                if (e.getWeight(VisualEdge.WeightType.PROBABILISTIC).value != 1.0)
                    allOne = false;
            }
            if (allOne)
                this.assignWeights(VisualEdge.WeightType.PROBABILISTIC);
        }
    }

    public VisualNode getNode(BMNode node) {
        return bmNodeToVNode.get(node);
    }

    VisualEdge getEdge(BMEdge node) {
        return bmEdgeToVEdge.get(node);
    }

    Collection<BMNode> getBMNodes() {
        return bmNodeToVNode.keySet();
    }

    Collection<BMEdge> getBMEdges() {
        return bmEdgeToVEdge.keySet();
    }

    public BMEntity getBMEntity(LayoutItem it) {
        if (it instanceof VisualNode)
            return vNodeToBMNode.get(it);
        else
            return vEdgeToBMEdge.get(it);
    }

    public VisualGroupNode getRootNode() {
        return rootNode;
    }

    public void paint(Graphics2D g) {
        VisualNode.setColoring(this.nodeColoring);
        for (LayoutItem i : getZOrderItems())
            i.paint(g);
    }

    public boolean isVisible(LayoutItem item) {
        return getNodes().contains(item) || getEdges().contains(item);
    }

    public boolean contains(LayoutItem item) {
        return getAllNodes().contains(item) || getAllEdges().contains(item);
    }

    public BMGraph getBMGraph() {
        BMGraph ret = new BMGraph();

        // Group nodes
        int groupNo = 0;
        HashMap<VisualNode, BMNode> newBMNodes = new HashMap<VisualNode, BMNode>();
        Collection<VisualNode> allNodes = getAllNodes();
        for (VisualNode vn : getAllNodes()) {
            if (vn instanceof VisualGroupNode)
                continue;
            BMNode n = vn.getBMNode();
            if (n != null) {
                Vec2 p = vn.getPos();
                n.put(BMGraphAttributes.POS_KEY, p.x + "," + p.y);
                ret.ensureHasNode(n);
                newBMNodes.put(bmNodeToVNode.get(n), n);
            }
        }
        for (BMEdge e : bmEdgeToVEdge.keySet()) {
            VisualNode from = getNode(e.getFrom());
            VisualNode to = getNode(e.getTo());
            if (allNodes.contains(from) && allNodes.contains(to)) {
                ret.ensureHasEdge(e);
            }
        }

        for (VisualNode n : getAllNodes()) {
            if (n instanceof VisualGroupNode) {
                VisualGroupNode vgn = (VisualGroupNode) n;
                if (vgn == getRootNode())
                    continue;
                String id = n.getName().equals("Group") ? "G" + groupNo++ : n
                        .getName();
                BMNode bmGroupNode = new BMNode("Group", id);
                bmGroupNode.setAttributes(new HashMap<String, String>());
                if (n instanceof VisualGroupNodeAutoEdges)
                    bmGroupNode.put("autoedges", "1");
                else {
                    bmGroupNode.put("autoedges", "0");
                }
                if (vgn.isOpen()) {
                    bmGroupNode.put("open", "1");
                } else {
                    bmGroupNode.put("open", "0");
                }

                newBMNodes.put(vgn, bmGroupNode);
            }
        }

        // group membership edges
        for (VisualNode n : newBMNodes.keySet()) {
            if (!(n instanceof VisualGroupNode))
                continue;
            VisualGroupNode vgn = (VisualGroupNode) n;
            for (VisualNode c : vgn.getChildren()) {

                BMEdge member = new BMEdge(newBMNodes.get(vgn),
                        newBMNodes.get(c), "has_member");
                ret.ensureHasEdge(member);
            }
        }

        // edges from nonauto groupnodes
        for (VisualNode n : newBMNodes.keySet()) {
            if (n instanceof VisualGroupNode
                    && !(n instanceof VisualGroupNodeAutoEdges)) {
                VisualGroupNode vgn = (VisualGroupNode) n;
                for (VisualEdge e : vgn.getEdges()) {
                    BMEdge be = new BMEdge(newBMNodes.get(n), newBMNodes.get(e
                            .getOther(n)), "groupedge");
                    be.setAttributes(new HashMap<String, String>());
                    be.put(BMGraphAttributes.GOODNESS_KEY, "" + e.getGoodness());
                    ret.ensureHasEdge(be);
                }
            }
        }
        for (VisualNode n : getNodesOfInterest().keySet()) {
            BMNode bmn = newBMNodes.get(n);
            if (bmn != null)
                ret.setSpecial(bmn, true);
        }

        // groupNodes.put(bmGroupNode, vgn);
        // BMEdge has_child = new BMEdge(bmG
        // group edges

        return ret;
    }

    private void getVisibleNodes(VisualNode ng, Collection<VisualNode> items) {
        if (ng instanceof VisualGroupNode) {

            VisualGroupNode vgn = (VisualGroupNode) ng;

            if (vgn.isOpen()) {
                for (VisualNode n : vgn.getChildren()) {
                    if (n.isVisible() == false) {
                        System.out.println("ERROR ERROR parent is open but");
                        System.out.println("n.vis = " + n.isVisible());
                        System.out.println("p.vis = " + vgn.isVisible());
                        System.out.println("p.open = " + vgn.isOpen());
                    }
                    getVisibleNodes(n, items);

                }

                return;
            }
        }
        items.add(ng);
    }


    public ArrayList<LayoutItem> getZOrderItems() {
        ArrayList<LayoutItem> items = new ArrayList<LayoutItem>(getEdges());

        Comparator<LayoutItem> zComparator = new Comparator<LayoutItem>() {
            public int compare(LayoutItem o1, LayoutItem o2) {
                return Double.compare(o1.getZ(), o2.getZ());
            }
        };
        Collections.sort(items, zComparator);
        Collection<VisualNode> vns = getNodes();
        assert (vns.size() >= 0);

        ArrayList<LayoutItem> nodes = new ArrayList<LayoutItem>(vns);
        Collections.sort(nodes, zComparator);

        items.addAll(nodes);

        return items;
    }

    public void pullUp(LayoutItem i) {
        for (LayoutItem l : getZOrderItems()) {
            l.setZ(l.getZ() * 0.99);
        }
        i.setZ(1);
        visiblesCached = false;
    }

    public void selectionChanged() {
        Logging.debug("graph_operation", "selectionChanged()");
        for (GraphObserver o : new ArrayList<GraphObserver>(observers))
            o.selectionChanged(this);
    }

    /**
     * You must remember to call graph.selectionChanged() on any changes to selections!
     *
     * @return boolean true if anything was selected before, false otherwise
     */
    public boolean clearSelected() {
        int count = 0;
        for (VisualNode node : new HashSet<VisualNode>(selected)) {
            node.setSelected(false);
            count++;
        }

        if (count > 0) {
            this.selected.clear();
            return true;
        }
        return false;
    }

    /**
     * You must remember to call graph.selectionChanged() on any changes to selections!
     *
     * @param n
     */
    public void addSelected(VisualNode n) {
        selected.add(n);
        n.setSelected(true);
    }

    /**
     * You must remember to call graph.selectionChanged() on any changes to selections!
     *
     * @param n
     */
    public void removeSelected(VisualNode n) {
        selected.remove(n);
        n.setSelected(false);
    }

    static class GroupingFailedException extends RuntimeException {
        public GroupingFailedException(String why) {
            super(why);
        }
    }

    public void makeGroup(Collection<VisualNode> nodes) {
        VisualGroupNode parent = null;
        for (VisualNode node : nodes) {
            if (parent != null && parent != node.getParent()) {
                throw new GroupingFailedException(
                        "Cannot group nodes from different groups");
            }
            parent = node.getParent();
        }
        this.disableObservers();
        VisualGroupNode newGroup = new VisualGroupNodeAutoEdges(this, parent);
        for (VisualNode node : nodes) {
            node.setParent(newGroup);
        }

        newGroup.setOpen(false);
        this.updateGroupEdges();
        this.enableObservers();
    }

    public void groupSelected() {
        if (selected.size() < 2)
            return;

        makeGroup(selected);

        clearSelected();
        selectionChanged();
    }

    private boolean[] sendingChange = {false, false, false, false};
    private boolean[] changesNotSent = {false, false, false, false};

    public void graphChanged(final Change ch) {
        if (ch == Change.VISIBILITY || ch == Change.STRUCTURE)
            visiblesCached = false;

        if (!observersEnabled) {
            changesNotSent[ch.ordinal()] = true;
            return;
        }

        if (sendingChange[ch.ordinal()]) {
            return;
        }
        assert groupEdgeUpdateOnGoing == false;

        if (!initialized)
            return;

        sendingChange[ch.ordinal()] = true;
        version[ch.ordinal()]++;

        switch (ch) {
            case POINTS_OF_INTEREST:
                break;
            case STRUCTURE:
            case VISIBILITY:
                version[Change.VISIBILITY.ordinal()]++;
            default:
        }
        final Collection<GraphObserver> oldObs = new ArrayList<GraphObserver>(
                observers);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // System.out.println("sending change events "+ch);
                if (ch == Change.STRUCTURE)
                    VisualGraph.this.updateGroupEdges();
                for (GraphObserver obs : oldObs) {
                    switch (ch) {
                        case POINTS_OF_INTEREST:
                            //System.out.println("SENDING POICHANGED");
                            obs.pointsOfInterestsChanged(VisualGraph.this);
                            break;
                        case STRUCTURE:
                            obs.graphStructureChanged(VisualGraph.this);
                            break;
                        case VISIBILITY:
                            obs.visibleNodesChanged(VisualGraph.this);
                            break;
                        case COLORS:
                            obs.colorsChanged(VisualGraph.this);
                            break;
                    }
                }
                sendingChange[ch.ordinal()] = false;
            }
        });

    }

    protected void colorsChanged() {
        for (GraphObserver o : observers) {
            o.colorsChanged(this);
        }
    }

    public void addObserver(GraphObserver o) {
        this.observers.add(o);
    }

    public void removeObserver(GraphObserver o) {
        observers.remove(o);
    }

    Collection<VisualEdge> getNodeEdges(VisualNode n) {
        return n.getEdges();
    }

    public Set<VisualNode> getSelected() {
        return Collections.unmodifiableSet(selected);
    }

    public void setHighlightMode(HighlightMode highlightMode) {
        this.highlightMode = highlightMode;
        colorsChanged();
    }

    public HighlightMode getHighlightMode() {
        return highlightMode;
    }

    public Collection<VisualNode> getNodeNeighbors(VisualNode node) {
        HashSet<VisualNode> neighbors = new HashSet<VisualNode>();

        for (VisualEdge e : this.getNodeEdges(node)) {
            VisualNode node2 = e.getOther(node);
            neighbors.add(node2.getVisibleAncestor());
        }
        return neighbors;
    }

    public Collection<LayoutItem> getNeighbors(LayoutItem i) {
        ArrayList<LayoutItem> neighbors = new ArrayList<LayoutItem>();

        if (i instanceof VisualNode) {
            VisualNode node = (VisualNode) (i);

            for (VisualEdge e : this.getNodeEdges(node)) {
                VisualNode node2 = e.getOther(node);
                neighbors.add(node2.getVisibleAncestor());
            }

        } else if (i instanceof VisualEdge) {
            VisualEdge edge = (VisualEdge) i;
            neighbors.add(edge.getTo());
            neighbors.add(edge.getFrom());
        }
        return neighbors;
    }

    public void setHighlighted(LayoutItem item) {
        for (LayoutItem i : getAllNodes()) {
            i.setHighlight(false);
            i.setNeighborHighlight(false);
        }
        for (LayoutItem i : getAllEdges()) {
            i.setHighlight(false);
            i.setNeighborHighlight(false);
        }
        if (item == null)
            return;
        Collection<LayoutItem> nhl = new ArrayList<LayoutItem>();
        item.setHighlight(true);

        switch (highlightMode) {
            case GROUP:
                if (item instanceof VisualNode) {
                    VisualNode n = (VisualNode) item;
                    if (n.getParent() != null) {
                        nhl = new ArrayList<LayoutItem>(n.getParent().getChildren());
                    }
                    break;
                }
                // if item is edge, just highlight its neighbors
            case NEIGHBORS:
                nhl = getNeighbors(item);
        }

        for (LayoutItem n : nhl) {
            n.setNeighborHighlight(true);
        }
        colorsChanged();
    }

    public void destroyGroup(VisualGroupNode vgn) {
        // never destroy the rootNode.
        if (vgn.getParent() == null)
            return;

        HashSet<VisualEdge> edgesToRemove = new HashSet<VisualEdge>(
                vgn.getEdges());

        for (VisualEdge e : edgesToRemove) {
            e.getFrom().removeEdge(e);
            e.getTo().removeEdge(e);
        }

        ArrayList<VisualNode> children = new ArrayList<VisualNode>(
                vgn.getChildren());
        for (VisualNode c : children)
            c.setParent(vgn.getParent());
        vgn.getParent().removeChild(vgn);

    }

    private Set<VisualNode> nodeCache;
    private Set<VisualEdge> edgeCache;
    private boolean visiblesCached;

    private void updateCaches() {
        nodeCache = new LinkedHashSet<VisualNode>();
        getVisibleNodes(rootNode, nodeCache);
        if (hiddenNodes != null)
            nodeCache.removeAll(hiddenNodes);

        edgeCache = new LinkedHashSet<VisualEdge>();
        for (VisualNode vn : nodeCache) {
            for (VisualEdge e : vn.getEdges()) {
                if (nodeCache.contains(e.getOther(vn))) {
                    edgeCache.add(e);
                }
            }
        }

        if (hiddenEdges != null)
            edgeCache.removeAll(hiddenEdges);

        visiblesCached = true;
    }

    public void assignWeights(VisualEdge.WeightType type) {
        if (type == VisualEdge.WeightType.PROBABILISTIC) {
            Logging.debug("graph_reading", "Assigning probabilistic values to nodes in graph " + this);
            double maxWeight = Double.MIN_VALUE;
            double minWeight = Double.MAX_VALUE;

            for (VisualEdge edge : this.getAllEdges()) {
                VisualEdge.Weight w = edge.getWeight(VisualEdge.WeightType.WEIGHT);
                if (w == null)
                    w = edge.getWeight(VisualEdge.WeightType.FLOW);
                if (w == null)
                    continue;

                double edgeWeight = w.value;

                if (edgeWeight > maxWeight)
                    maxWeight = edgeWeight;
                if (edgeWeight < minWeight)
                    minWeight = edgeWeight;
            }

            for (VisualEdge edge : this.getAllEdges()) {
                VisualEdge.Weight w = edge.getWeight(VisualEdge.WeightType.WEIGHT);

                if (w == null)
                    w = edge.getWeight(VisualEdge.WeightType.FLOW);
                if (w == null) {
                    edge.setWeight(type, 1.0);
                    continue;
                }

                edge.setWeight(type, w.value / maxWeight * 1.0);
            }
        }
    }

    public void deleteEdge(VisualEdge e) {
        e.getFrom().removeEdge(e);
    }

    public void deleteNode(VisualNode n) {
        removeNodeOfInterest(n);
        if (n instanceof VisualGroupNode) {
            VisualGroupNode vgn = (VisualGroupNode) n;
            HashSet<VisualNode> del = new HashSet<VisualNode>(vgn.getChildren());
            for (VisualNode c : del) {
                deleteNode(c);
            }
        }
        HashSet<VisualEdge> er = new HashSet<VisualEdge>(n.getEdges());
        for (VisualEdge e : er) {
            n.removeEdge(e);
        }
        /*
           * BMNode bmn = n.getBMNode(); if (bmn != null) { HashSet<BMEdge> bmer =
           * new HashSet<BMEdge>(bmgraph .getNodeEdges(bmn)); for (BMEdge e :
           * bmer) bmgraph.removeEdge(e); bmgraph.removeNode(bmn); }
           */
        if (n.getParent() != null)
            n.getParent().removeChild(n);
    }

    private void gatherNodes(VisualNode n, HashSet<VisualNode> s) {
        s.add(n);
        if (n instanceof VisualGroupNode) {
            for (VisualNode c : ((VisualGroupNode) n).getChildren()) {
                gatherNodes(c, s);
            }
        }
    }

    public Collection<VisualNode> getAllNodes() {
        HashSet<VisualNode> ret = new HashSet<VisualNode>();
        gatherNodes(rootNode, ret);
        return ret;
    }

    public Collection<VisualEdge> getAllEdges() {
        HashSet<VisualEdge> ed = new HashSet<VisualEdge>();
        for (VisualNode n : getAllNodes()) {
            ed.addAll(n.getEdges());
        }
        return ed;
    }

    public Collection<VisualNode> getNodes() {
        if (!visiblesCached)
            updateCaches();
        return nodeCache;
    }

    public Collection<VisualEdge> getEdges() {
        if (!visiblesCached)
            updateCaches();
        return edgeCache;
    }

    public long getVersion(Change ch) {
        return version[ch.ordinal()];
    }

    /**
     * returns long value which combines all version[] parts. This number
     * changes when any version part changes.
     *
     * @return
     */
    public long getCombinedVersion() {
        long ret = 0xfaf18c5db2a3984L;
        final long mul = 0x4f3c13c4b9a3f84L;
        for (int i = 0; i < version.length; i++) {
            ret = mul * version[i] + Long.rotateLeft(ret, 31);
        }
        return ret;
    }

    public Set<String> getAvailableNodeLabels() {
        Set<String> ret = new HashSet<String>();

        for (VisualNode vn : this.getAllNodes()) {
            BMNode n = (BMNode) this.getBMEntity(vn);
            if (n == null)
                continue;
            for (String s : n.getAttributes().keySet())
                ret.add(s);
        }

        return ret;
    }

    public Set<String> getAvailableEdgeLabels() {
        Set<String> ret = new HashSet<String>();

        for (VisualEdge e : getEdges()) {
            BMEdge n = (BMEdge) getBMEntity(e);
            if (n == null) {
                continue;
            }
            for (String s : n.getAttributes().keySet())
                ret.add(s);
        }

        return ret;
    }

    private boolean groupEdgeUpdateOnGoing = false;

    public void updateGroupEdges() {
        groupEdgeUpdateOnGoing = true;

        ArrayList<VisualEdge> removables = new ArrayList<VisualEdge>();
        for (VisualNode n : getAllNodes())
            for (VisualEdge e : n.getEdges())
                if (e.getFrom() instanceof VisualGroupNode || e.getTo() instanceof VisualGroupNode)
                    removables.add(e);

        for (VisualEdge e : removables)
            e.getFrom().removeEdge(e);

        for (VisualNode n : getAllNodes())
            if (n instanceof VisualGroupNode)
                ((VisualGroupNode) n).createEdges();

        groupEdgeUpdateOnGoing = false;
    }


    public void releaseAll() {
        Logging.info("enduser", "Unpin all items.");

        for (VisualEdge e : getEdges())
            e.setPositionFixed(false);

        for (VisualNode e : getNodes())
            e.setPositionFixed(false);

        this.graphChanged(Change.VISIBILITY);
    }

    public void pinAll() {
        Logging.info("enduser", "Pinning all nodes.");

        for (VisualNode e : getNodes())
            e.setPositionFixed(true);

        this.graphChanged(Change.VISIBILITY);
    }

    public void save(File f) throws IOException {
        BMGraphUtils.writeBMGraph(this.getBMGraph(), f);
    }

    public VisualGraph copy() {
        Logging.info("graph_operation", "Graph copy.");
        VisualGraph copy = new VisualGraph(this.getRootNode(), this.observers);
        Logging.debug("graph_operation", "Copied graph: " + copy);

        return copy;
    }

    private boolean observersEnabled = true;

    public void disableObservers() {
        observersEnabled = false;
    }

    public void enableObservers() {
        observersEnabled = true;
        for (Change ch : Change.values()) {
            if (changesNotSent[ch.ordinal()])
                graphChanged(ch);
        }
    }

    // Setting the coloring here at all might be a performance hack.
    // The semantics of various operations need to be adapted to be more Pipeline-centric from now being
    // VisualGraph-centric.
    public void setNodeColoring(NodeColoring nodeColoring) {
        this.nodeColoring = nodeColoring;
        colorsChanged();
    }

    public void zoomTo(Collection<LayoutItem> items) {
        for (GraphObserver observer : this.observers)
            observer.zoomRequested(this, items);
    }

    /**
     * Sets nodes as hidden. These nodes won't be included in getNodes or
     * getZOrderItems, but will be shown in getAllNodes.
     *
     * @param hidden
     */
    public void setHiddenNodes(Collection<VisualNode> hidden) {
        this.hiddenNodes = new HashSet<VisualNode>(hidden);
        this.graphChanged(Change.VISIBILITY);
    }

    public void hideNode(VisualNode hidden) {
        this.hiddenNodes.add(hidden);
        this.graphChanged(Change.VISIBILITY);
    }

    public void hideNodes(Collection<VisualNode> hidden) {
        this.hiddenNodes.addAll(hidden);
        this.graphChanged(Change.VISIBILITY);
    }

    public void unHideNode(VisualNode hidden) {
        this.hiddenNodes.remove(hidden);
        this.graphChanged(Change.VISIBILITY);
    }

    public void unHideNodes(Collection<VisualNode> hidden) {
        this.hiddenNodes.removeAll(hidden);
        this.graphChanged(Change.VISIBILITY);
    }


    /**
     * Sets edges as hidden. These edges won't be included in getEdges or
     * getZOrderItems, but will be shown in getAllEdges.
     *
     * @param hidden
     */
    public void setHiddenEdges(Collection<VisualEdge> hidden) {
        this.hiddenEdges = new HashSet<VisualEdge>(hidden);
        this.graphChanged(Change.VISIBILITY);
    }

    public void hideEdge(VisualEdge hidden) {
        this.hiddenEdges.add(hidden);
        this.graphChanged(Change.VISIBILITY);
    }

    public void hideEdges(Collection<VisualEdge> hidden) {
        this.hiddenEdges.addAll(hidden);
        this.graphChanged(Change.VISIBILITY);
    }

    public void unHideEdge(VisualEdge hidden) {
        this.hiddenEdges.remove(hidden);
        this.graphChanged(Change.VISIBILITY);
    }

    public void unHideEdges(Collection<VisualEdge> hidden) {
        this.hiddenEdges.removeAll(hidden);
        this.graphChanged(Change.VISIBILITY);
    }


    public Collection<VisualNode> getHiddenNodes() {
        return hiddenNodes;
    }

    public String getFileName() {
        return fileName;
    }

    public Collection<VisualEdge> getHiddenEdges() {
        return hiddenEdges;
    }

    public boolean getShowNodeTypes() {
        return this.showNodeTypes;
    }

    /**
     * Disables thumbtack drawing when set on.  Should only be set on before drawing on a PNG surface.
     *
     * @param printMode
     */
    public void setPrintMode(boolean printMode) {
        this.printMode = printMode;
    }

    public boolean getPrintMode() {
        return this.printMode;
    }

    public String toString() {
        return this.getClass().getName() + " " + this.getAllNodes().size() + " nodes, " + this.getAllEdges().size() + " edges.";
    }
}
