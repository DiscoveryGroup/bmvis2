package biomine.bmvis2.subgraph;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import biomine.bmvis2.pipeline.StructuralOperation;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a dark business interlude. An attempt to reproduce leronen's
 * famously infamous Crawler.
 * <p/>
 * Given a graph and knowledge of whether certain nodes are of interest
 * (scored between [-1,1] where -1 means the presence of a given node is not
 * wished for and 1 meaning the node should most probably be in the
 * visualization, this class should provide some kind of end result.
 * <p/>
 * Non-desired nodes should be hidden from the result.  The input graph
 * should be as complete as possible: it should be expanded around the
 * interesting points before this class is run to contain as much of the
 * structure of the graph.
 */
public abstract class Extractor extends StructuralOperation implements GraphOperation {
    private double score = 0.0;
    private int nodeBudget = 30;
    private int edgeBudget = 110;
    private Map<VisualNode, Double> interestMap;

    public String getTitle() {
        return "Visible subgraph extractor";
    }

    public String getToolTip() {
        return "Score: " + this.score;
    }

    protected void setScore(double newScore) {
        this.score = newScore;
    }

    public void setNodeBudget(int newBudget) {
        this.nodeBudget = newBudget;
    }

    public int getNodeBudget() {
        return this.nodeBudget;
    }

    public int getEdgeBudget() {
        return edgeBudget;
    }

    public void setEdgeBudget(int edgeBudget) {
        this.edgeBudget = edgeBudget;
    }

    public void setInterestMap(Map<VisualNode, Double> newMap) {
        this.interestMap = newMap;
    }

    public Map<VisualNode, Double> getInterestMap() {
        return this.interestMap;
    }

    public static void hideAllExceptNodes(VisualGraph g, Set<VisualNode> keepers) {
        Set<VisualNode> parents = new HashSet<VisualNode>();

        for (VisualNode node : keepers) {
            VisualNode parent = node.getParent();
            while (parent != null) {
                parents.add(parent);
                parent = parent.getParent();
            }
        }

        Set<VisualNode> newHidden = new HashSet<VisualNode>();
        for (VisualNode node : g.getAllNodes())
            if (!keepers.contains(node) && !parents.contains(node))
                newHidden.add(node);

        g.setHiddenNodes(newHidden);
    }

    public static void removeAllExceptNodes(VisualGraph g, Set<VisualNode> keepers) {
        Set<VisualNode> parents = new HashSet<VisualNode>();

        for (VisualNode node : keepers) {
            VisualNode parent = node.getParent();
            while (parent != null) {
                parents.add(parent);
                parent = parent.getParent();
            }
        }

        Set<VisualNode> newHidden = new HashSet<VisualNode>();
        for (VisualNode node : g.getAllNodes())
            if (!keepers.contains(node) && !parents.contains(node))
                newHidden.add(node);

        for (VisualNode node : newHidden) {
            g.deleteNode(node);
        }
    }

    public static void removeAllExceptEdges(VisualGraph g, Set<VisualEdge> keepers) {
        Set<VisualEdge> newHidden = new HashSet<VisualEdge>();
        for (VisualEdge edge: g.getAllEdges())
            if (!keepers.contains(edge))
                newHidden.add(edge);

        for (VisualEdge edge : newHidden)
            g.deleteEdge(edge);
    }

    public void fromJSON(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        this.nodeBudget = ((Integer) o.get("nodeBudget")).intValue();
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("nodeBudget", this.nodeBudget);
        return ret;
    }

    public abstract JComponent getSettingsComponent(SettingsChangeCallback v, VisualGraph graph);

    public abstract void doOperation(VisualGraph g) throws GraphOperationException;

}
