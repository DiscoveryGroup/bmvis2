package discovery.compression.kdd2011.ratio;

import java.util.Collection;

import biomine.bmvis2.algoutils.DefaultGraph;
import discovery.compression.kdd2011.ratio.RatioCompression.ResultGraph;

public interface CompressionMergeModel {	
	public void init(DefaultGraph ig);
	public double mergeError(int i,int j);
	public int mergeCostReduction(int i,int j);
	public int getStartCost();
	public void merge(int to,int from);
	public ResultGraph getResult();
	public boolean isAlive(int node);
	public Collection<Integer> getNeighbors(int n);
	public Collection<Integer> getHopNeighbors(int n,int hops);
	public CompressionMergeModel copy();
	
}
