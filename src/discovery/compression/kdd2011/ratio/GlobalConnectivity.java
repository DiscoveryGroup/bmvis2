package discovery.compression.kdd2011.ratio;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;
import biomine.bmvis2.algorithms.ProbDijkstra;

public class GlobalConnectivity {
	public static IntermediateGraph createGlobalGraph(SimpleVisualGraph sg){
		
		double[][] conns = ProbDijkstra.getProbMatrix(sg);
		IntermediateGraph ret =new ArrayIntermediateGraph(sg.n);
		for(int i=0;i<sg.n;i++)
			for(int j=i+1;j<sg.n;j++)
				ret.setConnection(i, j, conns[i][j]);
		for(int i=0;i<sg.n;i++)
			for(SimpleEdge x:sg.getEdges(i))
				ret.addNeighbors(i,x.to);
		return ret;
	}
}
