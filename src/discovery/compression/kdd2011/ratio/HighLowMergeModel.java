//package discovery.compression.compression.ratio;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//
//import biomine.bmvis2.algoutils.DefaultGraph;
//import discovery.compression.compression.ratio.RatioCompression.ResultGraph;
//
//public class HighLowMergeModel implements CompressionMergeModel{
//	private IntermediateGraph graph;
//	private IntermediateGraph hiGraph;
//	private IntermediateGraph lowGraph;
//	
//	private ArrayList<ArrayList<Integer>> groups;
//	private boolean alive[];
//
//	public final boolean isAlive(int supernode){
//		return alive[supernode];
//	}
//	private int startCost;
//	
//	public int getStartCost() {
//		return startCost;
//	}
//	
//	public final void init(IntermediateGraph ig){
//		graph=ig;
//		
//		hiGraph = ig.copy();
//		lowGraph =ig.copy();
//		alive = new boolean[ig.size()];
//		Arrays.fill(alive, true);
//		groups = new ArrayList<ArrayList<Integer>>();
//		
//		for (int i = 0; i < ig.size(); i++) {
//			ArrayList<Integer> ai = new ArrayList<Integer>();
//			ai.add(i);
//			groups.add(ai);
//			hiGraph.setConnection (i,i,-1);
//			lowGraph.setConnection(i,i,-1);
//		}
//
//		int totalCost = 0;//ig.size();
//		
//		for(int i=0;i<ig.size();i++)
//			totalCost+=ig.getNeighbors(i).size();
//		totalCost/=2;
//		//totalCost+=ig.size();
//		startCost=totalCost;
//	}
//	
//	public final double mergeError(int a, int b) {
//		
//		ArrayList<Integer> ns = new ArrayList<Integer>();
//		
//		ns.addAll(graph.getNeighbors(a));
//		ns.addAll(graph.getNeighbors(b));
//		
//		double max = 0;//maxDiff;2 * super.getDeviation() / (1 << thld);
//		
//		boolean canSucceed = false;
//		for (int i : ns) {
//			if(hiGraph.getConnection(a,i)<0||hiGraph.getConnection(b,i)<0)
//				continue;
//			
//			double hi = Math.max(hiGraph.getConnection(a,i), hiGraph.getConnection(b,i));
//			
//			double lo = Math.min(lowGraph.getConnection(a,i),lowGraph.getConnection(b,i));
//			
//			if (hi - lo > max)
//				max=hi-lo;
//		}
//		return max;
//	}
//	
//	/**
//	 * returns possible reduction in cost resulting from given merge. 
//	 * Must be called before merging.
//	 * @param to
//	 * @param from
//	 * @return
//	 */
//	public final int mergeCostReduction(int to,int from){
//		int ret=0;
//		for(int x:hiGraph.getNeighbors(to)){
//			if(hiGraph.getNeighbors(from).contains(x))
//				ret++;
//		}
//		if(hiGraph.getNeighbors(to).contains(from)==false){
//			ret--;
//		}
//		return ret;
//	}	
//	
//	public final void merge(int to,int from){
//		
//		groups.get(to).addAll(groups.get(from));
//		
//		groups.get(from).clear();
//		
//		//add froms neighbors to "to"
//		ArrayList<Integer> fromNS = new ArrayList<Integer>(hiGraph.getNeighbors(from));
//		for(int x:fromNS){
//			hiGraph.addNeighbors(to,x);
//			lowGraph.addNeighbors(to,x);
//			
//		}
//		
//		//remove "from" from neighbors
//		
//		for(int x:fromNS){
//			hiGraph.removeNeighbors(x,from);
//			lowGraph.removeNeighbors(x,from);
//		}
//		
//		//always add self-edge
//		hiGraph.addNeighbors(to,to);
//		lowGraph.addNeighbors(to,to);
//		
//		// neighbors.get(to).removeAll(groups.get(to));
//
//		alive[from] = false;
//
//		Collection<Integer> connSet = new ArrayList<Integer>(hiGraph.getConnections(to));
//		for(int i:hiGraph.getConnections(to)){
//			
////			double hiConn = hiGraph.getConnection(to,i);
////			double loConn = hiGraph.getConnection(to,i);
////			
////			if(hiConn<0||loConn<0){
////				double s = hiGraph.getConnection(from, i);
////				hiGraph.setConnection(to,i,s);
////				s = lowGraph.getConnection(from,i);
////				lowGraph.setConnection(to,i,s);
////				System.out.println("selfedge merge "+s);
////				
////			}else 
//			
//			{
//				
//				double hiToI   = hiGraph.getConnection(to, i);
//				double loToI   = lowGraph.getConnection(to, i);
//				double hiFromI = hiGraph.getConnection(from, i);
//				double loFromI = lowGraph.getConnection(from, i);
//				
//				//hiToI = Math.max();
//				loToI = Math.max(loToI,loFromI*lowGraph.getConnection(to,from));
//				
//				if(loToI<0)
//					loToI=loFromI;
//				if(loFromI<0)
//					loFromI=loToI;
//				
//				hiGraph. setConnection (to,i,Math.max(hiToI, hiFromI));
//				lowGraph.setConnection (to,i,Math.min(loToI, loFromI));
//			
//			}
//			
//		}
//	}
//	public final ResultGraph getResult(){
//		System.out.println("getResult called");
//		ResultGraph r = new ResultGraph();
//		
//		r.graph = new DefaultGraph();
//		int c=0;
//		r.partition = groups;
//		int finalcost=0;
//		for(int i=0;i<graph.size();i++){
//			if(isAlive(i)){
//				c++;
//				finalcost++;
//				for(int j:hiGraph.getNeighbors(i)){
//					if(j<i)continue;
//					if(!alive[j])continue;
//					double sum=0;
//					int count=0;
//					for(int xi:groups.get(i)){
//						for(int xj:groups.get(j)){
//							if(xi==xj)continue;
//							sum+=graph.getConnection(xi, xj);
//							count++;
//						}
//					}
//					r.graph.addEdge(i,j,sum/count);
//					finalcost++;
//				}
//			}
//		}
//		
//		System.out.println(c+" alive nodes");
//		
//		return r;
//	}
//	public Collection<Integer> getNeighbors(int x){
//		return hiGraph.getNeighbors(x);
//	}
//	
//	public Collection<Integer> getHopNeighbors(int x,int n){
//		return hiGraph.getHopNeighbors(x,n);
//	}
//
//}
