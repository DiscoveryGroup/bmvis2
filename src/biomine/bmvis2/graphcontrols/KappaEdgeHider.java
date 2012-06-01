package biomine.bmvis2.graphcontrols;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JLabel;

import biomine.bmvis2.GraphCache;
import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.edgesimplification.KappaSimplifier;

public class KappaEdgeHider implements EdgeHider {

	GraphCache<ArrayList<VisualEdge>> cache = new GraphCache<ArrayList<VisualEdge>>();

	@Override
	public void hideEdges(VisualGraph graph, int target) {
		ArrayList<VisualEdge> removeOrder = cache.get(graph);
		if (removeOrder == null) {
			KappaSimplifier sim = new KappaSimplifier();

			removeOrder = new ArrayList<VisualEdge>(sim.getRemovedEdges(graph,1000000000	));
			cache.put(graph, removeOrder);
		}
		
		ArrayList<VisualEdge> rem= new ArrayList<VisualEdge>();
		int remaining = graph.getAllEdges().size();
		for(VisualEdge e:removeOrder){
			if(remaining<=target)break;
			rem.add(e);
			remaining--;
		}
		
		graph.setHiddenEdges(rem);
		
	}

	@Override
	public Component getComponent(VisualGraph vg) {
		// TODO Auto-generated method stub
		return new JLabel("");
	}
	
	
}
