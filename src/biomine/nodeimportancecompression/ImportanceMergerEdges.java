package biomine.nodeimportancecompression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ImportanceMergerEdges extends ImportanceMerger {

	private boolean keepEdges=false;

	public boolean keepEdges() {
		return keepEdges;
	}

	public void setKeepEdges(boolean keepEdges) {
		this.keepEdges = keepEdges;
	}

	public ImportanceMergerEdges(ImportanceGraph graph){
		super(graph);
	}
	
	
	public double getConnection(int u,int v){
		if(getCurrentGraph().hasEdge(u, v)==false)return 0;
		return getCurrentGraph().getEdgeWeight(u, v);
	}
	
	final private double sqr(double x){
		return x*x;
	}
	private Integer mergedGroupGet(int u,int v,int i){
		if(i<getGroups().get(u).size())
			return getGroups().get(u).get(i);
		else
			return getGroups().get(v).get(i-getGroups().get(u).size());
	}
	
	
	private double selfErr(int u,int v,double w){
		double sum = 0;
		double imps = 0;
		double d_uv = sqr(w-getConnection(u, v));
		double d_vv = sqr(w-getConnection(v, v));
		double d_uu = sqr(w-getConnection(u, u));
		sum+=pairwiseImpSum(u)*d_uu;
		sum+=impSum(u)*impSum(v)*d_uv;
		sum+=pairwiseImpSum(v)*d_vv;
		return sum;
	}
	
	private double err(int u,int v,int x,double w){
		assert u != v;
		double sum = 0 ;
		double d_ux = sqr(w-getConnection(u,x));
		double d_vx = sqr(w-getConnection(v,x));
		
		sum+=d_ux*impSum(u)*impSum(x);
		sum+=d_vx*impSum(v)*impSum(x);
		
		return sum;
	}
	
	/**
	 * @param u first node of merge
	 * @param v second node of merge
	 * @param x node with edges to u and/or v
	 * @param i 
	 * @return whether edge from supernode uv to x should be removed.
	 */
	private boolean isEdgeRemoved(int u,int v,int x, int sizeRed,double err){
		double limit = err/sizeRed;
		int sizered = 1;
		if(getNeighbors(x).size()==1)
			sizered++;
		double e2 = err(u,v,x,0);
		assert(e2>=err);
		double d = (e2-err)/sizered;
		return d<limit;
	}
	public double edgeRemovalDeltaError(int u,int v,int x,double totalError,double err){
		return Math.sqrt(totalError*totalError+err(u,v,x,0)-err)-totalError;
	}
	public double selfEdgeRemovalDeltaError(int u,int v,double totalError,double err){
		return Math.sqrt(totalError*totalError+selfErr(u, v, 0)-err)-totalError;
	}
	private boolean isSelfEdgeRemoved(int u, int v, int sizeRed,double err) {
		double limit = err/sizeRed;
		double e2 = selfErr(u, v, 0);
		if(e2<err){
			System.out.println("isSelfEdgeRemoved "+u+" "+v+" e="+err+" e'="+e2);
			System.out.println("w = "+selfNewWeight(u,v));
			assert(e2>=err);
		}
		double d = e2-err;
		return d<limit;
	}
	
	private double max3(double a,double b,double c){
		return Math.max(Math.max(a,b),c);
	}
	private double min3(double a,double b,double c){
		return Math.min(Math.min(a,b),c);
	}
	
	/**
	 * Returns 
	 * @param u
	 * @param v
	 * @return
	 */
	public double edgeDeleteError(int u,int v){
		if(u==v){
			return sqr(getConnection(u, v))*pairwiseImpSum(u);
		}else {
			return impSum(u)*impSum(v)*sqr(getConnection(u, v)-0);
		}
	}

	
	
        
	
	public MergeInfo getMergeInformation(int u,int v){
		
		HashSet<Integer> un = new HashSet<Integer>(getNeighbors(u));
		HashSet<Integer> vn  =new HashSet<Integer>(getNeighbors(v));
		
		HashSet<Integer> xs = new HashSet<Integer>(un);
		xs.addAll(vn);
		xs.remove(u);
		xs.remove(v);
		//xs.remove(v);
		//xs.remove(u);
		
		MergeInfo info = new MergeInfo();
		info.v = v;
		info.u = u;
		
		/*
		 * Compute size reduction:
		 * 
		 * Edges which are in both will be "removed"
		 */
		int edgesRemoved = 0;
		int nodesRemoved = 1;
		for(int x:xs){
			if(x==u || x==v)continue;
			int count = 0;
			if(un.contains(x)) count++;
			if(vn.contains(x))count++;
			if(count==2)edgesRemoved++;
		}
		int selfEdgesCount = 0;
		if(getNeighbors(u).contains(u))
			selfEdgesCount++;
		if(getNeighbors(v).contains(v))
			selfEdgesCount++;
		if(getNeighbors(u).contains(v))
			selfEdgesCount++;
		
		if(selfEdgesCount!=0)//if there are no edges to merge into self edge, no such will be added
			edgesRemoved += selfEdgesCount-1;//otherwise possibly multiple edges are replaced by selfedge
		
		info.hasSelfEdge = selfEdgesCount>0;
		//info.firstSizeReduction = nodesRemoved+edgesRemoved;
		info.sizeReduction = nodesRemoved+edgesRemoved;
		
		ArrayList<Integer> arrXs = new ArrayList<Integer>(xs);
		double[] error = new double[arrXs.size()];
		/*
		 * compute error
		 */
		double errorSum = 0;
		
		info.newWeights = new HashMap<Integer,Double>();
		
		//info.errors =  new HashMap<Integer,Double>();
		for(int i=0;i<arrXs.size();i++){
			int x = arrXs.get(i);
			double w = newWeight(u,v,x);
			info.newWeights.put(x,w);
			error[i]=err(u,v,x,w);
			errorSum+=error[i];
			//info.errors.put(x,error[i]);
		}
		
		//run checking for possible removals of edges
		class SortPair{ 
			double error;
			double sizeRed;
			int x;
		};	
		if(info.hasSelfEdge){
			double selfWeight = selfNewWeight(u,v);
			info.selfNewWeight = selfWeight;
			double selfError = selfErr(u,v,selfWeight);
			errorSum+=selfError;
			info.selfError = selfError;
		}

		info.error = Math.sqrt(errorSum);
		
		info.removedEdges = new HashSet<Integer>();
		if(!keepEdges){
			SortPair[] costs = new SortPair[arrXs.size()+(info.hasSelfEdge?1:0)];
			for(int i=0;i<arrXs.size();i++){
				SortPair p = new SortPair();
				p.x = arrXs.get(i);
				p.error = edgeRemovalDeltaError(u,v,p.x,info.error,error[i]);
				p.sizeRed = 1;
				
				if(getNeighbors(p.x).size()==1){
					p.sizeRed++;
				}else if(getNeighbors(p.x).size()==2){
					if(getNeighbors(p.x).contains(u)&&
						getNeighbors(p.x).contains(v)){
						p.sizeRed++;	
					}
				}
				costs[i] = p;
			}
			//check if self-
			if(info.hasSelfEdge){
				SortPair p = new SortPair();;
				p.x = v;
				p.error = selfEdgeRemovalDeltaError(u,v,info.error,info.selfError);
				p.sizeRed=1;
				costs[costs.length-1]=p;
			}
			Arrays.sort(costs,new Comparator<SortPair>(){
				@Override
				public int compare(SortPair a, SortPair b) {
					return Double.compare(a.error/a.sizeRed,b.error/b.sizeRed);
				}
				
			});
			
			for(int i=0;i<costs.length;i++){
				if(costs[i].error/costs[i].sizeRed < info.error/info.sizeReduction){
					int x = costs[i].x;
					info.error+=costs[i].error;
					info.sizeReduction+=costs[i].sizeRed;
					info.removedEdges.add(x);
				}else break;
			}
		}
			
		/*
		 * 
		 * Old version
		 * maybe faster but has bigger error
		info.removedEdges = new HashSet<Integer>();
		if(info.hasSelfEdge){
			double selfWeight = selfNewWeight(u,v);
			info.selfNewWeight = selfWeight;
			double selfError = selfErr(u,v,selfWeight);
			errorSum+=selfError;
			info.selfError = selfError;
			//check if self-edge will be removed
			if(isSelfEdgeRemoved(u,v,info.sizeReduction,selfError)){
				edgesRemoved++;
				info.removedEdges.add(v);
			}
		}
		
		for(int i=0;i<arrXs.size();i++){
			int x = arrXs.get(i);
			if(isEdgeRemoved(u,v,x,info.sizeReduction,error[i])){
				edgesRemoved++;
				info.removedEdges.add(x);
				
			 // Check if we also need to remove the node x 
				if(currentGraph.getNeighbors(x).size()==1)
					nodesRemoved++;
			}
		
		
		//update with real sizeReduction
		info.sizeReduction = nodesRemoved+edgesRemoved;
		info.nr = nodesRemoved;
		info.er = edgesRemoved;
		}*/
		
		return info;
	}




}
