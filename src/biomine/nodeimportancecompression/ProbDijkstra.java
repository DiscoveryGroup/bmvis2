package biomine.nodeimportancecompression;


import java.util.Arrays;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

class PQNode implements Comparable<PQNode> {
	int node;
	double dist;
	
	public int compareTo(PQNode a) {
		return Double.compare(dist, a.dist);
	}
}

public final class ProbDijkstra {

	
	private ImportanceGraph graph;
	
	//private HashMap<Integer, Double> distMap = new HashMap<Integer, Double>();
	private double[] distMap;

	private PriorityQueue<PQNode> pq = new PriorityQueue<PQNode>();
	
	
	public ProbDijkstra(ImportanceGraph dg,Map<Integer,Double> start,Set<Integer> remove){
		this.graph = dg;
		for(int x:start.keySet()){
			PQNode sn= new PQNode();
			sn.dist=probToLen(start.get(x));
			sn.node=x;
			pq.add(sn);
		}
		distMap = new double[dg.getMaxNodeId()+1];
		Arrays.fill(distMap,-1);
		for(int x:remove){
			distMap[x] = Double.MAX_VALUE;
		}
	}
	public ProbDijkstra(ImportanceGraph dg,int start){
		this.graph=dg;
		PQNode sn= new PQNode();
		sn.dist=0;
		sn.node=start;
		pq.add(sn);
		distMap = new double[dg.getMaxNodeId()+1];
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
			for (int e : graph.getNeighbors(node)){
				
				if (visited(e))
					continue;
				double el = probToLen(graph.getEdgeWeight(node,e));
				if(el<0)el=100;
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
	public static double[][] getProbMatrix(ImportanceGraph dg){
		double[][] ret = new double[dg.getMaxNodeId()+1][dg.getMaxNodeId()+1];
		for(int i=0;i<dg.getMaxNodeId();i++){
			ProbDijkstra pd=new ProbDijkstra(dg, i);
			ret[i][i]=1;
			for(int j=i+1;j<dg.getMaxNodeId();j++){
				double p = pd.getProbTo(j);
				ret[i][j]=p;
				ret[j][i]=p;
			}
		}
		return ret;
	}
}
