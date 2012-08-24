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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import biomine.bmvis2.algoutils.DefaultGraph;

public class GreedyRatioCompression implements RatioCompression {

	public boolean hop2;

	public GreedyRatioCompression(boolean useHop2) {
		hop2 = useHop2;
	}

	public GreedyRatioCompression(CompressionMergeModel mo, boolean h) {
		setMergeModel(mo);
		hop2 = h;
	}

	private CompressionMergeModel mergeModel = new EdgeAverageMergeModel();

	public CompressionMergeModel getMergeModel() {
		return mergeModel;
	}

	public void setMergeModel(CompressionMergeModel mergeModel) {
		this.mergeModel = mergeModel;
	}

	//final double[] thlds = {0,0.05,0.1,0.2,0.3,0.5,1};

	final int THRESHOLD_COUNT = 20;// thlds.length;

	private double threshold(int n) {
		//return thlds[n];

		if (n == 0)
			return 0;

		n--;
		if (n >= THRESHOLD_COUNT)
			return 1;
		return 1.0 / (1 << (THRESHOLD_COUNT - 1 - n));
	}

	public ResultGraph compressGraph(DefaultGraph ig, double goalRatio) {
		mergeModel.init(ig);

		int totalCost = mergeModel.getStartCost();

		int goalCost = (int) (totalCost * goalRatio);

		int size = ig.getNodeCount();
		int thldN = 0;
		while (totalCost > goalCost) {
			boolean found = false;
			double minFound = 100;
			int a, b;
			a = b = 0;

			ArrayList<Integer> ro = new ArrayList<Integer>();
			for(int i=0;i<size;i++)
				ro.add(i);
			
			Collections.shuffle(ro);
			double lastD = -1;
			bestLoop: for (int i:ro){
				if (!mergeModel.isAlive(i))
					continue;
				if (found)
					break;
				Collection<Integer> mergeCheck ;
				if(hop2)
					mergeCheck= mergeModel.getHopNeighbors(i, 2);
				else {
					mergeCheck = new ArrayList<Integer>();
					for(int j=i+1;j<size;j++)
						mergeCheck.add(j);
				}
				
				// System.out.println("N1("+i+")="+mergeModel.getNeighbors(i).size());
				// System.out.println("N2("+i+")="+foo.size());
				//Collection<Integer> 
					
				 for(int j:mergeCheck){
					if (j == i)
						continue;
					if (!mergeModel.isAlive(j))
						continue;

					double d = mergeModel.mergeError(i, j);
					int reduction = mergeModel.mergeCostReduction(i, j);
					// System.out.println("try: "+i+" "+j+" d="+d);
					// if(reduction<=0)continue;
					// System.out.println(threshold(thldN));
					// minFound=Math.min(d,minFound);
					// System.out.println("md "+i+" "+j+" = "+d);

					if (d < minFound) {
						minFound = d;
						a = i;
						b = j;
					}

					if (d <= threshold(thldN)) {
						
						mergeModel.merge(i, j);
						// System.out.println("merge "+i+" "+j);
						lastD = d;
						totalCost -= reduction;
						found = true;
						if (totalCost <= goalCost)
							break bestLoop;
					}
				}
			}
			if (minFound >= 1.0)
				break;

			if (!found) {
				thldN++;
				// System.out.println("minFound = "+minFound+" "+a+" "+b);
				// System.out.println(threshold(thldN));
			}
		//System.out.println("t="+threshold(thldN)+" d="+lastD);
			if (thldN > THRESHOLD_COUNT)
				break;
		}
		System.out.println("totalCost = " + totalCost);
		System.out.println("goalCost = " + goalCost);

		return mergeModel.getResult();

	}

}