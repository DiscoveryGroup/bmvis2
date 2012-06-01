package biomine.bmvis2.pipeline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import biomine.bmgraph.BMEdge;
import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMNode;
import biomine.bmvis2.*;
import biomine.bmvis2.layout.InitialLayout;
import biomine.bmvis2.pipeline.GraphOperation.GraphOperationException;
import biomine.bmvis2.pipeline.sources.GraphSource;
import biomine.bmvis2.pipeline.sources.StreamGraphSource;
import biomine.bmvis2.ui.GraphControls;
import biomine.bmvis2.ui.GraphView;
import biomine.bmvis2.ui.GraphVisualizer;

/**
 * Settings in this component
 * <p/>
 * 1. Queries
 * <p/>
 * 2. Groupings
 * <p/>
 * 3. Visibility
 * <p/>
 * 4. Colors
 * <p/>
 * 5....
 */

public class Pipeline implements GraphObserver, SettingsChangeCallback {
    private Vis vis;
    private GraphVisualizer visualizer;
    private GraphControls controls;

    private File loadedFile = null; // for JSON operations loading
    private LinkedHashSet<String> sourceDatabases = new LinkedHashSet<String>();

    private Map<VisualNode, Double> nodesOfInterest = new HashMap<VisualNode, Double>();
    private Map<String, VisualNode> idToNode = new HashMap<String, VisualNode>();
    private Map<String, VisualNode> nameToNode = new HashMap<String, VisualNode>();
    private Set<VisualNode> knownNodeCache = new HashSet<VisualNode>();

    private Collection<GraphSource> previousSources = Collections.EMPTY_SET;
    private List<GraphSource> currentSources = new ArrayList<GraphSource>();
    private VisualGraph vanillaGraph = null;
    private VisualGraph currentGraph = VisualGraph.EMPTY;
    private boolean changesRunning;

    private Collection<GraphOperation> previousStructuralOps = Collections.EMPTY_LIST;
    private List<StructuralOperation> currentStructuralOps = new ArrayList<StructuralOperation>();

    private Map<String, Vec2> nodePosCache = new HashMap<String, Vec2>();
    private Map<String, Vec2> edgePosCache = new HashMap<String, Vec2>();

    ArrayList<GraphOperation> fastOps = new ArrayList<GraphOperation>();

    public Pipeline(GraphView visualizer, GraphControls controls, Vis vis) {
        this.vis = vis;
        this.visualizer = visualizer;
        this.controls = controls;
        this.controls.setPipeline(this);
        this.visualizer.setPipeline(this);
    }

    public Pipeline(GraphControls controls, Vis vis) {
        this.vis = vis;
        this.controls = controls;
        this.controls.setPipeline(this);
    }

