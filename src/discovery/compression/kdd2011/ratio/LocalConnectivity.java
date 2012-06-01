package discovery.compression.kdd2011.ratio;

import java.util.ArrayList;
import java.util.HashMap;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;

public class LocalConnectivity {

	static public IntermediateGraph createLocalGraph(SimpleVisualGraph sg,
			int pathlen) {
		if(pathlen==-1)return GlobalConnectivity.createGlobalGraph(sg);
		
		IntermediateGraph ret = new MapIntermediateGraph(sg.n);
		for (int i = 0; i < sg.n; i++)
			for (SimpleEdge x : sg.getEdges(i)) {
				ret.addNeighbors(i, x.to);
				ret.setConnection(i, x.to, x.weight);
			}

		for (int i = 0; i < sg.n; i++) {
			HashMap<Integer, Double> dist = new HashMap<Integer, Double>();
			ArrayList<Integer> q = new ArrayList<Integer>();
			q.add(i);
			dist.put(i,1.0);
			for (int z = 0; z < pathlen; z++) {
				ArrayList<Integer> nq = new ArrayList<Integer>();
				for (int node : q) {
					double d = dist.get(node);
					for (SimpleEdge se : sg.getEdges(node)) {
						Double dd = dist.get(se.to);
						if (dd==null || dd<d*se.weight) {
							nq.add(se.to);
							dist.put(se.to, d * se.weight);
							ret.setConnection(i, se.to, d * se.weight);
						}
					}
				}
				q = nq;
			}
		}
		return ret;
	}

}
