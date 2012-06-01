package biomine.bmvis2.algorithms;

import java.util.Random;

import biomine.bmvis2.SimpleVisualGraph;

/**
 * Implementation of kmedoids for prob-graphs.
 * @author alhartik
 *
 */
public class KMedoids {

	private static double medoidsCost(double[][] probMatrix, int[] meds) {
		int n = probMatrix.length;

		double ret = 1;
		for (int i = 0; i < n; i++) {
			double minDist = Double.MAX_VALUE;
			for (int m : meds) {
				double prob = probMatrix[i][m];
				double dist = -Math.log(prob);
				minDist = Math.min(minDist, dist);
			}
			ret += minDist;
		}
		return ret;
	}

	public static class KMedoidsResult {
		public int[] medoids;
		public int[] clusters;
	}

	public static KMedoidsResult runKMedoids(SimpleVisualGraph sg, int k) {
		return runKMedoids(ProbDijkstra.getProbMatrix(sg), k);
	}

	public static KMedoidsResult runKMedoids(double[][] probMatrix, int k) {

		int n = probMatrix.length;

		System.out.println("nn = " + n);
		int[] medoids = new int[k];

		boolean[] isMedoid = new boolean[n];
		// select random starting medoids
		{
			Random rand = new Random();
			int[] r = new int[n];
			for (int i = 0; i < n; i++)
				r[i] = i;
			for (int i = 0; i < k; i++) {
				int ri = i + rand.nextInt(n - i);
				medoids[i] = r[ri];
				isMedoid[r[ri]] = true;
				r[ri] = r[i];
			}
		}

		boolean change = true;
		double cost = medoidsCost(probMatrix, medoids);
		int iterations = 0;
		while (change) {
			iterations++;
			change = false;
			for (int mi = 0; mi < k; mi++) {
				for (int i = 0; i < n; i++) {
					if (isMedoid[i])
						continue;
					int oldMed = medoids[mi];
					medoids[mi] = i;
					double p = medoidsCost(probMatrix, medoids);
					if (p < cost) {
						cost = p;
						isMedoid[oldMed] = false;
						medoids[mi] = i;
						change = true;
					} else {
						medoids[mi] = oldMed;
					}
				}
			}
		}

		System.out.printf("final cost: %.5f in %d iterations\n", cost,
				iterations);

		KMedoidsResult ret = new KMedoidsResult();
		ret.medoids = medoids;
		ret.clusters = new int[n];
		
		for (int i = 0; i < n; i++) {
			double max = 0;
			int cluster = 0;
			for (int j = 0; j < k; j++) {
				double c = probMatrix[i][medoids[j]];
				if (c > max) {
					max = c;
					cluster = j;
				}
			}
			ret.clusters[i]=cluster;
		}
		return ret;

	}
}
