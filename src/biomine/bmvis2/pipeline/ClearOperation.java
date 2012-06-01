package biomine.bmvis2.pipeline;

import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JLabel;

import biomine.bmvis2.Logging;
import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.color.DefaultNodeColoring;

public class ClearOperation implements GraphOperation{
	public void doOperation(VisualGraph g) throws GraphOperationException {
		DefaultNodeColoring col = new DefaultNodeColoring();
		Logging.debug("graph_operation", "ClearOperation.doOperation()");
		for(VisualNode n:g.getAllNodes()){
			n.setExtraLabels(Collections.EMPTY_LIST);
			n.setBaseColor(col.getFillColor(n));
		}
		g.setHiddenEdges(Collections.EMPTY_LIST);
		g.setHiddenNodes(Collections.EMPTY_LIST);
	}

	public JComponent getSettingsComponent(SettingsChangeCallback v,
			VisualGraph graph) {
		return new JLabel("This should not be visible");
	}

	public String getTitle() {
		return "clear op: should be hidden";
	}
		
	public String getToolTip() {
		
		return null;
	}

	public void fromJSON(JSONObject o) throws Exception {
	}

	public JSONObject toJSON() {
		return new JSONObject();
	}

}
