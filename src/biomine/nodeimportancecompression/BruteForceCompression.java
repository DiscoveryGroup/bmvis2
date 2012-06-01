package biomine.nodeimportancecompression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import biomine.nodeimportancecompression.ImportanceGraph.Edge;

public class BruteForceCompression implements CompressionAlgorithm {

	public BruteForceCompression() {
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
			ImportanceMergerEdges.MergeInfo bestMerge = null;
			double mergeCost = 100000;
			int MergeSizeReduction = 0; // added by Fang
			for (int i = 0; i < ig.getMaxNodeId(); i++) {
				if (ig.hasNode(i)) {
					for (int j : im.getCurrentGraph().getHop2Neighbors(i)) {
						if (i >= j)
							continue;
						ImportanceMerger.MergeInfo info = im.getMergeInformation(i, j);
						double s;
						s = info.error / info.sizeReduction;
						// System.out.println("error = "+info.error);
						// System.out.println("sizeReduction = "+info.sizeReduction);
						if (s < min) {
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
				System.err
						.println("No suitable merge found, stopping compression");
				break;
			}
			ArrayList<Edge> f = new ArrayList<Edge>();
			double edgeDeleteCost = 10101010;
			{
				List<ImportanceGraph.Edge> edges = im.getCurrentGraph()
						.getEdges();

				/*
				 * for (Edge e : edges) { System.out.println("edge infor "+
				 * e.from +"  "+ e.to); //added by Fang }
				 */

				Collections.sort(edges, new Comparator<Edge>() {   
					@Override
					public int compare(Edge a, Edge b) {
					//	double ae = im.edgeDeleteError(a.from, a.to);    //modified by Fang
                                                double ae = Math.sqrt(im.edgeDeleteError(a.from, a.to));

					//	double be = im.edgeDeleteError(b.from, b.to);    //modified by Fang
                                                double be = Math.sqrt(im.edgeDeleteError(b.from, b.to));

						int as = im.edgeDeleteSizeReduction(a.from, a.to);
						int bs = im.edgeDeleteSizeReduction(b.from, b.to);
						return Double.compare(ae / as, be / bs);
					}
				});

				double edgeError = 0;
				int size = 0;
				Edge first = edges.get(0);
				edgeError = im.edgeDeleteError(first.from, first.to);
				size = im.edgeDeleteSizeReduction(first.from, first.to);
				f.add(first);
				edgeDeleteCost = Math.sqrt(edgeError) / size;
			}

			/*
			 * if (edgeDeleteCost < mergeCost) { //removed by Fang // delete
			 * edges from f for(Edge e:f){ im.deleteEdge(e.from, e.to); } } else
			 * {
			 * 
			 * // perform merge System.out.println("merging " + bestMerge.u +
			 * " " + bestMerge.v); System.out.println("reduction is " +
			 * bestMerge.sizeReduction); System.out.println("error is " +
			 * bestMerge.error); im.merge(bestMerge);
			 * System.out.println("new size is " + im.getSize());
			 * 
			 * }
			 */

			if (mergeCost < edgeDeleteCost
					&& MergeSizeReduction <= im.getSize() - goalSize) { // modified
																		// by
																		// Fang
				// perform merge
//				System.out.println("merging " + 
//						bestMerge.u + " " + bestMerge.v);
//				System.out.println("reduction is " + bestMerge.sizeReduction);
//				System.out.println("error is " + bestMerge.error);
				im.merge(bestMerge);
//				System.out.println("new size is " + im.getSize());
			} else {
				for (Edge e : f) {
					double edgeError = im.edgeDeleteError(e.from, e.to); // added
																			// by
																			// Fang
					im.deleteEdge(e.from, e.to);
					//im.deleteEdge(e.to, e.from); // added by Fang
													// "these two edges have same source and end node."
//					System.out.println("removing edge: " + e.from + "  " + e.to
//							+ "  " + "error: " + edgeError);
//
//					if (im.getCurrentGraph().getNeighbors(e.from).size() == 0) { // added by Fang
//																// "delete node"
//						im.deleteNode(e.from);
//						System.out.println("remove node: " + e.from);
//					}
//					if (im.getCurrentGraph().getNeighbors(e.to).size() == 0) { // added by Fang
//																// "delete node"
//						im.deleteNode(e.to);
//						System.out.println("remove node: " + e.to);
//					}
				}
				//System.out.println("new size is " + im.getSize());
			}

		}
	}
}
