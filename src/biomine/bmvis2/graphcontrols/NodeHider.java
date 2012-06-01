package biomine.bmvis2.graphcontrols;

import java.awt.Component;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.pipeline.Pipeline;

public interface NodeHider {
	
	/**
	 * 
	 * @param vg Graph to modify
	 * @param target number of nodes
	 */
	public void hideNodes(VisualGraph vg,int target);
	
	/**
	 * Optional configuration component associated with hider
	 * @return
	 */

	public Component getComponent(VisualGraph graph);
}
