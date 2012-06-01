package biomine.nodeimportancecompression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ImportanceMerger {

	private ImportanceGraph originalGraph;
	private ImportanceGraph currentGraph;
	private double[] impSum;
	private double[] pairwiseImpSum;
	private ArrayList<ArrayList<Integer>> groups;
	
	public abstract double getConnection(int u, int v);

	public double impSum(int v) {
		return impSum[v];
	}
	public double pairwiseImpSum(int v){
		return pairwiseImpSum[v];
	}
	public abstract void setKeepEdges(boolean ke);
	public abstract boolean keepEdges();
	public ImportanceMerger(ImportanceGraph graph) {
		originalGraph = (graph.copy());
		currentGraph = graph.copy();
		groups = new ArrayList<ArrayList<Integer>>(1 + graph.getMaxNodeId());
		impSum = new double[1 + graph.getMaxNodeId()];
		pairwiseImpSum = new double[1 + graph.getMaxNodeId()];

		for (int i = 0; i < graph.getMaxNodeId() + 1; i++) {
			if (graph.hasNode(i)) {
				groups.add(new ArrayList<Integer>());
				groups.get(i).add(i);
				impSum[i] = graph.getImportance(i);
				pairwiseImpSum[i] = 0;
			}
		}
	}

	public ImportanceGraph getUncompressedGraph() {
		ImportanceGraph ret = new ImportanceGraph();
		for (int i = 0; i <= currentGraph.getMaxNodeId(); i++) {
			for (int j : currentGraph.getNeighbors(i)) {
				if (j <= i)
					continue;
				for (int ix : groups.get(i))
					for (int jx : groups.get(j))
						ret.addEdge(ix, jx, getConnection(i, j));
			}
		}
		return ret;
	}

	public void deleteEdge(int u,int v){
		currentGraph.removeEdge(u,v);
		if(currentGraph.getNeighbors(u).size()==0)
			deleteNode(u);
		if(currentGraph.getNeighbors(v).size()==0)
			deleteNode(v);
		postEdgeDeletion(u, v);
	}

    public void deleteNode(int u){    // added by Fang
		currentGraph.removeNode(u);
		groups.get(u).clear();
	}
	public final ImportanceGraph getCurrentGraph() {
		return currentGraph;
	}

	public final ArrayList<ArrayList<Integer>> getGroups() {
		return groups;
	}

	public final ImportanceGraph getOriginalGraph() {
		return originalGraph;
	}

	public final int getSize(){
		return currentGraph.getSize();
	}

	/**
	 * Contains information about possible merge. Remains valid until any merge
	 * is done.
	 * 
	 * @author aleksi
	 * 
	 */
	public static class MergeInfo {
		public int sizeReduction;
		public double error;
		public int u, v;
		public Map<Integer, Double> newWeights;
		public double selfNewWeight;
		// public Map<Integer,Double> errors;
		public double selfError;
		public boolean hasSelfEdge;
		public Set<Integer> removedEdges;
	}

	public abstract MergeInfo getMergeInformation(int i, int j);

	public abstract double edgeDeleteError(int from, int to);

	public int edgeDeleteSizeReduction(int u,int v){
		int red = 1;
		if(u==v){
			if(getNeighbors(u).size()==1)red++;
		}else{
			if(getNeighbors(u).size()==1)red++;
			if(getNeighbors(v).size()==1)red++;
		}
		return red;
	}

	public int merge(MergeInfo info) {

		int u = info.u;
		int v = info.v;
		int z = u;

		HashSet<Integer> un = new HashSet<Integer>(currentGraph.getNeighbors(u));
		HashSet<Integer> vn = new HashSet<Integer>(currentGraph.getNeighbors(v));

		HashSet<Integer> xs = new HashSet<Integer>(un);

		xs.addAll(vn);

		xs.remove(u);
		xs.remove(v);

		int oldSize = currentGraph.getSize();

		for (int x : un) {
			currentGraph.removeEdge(u, x);
		}
		currentGraph.removeNode(v);

		// handle all non-self-edges
		for (int x : xs) {
			if (!info.removedEdges.contains(x)) {
				currentGraph.addEdge(z, x, info.newWeights.get(x));
			} else {
			//	System.out.println("'removed' edge");
				if (currentGraph.getNeighbors(x).size() == 0) {
					currentGraph.removeNode(x);
					//System.out.println("deleting node " + x);
				}
			}
		}

		// handle self-edge
		if (info.hasSelfEdge) {
			if (!info.removedEdges.contains(v))
				currentGraph.addEdge(z, z, info.selfNewWeight);
		//	else
			//	System.out.println("Didn't add self-edge");
		}

		groups.get(u).addAll(groups.get(v));
		groups.get(v).clear();

		pairwiseImpSum[z] = pairwiseImpSum[v] + pairwiseImpSum[u] + impSum[v]* impSum[u];
		impSum[z] = impSum[v] + impSum[u];

		int size = currentGraph.getSize();
		int E = currentGraph.getEdgeCount();
		int N = currentGraph.getNodeCount();
		if (oldSize - size != info.sizeReduction) {
			System.out.println("ERROR:");
			System.out.println("u = " + u + " v = " + v);
			System.out.println("oldSize = " + oldSize);
			System.out.println("size = " + size);
			System.out.println("hasSelfEdge = " + info.hasSelfEdge);
			System.out.println("promised reduction = " + info.sizeReduction);
			System.out.println("removed edges = "+info.removedEdges);
			System.out.println("exiting");
			System.exit(0);
		}
        postMerge(info);

		return z;

	}


	// convenience functions for subclasses

	protected double newWeight(int u, int v, int x) {
		assert u != v;
		double sum = 0;
		double w_ux = getConnection(v, x);
		double w_vx = getConnection(v, x);
		sum += impSum[x] * impSum[u] * w_ux;
		sum += impSum[x] * impSum[v] * w_vx;
		return sum / (impSum[x] * (impSum[u] + impSum[v]));
	}

	protected double selfNewWeight(int u, int v) {
		double sum = 0;
		double w_uv = getConnection(u, v);
		double w_vv = getConnection(v, v);
		double w_uu = getConnection(u, u);
		double imps = 0;
		imps += pairwiseImpSum[u];
		sum += pairwiseImpSum[u] * w_uu;
		imps += impSum[u] * impSum[v];
		sum += impSum[u] * impSum[v] * w_uv;
		imps += pairwiseImpSum[v];
		sum += pairwiseImpSum[v] * w_vv;

		return sum / imps;
	}

	public Collection<Integer> getNeighbors(int x){
		return currentGraph.getNeighbors(x);
	}
	
	/**
	 * Override to do something after merge
	 * @param info
	 */
	protected void postMerge(MergeInfo info){
		
	}

	/**
	 * Override to do something after edge deletion
	 * @param u
	 * @param v
	 */
	protected void postEdgeDeletion(int u, int v) {
		return;
	}
}
