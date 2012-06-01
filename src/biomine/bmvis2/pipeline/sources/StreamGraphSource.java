package biomine.bmvis2.pipeline.sources;

import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;
import biomine.bmgraph.BMNode;
import biomine.bmvis2.BMGraphAttributes;
import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

public class StreamGraphSource extends GraphSource {
    private String title;
    private BMGraph graph;

    public StreamGraphSource(InputStream graph, String title) {
        this.title = title;
        this.graph = BMGraphUtils.readBMGraph(graph);
    }

    private static void assignPositions(BMGraph graph, VisualNode node) {
        for (BMNode bmNode : graph.getNodes())
            bmNode.getAttributes().put(BMGraphAttributes.POS_KEY, node.getBMPos());
    }

    public static StreamGraphSource getNodeExpandProgramGraphSource(final String expandProgramName, VisualNode node) throws IOException, InterruptedException, GraphOperationException {
        String nodeId = node.getId();
        String cmd = expandProgramName + " " + nodeId;
        Logging.debug("expand", "Running command: " + cmd);
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);
        pr.waitFor();

        StreamGraphSource s = new StreamGraphSource(pr.getInputStream(), "Neighborhood for " + nodeId);
        StreamGraphSource.assignPositions(s.getBMGraph(), node);
        return s;
    }

    public static StreamGraphSource getNodeExpandURLGraphSource(final URL expandURL, VisualNode node) throws IOException, InterruptedException {
        String nodeId = node.getId();
        Logging.debug("expand", "Querying URL " + expandURL);
        /* String cmd = expandProgramName + " " + nodeId;
        Logging.debug("expand", "Running command: " + cmd);
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);
        pr.waitFor();

        StreamGraphSource s = new StreamGraphSource(pr.getInputStream(), "Neighborhood for " + nodeId);

        return s; */
        return null;
    }

    @Override
    public void doOperation(VisualGraph g) throws GraphOperationException {
        Logging.debug("expand", "StreamGraphSource.doOperation()");

        BMGraph bmg = this.getBMGraph();

        Collection<VisualNode> oldNodes = g.getNodes();

        HashMap<String, String> positions = new HashMap<String, String>();
        for (VisualNode node : oldNodes)
            positions.put(node.getId(), node.getBMPos());

        g.addBMGraph(bmg);

        for (VisualNode node : g.getNodes())
            if (positions.containsKey(node.getId()))
                node.getBMNode().put(BMGraphAttributes.POS_KEY, positions.get(node.getId()));
    }


    @Override
    public BMGraph getBMGraph() throws GraphOperationException {
        return graph;
    }

    public String getTitle() {
        return title;
    }

    public String getToolTip() {
        return null;
    }

    public JSONObject toJSON() {
        return null;
    }

    public void fromJSON(JSONObject o) throws Exception {
    }

    public String toString() {
        return this.getClass().getName() + ": " + this.title + ", " + this.graph.getNodes().size() + " nodes";
    }
}
