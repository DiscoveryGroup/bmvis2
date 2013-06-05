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

public class BruteForceCompressionOnlyMerges implements CompressionAlgorithm {

	public BruteForceCompressionOnlyMerges() {
		
		}



		@Override
	public void compress(final ImportanceMerger im, double goalRatio) {
		ImportanceGraph ig = im.getOriginalGraph();
		int origSize = im.getSize();
		int goalSize = (int) (origSize * goalRatio);
		System.out.println("original size = " + origSize);
		System.out.println("goal size = " + goalSize);

		while (im.getSize() > goalSize) {

			int operation = 1;

			// select merge with smallest error or error / sizeRed ratio
			double min = 10000;
			ImportanceMergerEdges.MergeInfo bestMerge = null;
			double mergeCost = 100000;
			int MergeSizeReduction = 0; // added by Fang
			for (int i = 0; i < ig.getMaxNodeId(); i++) {
				if (ig.hasNode(i)) {
					for (int j : im.getCurrentGraph().getHop2Neighbors(i)) {
						if (i >= j)
							continue;
						ImportanceMergerEdges.MergeInfo info = im.getMergeInformation(i, j);
						double s;
						s = info.error / info.sizeReduction;
						// System.out.println("error = "+info.error);
						// System.out.println("sizeReduction = "+info.sizeReduction);

					//	if (s < min) { // modified by Fang
                                                if (s < min && info.sizeReduction <= im.getSize()- goalSize) {
							min = s;
							MergeSizeReduction = info.sizeReduction; // added by
																		// Fang
							bestMerge = info;
						}
					}
				}
			}

			mergeCost = min;

			if (bestMerge == null) {
				System.err.println("No suitable merge found, stopping compression");
				break;
			}


			// if ( MergeSizeReduction <= im.getSize() - goalSize)
			{ // modified by Fang
				// perform merge
				//System.out
						//.println("merging " + bestMerge.u + " " + bestMerge.v);
				//System.out.println("reduction is " + bestMerge.sizeReduction);
				//System.out.println("error is " + bestMerge.error);
				im.merge(bestMerge);
				//System.out.println("new size is " + im.getSize());
			}

		}
	}
}
