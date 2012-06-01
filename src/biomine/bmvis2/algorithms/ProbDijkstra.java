package biomine.bmvis2.algorithms;

import java.util.Arrays;
import java.util.PriorityQueue;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.algoutils.DefaultGraph;

class PQNode implements Comparable<PQNode> {
	int node;
	double dist;
	
	public int compareTo(PQNode a) {
		return Double.compare(dist, a.dist);
	}
}

public final class ProbDijkstra {

	private VisualGraph visualGraph;
	private VisualNode start;
	
	private DefaultGraph graph;
	private SimpleVisualGraph sg;
	
	//private HashMap<Integer, Double> distMap = new HashMap<Integer, Double>();
	private double[] distMap;

	private PriorityQueue<PQNode> pq = new PriorityQueue<PQNode>();
	
	public ProbDijkstra(VisualGraph g, VisualNode start) {
		visualGraph = g;
		this.start = start;
		SimpleVisualGraph sg = new SimpleVisualGraph(g.getRootNode());
		graph = new DefaultGraph(sg);
		
		this.sg=sg;
		PQNode sn = new PQNode();
		sn.dist = 0;
		sn.node = sg.getInt(start);
		pq.add(sn);
		distMap = new double[graph.getNodeCount()];
		Arrays.fill(distMap, -1);
		
	}
	
	public ProbDijkstra(SimpleVisualGraph sg, int start) {
		visualGraph = sg.getVisualNode(0).getGraph();
		
		this.start = sg.getVisualNode(start);
		this.sg=sg;
		graph = new DefaultGraph(sg);
		
		PQNode sn = new PQNode();
		sn.dist = 0;
		sn.node = start;
		pq.add(sn);
		distMap = new double[graph.getNodeCount()];
		Arrays.fill(distMap, -1);
	}
	public ProbDijkstra(DefaultGraph dg,int start){
		this.visualGraph=null;
		this.sg=null;
		this.graph=dg;
		PQNode sn= new PQNode();
		sn.dist=0;
		sn.node=start;
		pq.add(sn);
		distMap = new double[dg.getNodeCount()];
		Arrays.fill(distMap,-1);
	}
	private boolean visited(int n) {
		if(n>=distMap.length){
			System.out.println("array error: size="+distMap.length+" index="+n);
			return true;
		}
		return distMap[n] >=0;
	}

	double probToLen(double prob) {
		return -Math.log(prob);
	}

	double lenToProb(double len) {
		if (len < 0)
			return 0;
		return Math.exp(-len);
	}

	public double getProbTo(VisualNode to) {
		int target = sg.getInt(to);
		return getProbTo(target);
		
	}

	public double getProbTo(int target){
		
		if(distMap.length<=target)return 0;
		double memret = distMap[target];//distMap.get(target);

		if(memret>=0)
			return lenToProb(memret);

		
		while (pq.isEmpty() == false) {
			PQNode p = pq.peek();
			int node = p.node;
			double dist = p.dist;
		
			
			pq.poll();
			if (visited(node))
				continue;
			distMap[node]=dist;
			//this.distMap.put(node, dist);
		
			// inc(graph.visualNodes[node], dist * weight);
			assert(pq.size()<=sg.n*sg.n);
			for (int e : graph.getNeighbors(node)){
				
				if (visited(e))
					continue;
				double el = probToLen(graph.getEdgeWeight(node,e));
				if(el<0)el=10;
				PQNode add = new PQNode();
				add.dist = dist + el;
				add.node = e;
				pq.add(add);
			}
			if(node==target)
				return lenToProb(dist);
			
			
		}
//		assert(false);
		return 0.0;
	}
	public static double[][] getProbMatrix(SimpleVisualGraph sg){
		double[][] ret = new double[sg.n][sg.n];
		for(int i=0;i<sg.n;i++){
			ProbDijkstra pd=new ProbDijkstra(sg, i);
			ret[i][i]=1;
			for(int j=i+1;j<sg.n;j++){
				double p = pd.getProbTo(j);
				ret[i][j]=p;
				ret[j][i]=p;
			}
		}
		return ret;
	}
	public static double[][] getProbMatrix(DefaultGraph dg){
		double[][] ret = new double[dg.getNodeCount()][dg.getNodeCount()];
		for(int i=0;i<dg.getNodeCount();i++){
			ProbDijkstra pd=new ProbDijkstra(dg, i);
			ret[i][i]=1;
			for(int j=i+1;j<dg.getNodeCount();j++){
				double p = pd.getProbTo(j);
				ret[i][j]=p;
				ret[j][i]=p;
			}
		}
		return ret;
	}
}
