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
