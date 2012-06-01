package biomine.bmvis2.pipeline;

import java.util.HashSet;

import javax.swing.JComponent;

import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;

public class ManualHider implements GraphOperation{

	HashSet<String> hidden  = new HashSet<String>();
	@Override
	public void doOperation(VisualGraph g) throws GraphOperationException {
		
	}

	@Override
	public void fromJSON(JSONObject o) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JComponent getSettingsComponent(SettingsChangeCallback v,
			VisualGraph graph) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