    public void saveOperations(File saveFile) {
        List<GraphOperation> ops = new ArrayList<GraphOperation>();

        ops.addAll(currentSources);
        ops.addAll(currentStructuralOps);
        ops.addAll(fastOps);

        try {
            GraphOperationSerializer.saveList(ops, saveFile, getNodesOfInterestStringMap());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(vis, e.getMessage(),
                    "Error while saving file", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addOperations(List<GraphOperation> newOps) {
        ArrayList<StructuralOperation> oldStructOps = new ArrayList<StructuralOperation>(currentStructuralOps);
        for (StructuralOperation op : oldStructOps)
            removeStructuralOp(op);
        ArrayList<GraphOperation> oldFastOps = new ArrayList<GraphOperation>(
                fastOps);
        for (GraphOperation go : oldFastOps)
            removeFastOp(go);


        for (GraphOperation newOp : newOps) {
            if (newOp instanceof GraphSource)
                addSource((GraphSource) newOp);
            else if (newOp instanceof GrouperOperation)
                addStructuralOp((StructuralOperation) newOp);
            else if (newOp instanceof StructuralOperation)
                addStructuralOp((StructuralOperation) newOp);
            else
                addFastOp(newOp);
        }
    }

    public void addNodesOfInterest(Map<VisualNode, Double> map) {
        for (VisualNode node : map.keySet())
            if (this.getCurrentGraph().getAllNodes().contains(node))
                this.nodesOfInterest.put(node, map.get(node));
    }

    public void addNodeIdsOfInterest(Map<String, Double> map) {
        for (VisualNode node : currentGraph.getAllNodes()) {
            if (map.containsKey(node.getId())) {
                this.nodesOfInterest.put(node, map.get(node));
            }
        }
    }

    public void addNodeOfInterest(VisualNode node, Double interestingness) {
        Logging.debug("pipeline", "New POI " + node + ": " + interestingness);
        this.nodesOfInterest.put(node, interestingness);
        this.getCurrentGraph().addNodeOfInterest(node, interestingness);
    }

    public void removeNodeOfInterest(VisualNode node) {
        this.nodesOfInterest.put(node, 0.0);
        this.getCurrentGraph().removeNodeOfInterest(node);
    }

    public void removeNodeOfInterest(String s) {
        for (VisualNode node : this.getCurrentGraph().getAllNodes()) {
            if (!node.getId().equalsIgnoreCase(s) && !node.getName().equalsIgnoreCase(s)) {
                this.nodesOfInterest.put(node, 0.0);
                this.getCurrentGraph().removeNodeOfInterest(node);
            }
        }
    }

    public Double getInterestingness(VisualNode node) {
        Double score = this.nodesOfInterest.get(node);
        if (score == null)
            return 0.0;
        else
            return score;
    }

    public Map<VisualNode, Double> getPositiveNodesOfInterest() {
        HashMap<VisualNode, Double> ret = new HashMap<VisualNode, Double>();

        for (VisualNode node : this.nodesOfInterest.keySet()) {
            Double score = this.nodesOfInterest.get(node);
            if (score == null || score <= 0.0)
                continue;
            else
                ret.put(node, score);
        }

        return ret;
    }

    public Map<VisualNode, Double> getNegativeNodesOfInterest() {
        HashMap<VisualNode, Double> ret = new HashMap<VisualNode, Double>();

        for (VisualNode node : this.nodesOfInterest.keySet()) {
            Double score = this.nodesOfInterest.get(node);
            if (score == null || score >= 0.0)
                continue;
            else
                ret.put(node, score);
        }

        return ret;
    }

    public Set<VisualNode> getNeutralNodesOfInterest() {
        Set<VisualNode> ret = new HashSet<VisualNode>();

        for (VisualNode node : this.getCurrentGraph().getAllNodes()) {
            Double score = this.nodesOfInterest.get(node);
            if (score == null)
                ret.add(node);
        }

        return ret;
    }

    public Map<String, Double> getNodesOfInterestStringMap() {
        HashMap<String, Double> map = new HashMap<String, Double>();
        for (VisualNode node : currentGraph.getNodesOfInterest().keySet()) {
            map.put(node.getId(), currentGraph.getNodesOfInterest().get(node));
        }
        return map;
    }

    public void clearNodesOfInterest() {
        this.nodesOfInterest.clear();
    }


    public void loadOperations(String json) {
        Logging.info("graph_operation", "Loading operations from JSON.");

        try {
            List<GraphOperation> newOps = GraphOperationSerializer
                    .loadList(json);
            Logging.info("graph_operation", "Adding " + newOps.size() + " new operations.");

            Map<String, Double> oldNOI = getNodesOfInterestStringMap();
            addOperations(newOps);
            this.clearNodesOfInterest();
            addNodeIdsOfInterest(oldNOI);
            addNodeIdsOfInterest(GraphOperationSerializer.getNodesOfInterest(json));
            Logging.info("graph_operation", "JSON operations loaded.");
        } catch (GraphOperationSerializationException e) {
            e.printStackTrace();
            System.out.println(e);
            JOptionPane.showMessageDialog(vis, e.getMessage(),
                    "Error while loading file", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadOperations(File loadFile) {
        try {
            loadedFile = loadFile;
            List<GraphOperation> newOps = GraphOperationSerializer
                    .loadList(loadFile);

            Map<String, Double> oldNOI = getNodesOfInterestStringMap();
            addOperations(newOps);

            this.clearNodesOfInterest();
            addNodeIdsOfInterest(oldNOI);
            addNodeIdsOfInterest(GraphOperationSerializer.getNodesOfInterest(loadFile));
            addNodeIdsOfInterest(GraphOperationSerializer.getNodesOfInterest(loadFile));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(vis, e.getMessage(),
                    "Error while loading file", JOptionPane.ERROR_MESSAGE);
        } catch (GraphOperationSerializationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(vis, e.getMessage(),
                    "Error while loaGding file", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addFastOp(final GraphOperation op) {
        if (fastOps.contains(op))
            return;
        Logging.debug("pipeline", "Adding fast op " + op);

        fastOps.add(op);
        settingsChanged(false);
        controls.updateControls();
    }

    public void removeFastOp(final GraphOperation op) {
        if (!fastOps.contains(op))
            return;

        fastOps.remove(op);
        settingsChanged(false);
        controls.updateControls();
    }

    public void addSource(GraphSource src) {
        Logging.debug("pipeline", "Adding GraphSource " + src + ".");
        if (!currentSources.contains(src)) {
            currentSources.add(src);

            this.settingsChanged(true);
            this.controls.updateControls();
        }
    }

    public void removeSource(GraphSource src) {
        if (currentSources.contains(src)) {
            currentSources.remove(src);
            settingsChanged(true);
            controls.updateControls();
        }
    }

    public void addStructuralOp(final StructuralOperation op) {
        if (!this.currentStructuralOps.contains(op)) {
            Logging.debug("pipeline", "addStructuralOp(" + op + ")");
            this.currentStructuralOps.add(op);

            settingsChanged(false);
            controls.updateControls();
        }
    }

    public void removeStructuralOp(StructuralOperation op) {
        if (currentStructuralOps.contains(op)) {
            currentStructuralOps.remove(op);
            settingsChanged(true);
            controls.updateControls();
        }
    }

    public JComponent getComponent(GraphOperation graphOp) {
        boolean redoStructuralOps = false;

        if (currentStructuralOps.contains(graphOp))
            redoStructuralOps = true;
        else if (currentSources.contains(graphOp))
            redoStructuralOps = true;
        else if (fastOps.contains(graphOp))
            redoStructuralOps = false;
        else
            assert (false) : "operation was not in the pipeline";
        return graphOp
                .getSettingsComponent(rerunCallback(redoStructuralOps), currentGraph);
    }

    private SettingsChangeCallback rerunCallback(final boolean redoStructuralOps) {
        return new SettingsChangeCallback() {
            public void settingsChanged(boolean redoStructOps) {
                Pipeline.this.settingsChanged(redoStructuralOps);
            }
        };
    }

    private void rebuildStringCaches() {
        this.idToNode.clear();
        this.nameToNode.clear();
        for (VisualNode node : currentGraph.getAllNodes()) {
            this.idToNode.put(node.getId(), node);
            this.nameToNode.put(node.getName(), node);
        }
    }

    public void settingsChanged(boolean rerunStructuralOps) {
        if (changesRunning)
            return;

        if (this.previousSources.size() == 0 && this.currentSources.size() == 0)
            return;

        try {
            changesRunning = true;
            // try-block to reset changesRunning, whatever happens

            VisualGraph.HighlightMode oldHighlightMode = this.getCurrentGraph().getHighlightMode();

            // Persist points of interest and positions over a pipeline settingsChanged run
            Map<String, Double> pointsOfInterestCache = new HashMap<String, Double>();
            for (VisualNode node : this.currentGraph.getNodesOfInterest().keySet())
                pointsOfInterestCache.put(node.getId(), this.currentGraph.getNodesOfInterest().get(node));

            Logging.debug("pipeline", "Persisting " + pointsOfInterestCache.size() + " points of interest!");

            for (VisualNode node : this.currentGraph.getAllNodes())
                nodePosCache.put(node.getId(), node.getPos());
            for (VisualEdge edge : this.currentGraph.getAllEdges())
                edgePosCache.put(edge.toString(), edge.getPos());

            Logging.debug("pipeline", "Sources before:");
            for (GraphSource s : this.previousSources)
                Logging.debug("pipeline", " < " + s);

            Logging.debug("pipeline", "Sources after:");
            for (GraphSource s : this.currentSources)
                Logging.debug("pipeline", " > " + s);


            VisualGraph nextCurrentGraph;
            // Update sources if there's a new source
            boolean sourcesChanged = false;
            boolean sourceAdded = false;
            boolean wasEmpty = this.previousSources.isEmpty();

            if (this.currentSources.containsAll(this.previousSources) &&
                    this.previousSources.containsAll(currentSources))
                sourcesChanged = false;
            else
                sourcesChanged = true;

            // Check if only sources were added
            if (this.currentSources.containsAll(this.previousSources) &&
                    !this.previousSources.containsAll(this.currentSources))
                sourceAdded = true;

            Logging.debug("pipeline", "sourcesChanged: " + sourcesChanged + ", redoStructOps: " + rerunStructuralOps);
            if (sourcesChanged || vanillaGraph == null) {
                Logging.info("pipeline", "Rebuilding the whole graph.");
                rerunStructuralOps = true;
                sourceDatabases.clear();
                for (GraphSource src : currentSources)
                    sourceDatabases.addAll(src.getSourceDatabases());

                nextCurrentGraph = new VisualGraph();

                for (GraphSource src : currentSources) {
                    try {
                        nextCurrentGraph.addBMGraph(src.getBMGraph());
                    } catch (GraphOperationException e) {
                        showFetchError(e.getMessage());
                        removeSource(src);
                    }

                }
                this.vanillaGraph = nextCurrentGraph.copy();
                Logging.info("pipeline", "Graph rebuilt.");
            } else if (!sourcesChanged && rerunStructuralOps) {
                Logging.info("pipeline", "Reusing previous vanilla graph.");
                nextCurrentGraph = this.vanillaGraph.copy();
            } else {
                nextCurrentGraph = null;
            }

            this.previousSources = new HashSet<GraphSource>(this.currentSources);
            this.previousStructuralOps = new ArrayList<GraphOperation>(this.currentStructuralOps);

            if (nextCurrentGraph != null)
                for (VisualNode node : nextCurrentGraph.getAllNodes())
                    this.knownNodeCache.add(node);

            // Re-run structural ops
            if (rerunStructuralOps) {
                if (!wasEmpty)
                    this.restorePointsOfInterestAndPositions(nextCurrentGraph, pointsOfInterestCache);
                Logging.debug("pipeline", "Running structural ops: " + getCurrentStructuralOps());
                for (GraphOperation op : getCurrentStructuralOps()) {
                    try {
                        op.doOperation(nextCurrentGraph);
                    } catch (GraphOperationException e) {
                        Logging.error("pipeline", "Structural ops should not throw GraphOperationExceptions");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }

                BMGraph g = nextCurrentGraph.getBMGraph();

                Set<BMNode> positionsAssigned = new HashSet<BMNode>();
                for (BMNode node : g.getNodes())
                    if (node.get(BMGraphAttributes.POS_KEY) != null)
                        positionsAssigned.add(node);

                if (positionsAssigned.size() > 0)
                    InitialLayout.solvePositions(g);
                else {
                    Queue<BMNode> processable = new LinkedList<BMNode>();
                    HashSet<BMNode> processed = new HashSet<BMNode>();
                    for (BMNode node : positionsAssigned)
                        processable.add(node);

                    while (processable.size() > 0) {
                        BMNode current = processable.remove();

                        for (BMNode node : g.getNeighbors(current)) {
                            if (node.get(BMGraphAttributes.POS_KEY) == null) {
                                int count = 0;
                                double x = 0.0;
                                double y = 0.0;

                                for (BMNode neighbor : g.getNeighbors(node)) {
                                    String pos = neighbor.get(BMGraphAttributes.POS_KEY);

                                    if (pos == null)
                                        continue;

                                    String[] posParts = pos.split(",");
                                    double thisX = Double.parseDouble(posParts[0]);
                                    double thisY = Double.parseDouble(posParts[1]);

                                    x += thisX;
                                    y += thisY;
                                    count++;
                                }

                                x = x / count;
                                y = y / count;

                                if (!processed.contains(node))
                                    processable.add(node);
                            }


                        }

                        processed.add(current);
                    }
                }

                for (BMNode node : g.getNodes()) {
                    try {
                        nextCurrentGraph.getNode(node).setPos(new Vec2(node.get(BMGraphAttributes.POS_KEY)));
                    } catch (NullPointerException npe) {
                    }
                }
                for (VisualEdge edge : nextCurrentGraph.getAllEdges())
                    edge.assignPositionAccordingToEndpoints();

                Logging.info("pipeline", "Setting new graph.");
                this.setCurrentGraph(nextCurrentGraph);
                this.rebuildStringCaches();
            } else {
                Logging.info("pipeline", "Not setting new graph.");
            }

            if (!wasEmpty)
                this.restorePointsOfInterestAndPositions(this.getCurrentGraph(), pointsOfInterestCache);

            /**
             * Fast operations are always run.  This should clear the graph from colors and labels.
             */
            Logging.debug("pipeline", "Clear operations, then run pipeline.");
            try {
                ClearOperation clop = new ClearOperation();
                clop.doOperation(this.getCurrentGraph());
            } catch (GraphOperationException e1) {
                e1.printStackTrace();
                assert (false);
            }

            Logging.debug("pipeline", "Running fast ops: " + getFastOps());
            for (GraphOperation op : getFastOps()) {
                if (op instanceof EdgeHiderOperation)
                    ((EdgeHiderOperation) op).setCurrentGraph(this.getCurrentGraph());
                try {
                    Logging.debug("pipeline", "Running fast op " + op);
                    op.doOperation(this.getCurrentGraph());
                } catch (GraphOperationException e) {
                    e.printStackTrace();
                    Logging.error("pipeline", "Fast operations are not supposed to throw exceptions!");
                }
            }

            this.getCurrentGraph().setHighlightMode(oldHighlightMode);
            this.visualizer.getGraphArea().graphStructureChanged(nextCurrentGraph);
        } finally {
            changesRunning = false;
        }

    }

    private void restorePointsOfInterestAndPositions(VisualGraph graph, Map<String, Double> pointsOfInterestCache) {
        // Construct a map to use quickly in the next step
        HashMap<String, VisualNode> allNodes = new HashMap<String, VisualNode>();
        for (VisualNode node : graph.getAllNodes())
            allNodes.put(node.getId(), node);

        HashMap<String, VisualEdge> allEdges = new HashMap<String, VisualEdge>();
        for (VisualEdge edge : graph.getAllEdges())
            allEdges.put(edge.toString(), edge);

        // Reconstruct nodes of interest
        for (String key : pointsOfInterestCache.keySet())
            if (allNodes.containsKey(key)) {
                graph.addNodeOfInterest(allNodes.get(key), pointsOfInterestCache.get(key));
                Logging.debug("pipeline", "Restoring node of interest " + allNodes.get(key));
            }

        for (String key : this.nodePosCache.keySet())
            if (allNodes.containsKey(key))
                allNodes.get(key).setPos(this.nodePosCache.get(key));

        for (String key : this.edgePosCache.keySet())
            if (allEdges.containsKey(key))
                allEdges.get(key).setPos(this.edgePosCache.get(key));
    }


    public Map<VisualNode, Double> getNodesOfInterest() {
        return this.getCurrentGraph().getNodesOfInterest();
    }

    private void setCurrentGraph(VisualGraph newGraph) {
        assert this.visualizer != null;
        if (this.currentGraph != null)
            this.currentGraph.removeObserver(this);

        this.currentGraph = newGraph;
        this.visualizer.setGraph(this.currentGraph, false);
        this.controls.updateControls();
        this.currentGraph.addObserver(this);
    }

    public VisualNode getNodeById(String id) {
        return this.idToNode.get(id);
    }

    public VisualNode getNodeByName(String name) {
        return this.idToNode.get(name);
    }

    private void showFetchError(String errorMessage) {
        JOptionPane.showMessageDialog(vis, errorMessage, "Graph loading failed.",
                JOptionPane.WARNING_MESSAGE);
    }

    public void colorsChanged(VisualGraph g) {
    }

    public void zoomRequested(VisualGraph g, Collection<LayoutItem> items) {
    }

    public void graphStructureChanged(VisualGraph graph) {
        assert (graph == currentGraph);
        Logging.debug("pipeline", "Pipeline.graphStructureChanged()");
        this.settingsChanged(true);
    }

    public void pointsOfInterestsChanged(VisualGraph g) {
        assert (g == currentGraph);
        Logging.debug("pipeline", "Pipeline.pointsOfInterestsChanged()");

        this.settingsChanged(false);
        this.controls.updateControls();
    }

    public void selectionChanged(VisualGraph g) {
    }

    public void visibleNodesChanged(VisualGraph g) {
    }

    public void setGraphVisualizer(GraphVisualizer visualizer) {
        this.visualizer = visualizer;
        this.visualizer.setPipeline(this);
    }

    public void removeColoringOperations() {
        Collection<GraphOperation> removables = new HashSet<GraphOperation>();
        for (GraphOperation op : this.getFastOps())
            if (op instanceof NodeColoringOperation)
                removables.add(op);

        for (GraphOperation op : removables)
            this.removeFastOp(op);
    }

    public File getLoadedFile() {
        return loadedFile;
    }

    public Vis getVis() {
        return vis;
    }

    public GraphControls getControls() {
        return controls;
    }

    public Collection<GraphOperation> getFastOps() {
        return fastOps;
    }

    public VisualGraph getCurrentGraph() {
        return currentGraph;
    }

    public Set<VisualNode> getKnownNodes() {
        return this.knownNodeCache;
    }

    public Collection<GraphSource> getCurrentSources() {
        return currentSources;
    }

    public List<StructuralOperation> getCurrentStructuralOps() {
        return currentStructuralOps;
    }


    public GraphVisualizer getVisualizer() {
        return visualizer;
    }

    public LinkedHashSet<String> getSourceDatabases() {
        return sourceDatabases;
    }

    private String getExpandProgramName(VisualNode node) {
        for (GraphSource source : this.getCurrentSources()) {
            try {
                if (!source.getBMGraph().hasNode(node.getBMNode()))
                    continue;

                if (source.getNodeExpandProgram() != null)
                    return source.getNodeExpandProgram();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private URL getExpandURL(VisualNode node) {
        for (GraphSource source : this.getCurrentSources()) {
            try {
                if (!source.getBMGraph().hasNode(node.getBMNode()))
                    continue;

                if (source.getNodeExpandURL() != null)
                    return source.getNodeExpandURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isExpandable(BMNode bmNode) {
        for (GraphSource source : this.getCurrentSources()) {
            try {
                if (!source.getBMGraph().hasNode(bmNode))
                    continue;

                if (source.getNodeExpandProgram() != null)
                    return true;

                if (source.getNodeExpandURL() != null)
                    return true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public GraphSource expand(VisualNode node) throws IOException, InterruptedException, GraphOperationException {
        if (this.getExpandProgramName(node) != null)
            return StreamGraphSource.getNodeExpandProgramGraphSource(this.getExpandProgramName(node), node);
        if (this.getExpandURL(node) != null)
            return StreamGraphSource.getNodeExpandURLGraphSource(this.getExpandURL(node), node);
        return null;

/* QueryGraphSource src = new QueryGraphSource(
                                new CrawlQuery(Collections.singleton(bmn
                                        .getId())));
                        src.setNeighborhood(true);
                        pipeline.addSource(src);

                ArrayList<GraphSource> sources = new ArrayList<GraphSource>(pipeline.getCurrentSources());
            String expandProgram = null;
            for (GraphSource source : sources) {
                if (source.getNodeExpandProgram() != null)
                    expandProgram = source.getNodeExpandProgram();
            }
            if (expandProgram != null) {
                final String expandProgramName = expandProgram;
                Logging.debug("expand", "Expand program: " + expandProgram);

                menu.add(new AbstractAction("Show neighbors") {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        try {
                            pipeline.addSource(
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }*/


/* ArrayList<GraphSource> sources = new ArrayList<GraphSource>(pipeline.getCurrentSources());
   String expandProgram = null;
   for (GraphSource source : sources) {
       if (source.getNodeExpandProgram() != null)
           expandProgram = source.getNodeExpandProgram();
   }
   if (expandProgram != null) {
       final String expandProgramName = expandProgram;
       Logging.debug("expand", "Expand program: " + expandProgram);

       menu.add(new AbstractAction("Show neighbors") {
           public void actionPerformed(ActionEvent actionEvent) {
               try {
                   pipeline.addSource(StreamGraphSource.getNodeExpandProgramGraphSource(expandProgramName,
                           ((VisualNode) item).getId()));
               } catch (Exception ex) {
                   ex.printStackTrace();
               }
           }
       });
   }
} */
    }
}
