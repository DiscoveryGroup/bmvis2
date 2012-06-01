package biomine.bmvis2.graphcontrols;

import biomine.bmvis2.VisualNode;


public interface NodeGrader {
	public double getNodeGoodness(VisualNode n);

    public String getReadableAttribute();
}
