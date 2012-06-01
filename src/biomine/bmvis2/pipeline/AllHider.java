package biomine.bmvis2.pipeline;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import org.json.simple.JSONObject;

import javax.swing.*;

public class AllHider implements GraphOperation {
    public String getTitle() {
        return "Hide everything";
    }

    public String getToolTip() {
        return null;
    }

    public JComponent getSettingsComponent(SettingsChangeCallback v, VisualGraph graph) {
        return null;
    }

    public void doOperation(VisualGraph g) throws GraphOperationException {
        g.hideNodes(g.getAllNodes());
        // g.unHideNode(g.getRootNode());
        // g.getRootNode().setOpen(true);
    }

    public JSONObject toJSON() {
        return null;
    }

    public void fromJSON(JSONObject o) throws Exception {
    }
}
