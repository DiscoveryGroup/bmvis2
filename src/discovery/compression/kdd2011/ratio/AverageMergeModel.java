package discovery.compression.kdd2011.ratio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import biomine.bmvis2.algoutils.DefaultGraph;
import discovery.compression.kdd2011.ratio.RatioCompression.ResultGraph;

public abstract class AverageMergeModel implements CompressionMergeModel {

	private ArrayList<ArrayList<Integer>> groups;
	public ArrayList<ArrayList<Integer>> getGroups() {
		return groups;
	}

	private boolean alive[];

	public void initCopy(AverageMergeModel mm){
		mm.alive = Arrays.copyOf(alive, alive.length);
		mm.groups = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<groups.size();i++){
			mm.groups.add(new ArrayList<Integer>(groups.get(i)));
		}
	}
	public abstract double getConnection(int x,int y);
	public abstract void setConnection(int x,int y,double d);
	
	public abstract AverageMergeModel copy();
	
	public final boolean isAlive(int supernode) {
		return alive[supernode];
	}

	public final double mergeError(int a, int b) {

		ArrayList<Integer> ns = new ArrayList<Integer>();

		ns.addAll(getNeighbors(a));
		ns.addAll(getNeighbors(b));

		double max = 0;// maxDiff;2 * super.getDeviation() / (1 << thld);

		boolean canSucceed = false;
		double ab = getConnection(a, b);

		for (int i : ns) {
			//if (getConnection(a, i) < 0
					//|| getConnection(b, i) < 0)
				//continue;
			double da=0;
			if(i==a||i==b){
				double ea = getConnection(a,a);
				double eb = getConnection(b,b);
				double ec = getConnection(a,b);
				if(groups.get(a).size()==1){
					ea = ec;
				}
				if(groups.get(b).size()==1){
					eb = ec;
				}
				da = Math.max(ea,Math.max(eb,ec))-Math.min(ea,Math.min(eb,ec));
			}else{
				double ai = getConnection(a, i);
				double bi = getConnection(b, i);
				//ai = Math.max(ab * bi, ai);
				//bi = Math.max(ab * ai, bi);
				da = Math.abs(bi - ai);
			}

			max = Math.max(da, max);
		}
		return max;
	}

	/**
	 * returns possible reduction in cost resulting from given merge. Must be
	 * called before merging.
	 * 
	 * @param to
	 * @param from
	 * @return
	 */
	public final int mergeCostReduction(int to, int from) {
		int ret = 0;

		int old = getNeighbors(from).size()
				+ getNeighbors(to).size();

		// not counting to->from edge twice
		if (getNeighbors(from).contains(to))
			old--;

		int ne = 0;
		HashSet<Integer> hs = new HashSet<Integer>();

		for (int x : getNeighbors(from)) {
			if (x != to && x != from)
				ne++;
			hs.add(x);
		}
		// not counting self edges
		
		hs.add(to);
		hs.add(from);
		
		for (int x : getNeighbors(to)) {
			// only count neighbors if they not been counted previously
			if (hs.contains(x) == false)
				ne++;
		}

		if(getConnection(to,from)>0||getConnection(to,to)>0||getConnection(from,from)>0)
			ne += 1;// self-edge

		return old - ne;
	}

	public void merge(int to, int from) {

		int toSize = groups.get(to).size();
		int fromSize = groups.get(from).size();
		
		double toFromConn = getConnection(to,from);
		double fromFromConn = getConnection(from,from);
		double toToConn = getConnection(to,to);
		groups.get(to).addAll(groups.get(from));

		groups.get(from).clear();
		// add froms neighbors to "to"
		ArrayList<Integer> fromNS = new ArrayList<Integer>(
				getNeighbors(from));
		for (int x : fromNS) {
			addNeighbors(to, x);
		}

		// remove "from" from neighbors


		// always add self-edge
		if(toFromConn>0||fromFromConn>0)
			 addNeighbors(to, to);

		// neighbors.get(to).removeAll(groups.get(to));

		alive[from] = false;

		Collection<Integer> connSet = new ArrayList<Integer>(getConnections(to));
		double[] newVals = new double[connSet.size()];
		int connI=0;
		
		for (int i : getConnections(to)) {
			double fromI = getConnection(from, i);
			double toI = getConnection(to, i);
			
			double newVal = (fromI*fromSize + toSize*toI) / ((double)toSize+fromSize);
			
			if(i==from)
				newVal = 1000;
			
			if(i==to){
				//System.out.println("i=to "+newVal+" i="+i);
				newVal = fromI;
				double sum=0;
				
				sum+=toSize*(toSize-1)*toToConn/2;
				sum+=fromSize*(fromSize-1)*fromFromConn/2;
				sum+=toSize*fromSize*toFromConn;
				
				int ns = toSize+fromSize;
				newVal = sum/((ns*(ns-1))/2);
			}
			
			newVals[connI]=newVal;
			connI++;
		}
		
		connI=0;
		for(int i:getConnections(to))
			setConnection(to, i, newVals[connI++]);
		
		for (int x : fromNS) {
			removeNeighbors(x, from);
		}
		removeNeighbors(to,from);
	}

	public final ResultGraph getResult() {
		ResultGraph r = new ResultGraph();

		r.graph = new DefaultGraph();
		int c = 0;
		r.partition = groups;
		int finalcost = 0;
		for (int i = 0; i < graphSize(); i++) {
			if (isAlive(i)){
				c++;
				finalcost++;
				
				for (int j : this.getNeighbors(i)) {
					
					if (j < i)
						continue;
					if (!alive[j])
						continue;

					double sum = 0;
					int count = 0;

					// for(int xi:groups.get(i)){
					// for(int xj:groups.get(j)){
					// if(xi==xj)continue;
					// sum+=graph.getConnection(xi, xj);
					// count++;
					// }
					// }
					//					
					// r.graph.addEdge(i,j,sum/count);
					double ev = this.getConnection(i, j);
					
					if (ev < 0) {
						System.out.println("negative edgew " + ev + " i=" + i
								+ " j=" + j);
					}
					
					r.graph.addEdge(i, j, ev);

					finalcost++;
				}
			}
		}

		//System.out.println(c + " alive nodes");

		return r;
	}

	public abstract Collection<Integer> getNeighbors(int x);

	private void gatherHopNeighbors(HashSet<Integer> ns,int hops,int node){
		if(ns.contains(node))return;
		ns.add(node);
		if(hops==0)return;
		for(int x:getNeighbors(node)){
			gatherHopNeighbors(ns, hops-1, x);
		}
		
	}
	public Collection<Integer> getHopNeighbors(int node,int hops){
		HashSet<Integer> ret = new HashSet<Integer>();
		gatherHopNeighbors(ret, hops, node);
		return ret;
	}
	
	public abstract Collection<Integer> getConnections(int x);
	public abstract void addNeighbors(int a,int b);
	public abstract void removeNeighbors(int a,int b);
	
	protected abstract int graphSize();
	
	public void init(DefaultGraph dg){
		alive = new boolean[dg.getNodeCount()];
		Arrays.fill(alive, true);
		groups = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<dg.getNodeCount();i++){
			ArrayList<Integer> ai = new ArrayList<Integer>();
			ai.add(i);
			groups.add(ai);
		}
	}
	
}
