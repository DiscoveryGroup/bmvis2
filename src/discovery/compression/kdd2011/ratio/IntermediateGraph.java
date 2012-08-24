/*
 * Copyright 2012 University of Helsinki.
 *
 * This file is part of BMVis².
 *
 * BMVis² is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * BMVis² is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BMVis².  If not, see
 * <http://www.gnu.org/licenses/>.
 */

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

