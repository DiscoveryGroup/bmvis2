package biomine.bmvis2.edgesimplification;

import java.util.Collection;
import java.util.List;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;

public abstract class Simplifier {
	public abstract List<VisualEdge> getRemovedEdges(VisualGraph g,int removeK);
	
	public void simplify(VisualGraph g,int removedEdges){
		Collection<VisualEdge> es = getRemovedEdges(g, removedEdges);
		for(VisualEdge e:es)
			g.deleteEdge(e);
	}

	public abstract String getTitle();	
}
