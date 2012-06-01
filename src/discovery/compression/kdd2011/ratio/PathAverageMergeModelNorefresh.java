package discovery.compression.kdd2011.ratio;

import java.util.Collection;

import biomine.bmvis2.algorithms.ProbDijkstra;
import biomine.bmvis2.algoutils.DefaultGraph;

public class PathAverageMergeModelNorefresh extends AverageMergeModel{

	private int startCost=0;
	IntermediateGraph ig;
	@Override
	public void addNeighbors(int a, int b) {
		ig.addNeighbors(a, b);
	}
	

	@Override
	public AverageMergeModel copy() {
		PathAverageMergeModelNorefresh ret = new PathAverageMergeModelNorefresh();
		ret.ig=ig.copy();
		ret.startCost=startCost;
		super.initCopy(ret);
		return ret;
	}

	@Override
	public double getConnection(int x, int y) {
		return ig.getConnection(x,y);
	}

	@Override
	public Collection<Integer> getConnections(int x) {
		return ig.getConnections(x);
	}

	@Override
	public Collection<Integer> getHopNeighbors(int x, int n) {
		return ig.getHopNeighbors(x, n);
	}

	@Override
	public Collection<Integer> getNeighbors(int x) {
		return ig.getNeighbors(x);
	}

	@Override
	protected int graphSize() {
		return ig.size();
	}

	@Override
	public void removeNeighbors(int a, int b) {
		ig.removeNeighbors(a, b);
	}

	@Override
	public void setConnection(int x, int y, double d) {
		ig.setConnection(x,y,d);
	}

	@Override
	public int getStartCost() {
		return startCost;
	}

	@Override
	public void init(DefaultGraph dg) {
		super.init(dg);
		startCost = dg.getEdgeCount();
		
		ig = new MapIntermediateGraph(dg.getNodeCount());
		for(int i=0;i<dg.getNodeCount();i++){
			for(int j:dg.getNeighbors(i)){
				ig.addNeighbors(i,j);
			}
		}
		for(int i=0;i<dg.getNodeCount();i++){
			ProbDijkstra pd = new ProbDijkstra(dg, i);
			for(int j=0;j<i;j++){
				ig.setConnection(i,j,pd.getProbTo(j));
			}
			ig.setConnection(i,i,-1);
		}
	}

}
