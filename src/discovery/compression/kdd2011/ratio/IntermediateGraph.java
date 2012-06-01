package discovery.compression.kdd2011.ratio;

import java.util.Collection;

/**
 * Intermediate graph format used for all connectivity compression techniques (edge,local,global).
 * Contains information about connection qualities and neighbors.
 * @author alhartik
 *
 */

public interface IntermediateGraph{
	
	public int size();
	public void setConnection(int from,int to,double w);
	public void addNeighbors(int a,int b);
	public void removeNeighbors(int a,int b);
	
	/**
	 * @param i
	 * @return Neighbors of i
	 */
	public Collection<Integer> getNeighbors(int i);
	/**
	 * @param i
	 * @return superset of all non-zero connections for node i
	 */
	public Collection<Integer> getConnections(int i);
	public double getConnection(int i, int j);
	public IntermediateGraph copy();
	public Collection<Integer> getHopNeighbors(int x, int n);
}

