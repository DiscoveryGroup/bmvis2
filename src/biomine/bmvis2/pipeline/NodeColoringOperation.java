package biomine.bmvis2.pipeline;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.color.Colorings;
import biomine.bmvis2.color.DefaultNodeColoring;
import biomine.bmvis2.color.NodeColoring;

public class NodeColoringOperation implements GraphOperation {

	private NodeColoring coloring;

	public NodeColoringOperation(NodeColoring col){
		coloring = col;
		if(coloringName.length()==0)
			coloringName = col.getClass().getName();
	}

	public void doOperation(VisualGraph g) throws GraphOperationException {
		//g.setNodeColoring(coloring);
		NodeColoring usedColoring =  coloring;
		if(coloring==null)
			usedColoring= new DefaultNodeColoring();

		for(VisualNode n:g.getAllNodes()){
			////dont override colors set by other operations.
			//if(n.getBaseColor()==null)
			n.setBaseColor(usedColoring.getFillColor(n));
		}
	}

	String coloringName="";

	public String getName() {
		return coloringName;
	}

	public String getToolTip() {
		return null;
	}

	public void setName(String name) {
		this.coloringName = name;
	}

	public JComponent getSettingsComponent(final SettingsChangeCallback v,
			VisualGraph graph) {

		return new JLabel(coloring.toString());
		//return new JLabel(coloringName);
//		JPanel ret = new JPanel();
//		ret.setLayout(new GridBagLayout());
//		GridBagConstraints c  = new GridBagConstraints();
//		c.weightx=1;
//		c.weighty=0;
//		c.fill = c.HORIZONTAL;
//		
//		ButtonGroup bg = new ButtonGroup();
//		Map<String,NodeColoring> cols = Colorings.getNodeColorings();
//		for(final Map.Entry<String,NodeColoring> ent:cols.entrySet()){
//			final String str = ent.getKey();
//			final NodeColoring color=ent.getValue();
//			final JRadioButton b = new JRadioButton();
//			if(coloring==null && color instanceof DefaultNodeColoring)
//				coloring = color;
//			if(coloring==color)
//				b.setSelected(true);
//			b.setAction(new AbstractAction(str) {
//				public void actionPerformed(ActionEvent arg0) {
//					if(b.isSelected()){
//						System.out.println("coloring = "+color);
//						coloring = color;
//						coloringName = str;
//						v.settingsChanged();
//					}
//				}
//			});
//			
//			bg.add(b);
//			ret.add(b,c);
//			c.gridy++;
//		}
//		
//		return ret;
	}


	public String getTitle() {
		return "Node colors: "+getName();
	}

	/* JSON  */
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		ret.put("color", coloringName);
		return ret;
	}
	public void fromJSON(JSONObject o) throws Exception {
		String str = o.get("color").toString();
		Map<String,NodeColoring> cm = Colorings.getNodeColorings();
		this.coloring = cm.get(str);
		this.coloringName=str;
	}

    public String getColoringSimpleUIName () {
        return this.coloring.getByName();
    }

    public NodeColoring getColoring() {
        return this.coloring;
    }
}
