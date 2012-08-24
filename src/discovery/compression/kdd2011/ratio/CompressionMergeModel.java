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
