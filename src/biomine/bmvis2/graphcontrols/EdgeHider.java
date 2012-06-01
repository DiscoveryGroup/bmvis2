package biomine.bmvis2.graphcontrols;

import java.awt.Component;

import biomine.bmvis2.VisualGraph;

public interface EdgeHider {
	public void hideEdges(VisualGraph graph,int target);
	
	/**
	 * Optional configuration component associated with hider
	 * @return
	 */

	public Component getComponent(VisualGraph vg);

}
