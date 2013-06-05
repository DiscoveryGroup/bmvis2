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

package biomine.nodeimportancecompression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import biomine.nodeimportancecompression.ImportanceGraph.Edge;

public class BruteForceCompressionOnlyEdges implements CompressionAlgorithm {

	

	public BruteForceCompressionOnlyEdges() {
	}

	
	@Override
	public void compress(final ImportanceMerger im, double goalRatio) {
		ImportanceGraph ig = im.getOriginalGraph();
		int origSize = im.getSize();
		int goalSize = (int) (origSize * goalRatio);
//		System.out.println("original size = " + origSize);
//		System.out.println("goal size = " + goalSize);

		while (im.getSize() > goalSize) {

			int operation = 1;

			// select merge with smallest error or error / sizeRed ratio
			double min = 10000;
			int MergeSizeReduction = 0; // added by Fang

			ArrayList<Edge> f = new ArrayList<Edge>();
			double edgeDeleteCost = 10101010;
			List<ImportanceGraph.Edge> edges = im.getCurrentGraph().getEdges();
			Edge bestEdge = null;

			for (Edge a : edges) {
				double ae = Math.sqrt(im.edgeDeleteError(a.from, a.to));
				int as = im.edgeDeleteSizeReduction(a.from, a.to);
				if (ae / as < min) {
					min = ae / as;
					bestEdge = a;
				}
			}

			if(bestEdge!=null){
				Edge e = bestEdge;
				im.deleteEdge(e.from, e.to);
			}
//			System.out.println("new size is " + im.getSize());

		}
	}
}
