package discovery.compression.kdd2011.ratio;

import biomine.bmvis2.algorithms.ProbDijkstra;
import biomine.bmvis2.algoutils.DefaultGraph;

public class GraphDifference { 
	
	public static double difference(DefaultGraph a,DefaultGraph b){
		assert(a.getNodeCount()==b.getNodeCount());
		double d = 0;
		for(int i=0;i<a.getNodeCount();i++){
			ProbDijkstra pdA = new ProbDijkstra(a, i);
			ProbDijkstra pdB = new ProbDijkstra(b, i);
			for(int j=i+1;j<a.getNodeCount();j++){
				double aij = pdA.getProbTo(j);
				double bij = pdB.getProbTo(j);
				d+=(aij-bij)*(aij-bij);
			}
		}
		return Math.sqrt(d);
	}

	public static double edgeDifference(DefaultGraph a,
			DefaultGraph b) {
		assert(a.getNodeCount()==b.getNodeCount());
		
		double d = 0;
		for(int i=0;i<a.getNodeCount();i++){
			for(int j=i+1;j<a.getNodeCount();j++){
				double aij = a.getEdgeWeight(i, j);
				double bij = b.getEdgeWeight(i, j);
				d+=(aij-bij)*(aij-bij);
			}
		}
		return Math.sqrt(d);
	}
}
