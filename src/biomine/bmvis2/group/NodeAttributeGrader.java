package biomine.bmvis2.group;

import biomine.bmgraph.BMNode;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.graphcontrols.NodeGrader;

public class NodeAttributeGrader implements NodeGrader{

	private String attribute;
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public double getDefaultG() {
		return defaultG;
	}
	public void setDefaultG(double defaultG) {
		this.defaultG = defaultG;
	}
	private double defaultG=0;
	public NodeAttributeGrader(String attr,double def){
		attribute=attr;
		defaultG=def;
	}
	@Override
	public double getNodeGoodness(VisualNode n) {
		BMNode bmn = n.getBMNode();
		if(bmn==null)return defaultG;
		String s = bmn.get(attribute);
		if(s==null)return defaultG;		try{
			return Double.parseDouble(s);
		}catch(NumberFormatException e){
			return defaultG;
		}
	}

	public static final NodeAttributeGrader BEST_PATH = new NodeAttributeGrader("goodness_of_best_path",0);

    public String getReadableAttribute() {
        if (this.attribute == "goodness_of_best_path")
            return "best path goodness";
        else
            return this.attribute + " goodness";
    }
}
