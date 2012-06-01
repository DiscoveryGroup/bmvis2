package biomine.bmvis2.pipeline;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.simple.JSONObject;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;

public class EdgeGoodnessHider implements GraphOperation{
	
	private double limit=0;
	
	public void doOperation(VisualGraph g) throws GraphOperationException {
		HashSet<VisualEdge> hiddenEdges = new HashSet<VisualEdge>();
		hiddenEdges.addAll(g.getHiddenEdges());
		for(VisualEdge e:g.getAllEdges()){
			if(e.getGoodness()<limit){
				hiddenEdges.add(e);
			}
		}
		ArrayList<VisualNode> hiddenNodes = new ArrayList();
		for(VisualNode n:g.getNodes()){
			int edgeCount = 0;
			for(VisualEdge e:n.getEdges()){
				if(!hiddenEdges.contains(e) )
					edgeCount++;
			}
			if(edgeCount==0){
				hiddenNodes.add(n);
			}
		}
		g.hideNodes(hiddenNodes);
		g.hideEdges(hiddenEdges);
		System.out.println("edgeGoodness update!");
	}

	public JComponent getSettingsComponent(final SettingsChangeCallback v,
			VisualGraph graph) {
		double maxEdge=0;
		for(VisualEdge e:graph.getAllEdges()){
			maxEdge = Math.max(maxEdge,e.getGoodness());
		}
		
		
		final int scale = 100;
		final JSlider limitSlider =  new JSlider();
		limitSlider.setMinimum(0);
		limitSlider.setMaximum(scale);
		limitSlider.setValue((int)(limit*scale));
		final JTextField limitText=new JTextField();
		limitText.setEditable(false);
		limitText.setText(""+limit);
		
		limitSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				limit = limitSlider.getValue()/(double)scale;
				limitText.setText(""+limit);
				v.settingsChanged(false);
			}
		});
		
		
		JPanel ret = new JPanel();
		GridBagLayout bag = new GridBagLayout();
		ret.setLayout(bag);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.fill=c.HORIZONTAL;
		ret.add(limitSlider,c);
		c.gridy++;
		ret.add(limitText,c);
		return ret;
	}

	public String getTitle() {
		return "Edge filter";
	}

	public String getToolTip() {
		return "";
	}

	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		ret.put("limit",limit);
		return ret;
	}
	
	public void fromJSON(JSONObject o) throws Exception {
		limit = (Double)o.get("limit");
	}


}
