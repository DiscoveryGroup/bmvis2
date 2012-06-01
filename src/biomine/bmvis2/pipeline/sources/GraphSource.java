package biomine.bmvis2.pipeline.sources;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JComponent;

import biomine.bmgraph.BMGraph;
import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.layout.InitialLayout;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.SettingsChangeCallback;
import biomine.bmvis2.utils.StringUtils;

public abstract class GraphSource implements GraphOperation {
    public abstract BMGraph getBMGraph() throws GraphOperationException;

    public JComponent getSettingsComponent(SettingsChangeCallback v,
                                           VisualGraph graph) {
        return null;
    }

    public void doOperation(VisualGraph g) throws GraphOperationException {
        BMGraph bmg = this.getBMGraph();
        InitialLayout.solvePositions(bmg);

        g.addBMGraph(bmg);
    }

    /**
     * This method should return databases used in this source
     *
     * @return Collection of databases graph is constructed from.
     * @see BMGraph.getDatabaseArray()
     */
    public Collection<String> getSourceDatabases() {
        BMGraph r;
        try {
            r = getBMGraph();
        } catch (GraphOperationException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
        return Collections.singleton(r.getDatabaseArray()[1]);
    }

    public String getNodeExpandProgram() {
        try {
            BMGraph g = this.getBMGraph();
            if (g != null)
                return g.getNodeExpandProgram();
            else
                Logging.info("expand", "No graph for graph source.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public URL getNodeExpandURL() {
        try {
            BMGraph g = this.getBMGraph();
            if (g != null)
                return g.getNodeExpandURL();
            else
                Logging.info("expand", "No graph for graph source.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
