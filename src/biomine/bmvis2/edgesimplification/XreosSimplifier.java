package biomine.bmvis2.edgesimplification;

import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;

import java.util.*;

/**
 * A simpler simplifier.
 * <p/>
 * Naming inspired by the current national debt crisis of the great Hellenic
 * nation from which alphabet we so often borrow variable names from.  The
 * motivation behind this work is the high cost of the KappaSimplifier: it
 * doesn't work for large graphs at all.
 *
 * This algorithms removes edges starting from the highest degree node,
 * picking the lowest weight edge and removes the edge if neither of the
 * endpoints ends up having degree 0.  I.e. after the removal both nodes have
 * at least degree 1.
 *
 * It would be nice to be able to re-add some edges to make the whole thing
 * connected again.  There's a note about that.
 *
 * @author ahinkka
 */
public class XreosSimplifier extends Simplifier {

    @Override
    public List<VisualEdge> getRemovedEdges(VisualGraph g, int removeK) {
        Logging.info("enduser", "Starting simplification initial calculation.");
        long t = System.currentTimeMillis();

        List<VisualEdge> removed = new ArrayList<VisualEdge>(SimplificationUtils.removeParallelEdges(g, removeK));

        VisualNode[] nodesByDegree = new VisualNode[g.getAllNodes().size()];

        int i = 0;
        for (VisualNode node : g.getAllNodes())
            nodesByDegree[i++] = node;

        Arrays.sort(nodesByDegree, new Comparator<VisualNode>() {
            public int compare(VisualNode node, VisualNode other) {
                return other.getDegree() - node.getDegree();
            }
        });

        do {
            for (VisualNode node : nodesByDegree) {
                int degree = 0;
                for (VisualEdge edge : node.getEdges())
                    if (!removed.contains(edge))
                        degree++;

                VisualEdge worstEdge = this.worstEdge(node.getEdges());

                int otherDegree = 0;

                try {
                    for (VisualEdge edge : worstEdge.getOther(node).getEdges())
                        if (!removed.contains(edge))
                            otherDegree++;
                } catch (NullPointerException npe) {
                }

                if (degree > 1 && otherDegree > 1)
                    removed.add(this.worstEdge(node.getEdges()));
            }
        } while (removed.size() < removeK);

        // TODO: how to keep the whole graph connected?
        // Ideas:
        //  - look for best paths between a node in every separated component from the largest component
        //   - then remove the nodes from the removed collection
        //   - needs a way to efficiently recognize components

        Logging.debug("simplifier", "Xreos: " + removed.size() + " removed!");
        // for (VisualEdge edge : removed)
        //    Logging.debug("simplifier", "" + edge);

        long time = System.currentTimeMillis() - t;
        Logging.info("enduser", "Simplification calculation took " + time * 0.001 + " seconds.");
        return removed;
    }

    public static VisualEdge worstEdge(Collection<VisualEdge> edges) {
        VisualEdge ret = null;
        for (VisualEdge edge : edges)
            if (ret == null || ret.getGoodness() < edge.getGoodness())
                ret = edge;

        return ret;
    }

    @Override
    public String getTitle() {
        return "Simplify edges by χρέος simplifier";
    }
}
