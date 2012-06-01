package discovery.compression.kdd2011.old;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGroupNode;

public class FastGreedyCompressor extends Compressor {

	public static String NAME = "Greedy compressor";

	CompressedGraph lastCGraph=null;
	public String makeGroups(VisualGroupNode group) {
		CompressedGraph cgraph = super.createCompressedGraph(group);

		long startTime = System.currentTimeMillis();

		int fails = 0;

		startCosts = cgraph.getCosts();

		// We are using TreeMap as a sort of priority queue
		// where we can also remove stuff

		class NodePair implements Comparable<NodePair> {
			NodePair(int f, int t, double red) {
				r = red;
				a = Math.min(f, t);
				b = Math.max(f, t);
			}

			private int a, b;
			private double r;

			public int from() {
				return a;
			}

			public int to() {
				return b;
			}

			public double reduction() {
				// this is rounded to help floating point rounding errors

				return ((int) (r * (1 << 16)) / ((double) (1 << 16)));
			}

			public int other(int z) {
				if (a == z)
					return b;
				assert (b == z) : "kekasdf";
				return a;
			}

			@Override
			public int compareTo(NodePair o) {
				if (reduction() != o.reduction())
					return Double.compare(o.reduction(), reduction());
				if (a != o.a)
					return a - o.a;
				if (b != o.a)
					return b - o.b;
				return 0;
			}

			public boolean equals(Object o) {
				if (o instanceof NodePair) {
					return compareTo((NodePair) o) == 0;
				}
				return false;
			}

			public int hashCode() {
				return new Double(reduction()).hashCode()
						+ (a * (31 + (b << 16) + (b >> 16)));
			}

			public String toString() {
				return "(" + a + "->" + b + "," + r + ")";
			}
		}
		int n = cgraph.size();

		double thld = 0;
		// for (double thld = 0.25; thld >= 0; thld -= 0.05)
		{

			TreeSet<NodePair> pairs = new TreeSet<NodePair>();
			Vector<TreeSet<NodePair>> myPairs = new Vector<TreeSet<NodePair>>();

			myPairs.setSize(n);
			for (int ai = 0; ai < n; ai++) {
				myPairs.set(ai, new TreeSet<NodePair>());
				int g = ai;
				HashSet<Integer> twohops = new HashSet<Integer>();

				for (int z : cgraph.neighbors(g)) {
					for (int x : cgraph.neighbors(z)) {
						int u = x;// nodeGroup[x];
						twohops.add(u);
					}
				}
				twohops.remove(g);
				for (int u : twohops) {
					double s = cgraph.reduction(g, u).s;

					if (s >= thld) {
						NodePair p = new NodePair(g, u, s);
						pairs.add(p);
						myPairs.get(g).add(p);
					}
				}
			}

			int aliveCount = n;
//			System.out.println("cost is " + cgraph.getCosts());
//			System.out.println("pairs = " + pairs);
//			System.out.println("aliveCount = " + aliveCount);
			while (pairs.size() != 0 && aliveCount > 1) {

				// d("pairs.size() = " + pairs.size());
				// System.out.println("aliveCount = "+aliveCount);
				// for(int i=0;i<n;i++)
				// System.out.print(""+i+"="+cgraph.alive(i)+" ");
				// System.out.println("");
				// for(NodePair p:pairs){
				// System.out.println(p.from()+"-"+p.to());
				// }

				// BEGIN CHECKING OF NEIGHBORS
//				for (int i = 0; i < n; i++) {
//					if(cgraph.alive(i))
//						for (int z : cgraph.neighbors(i)) {
//							if (cgraph.neighbors(z).contains(i) == false)
//								assert false : "fuu";
//						}
//				}
				// CHECKING OF MYPAIRS
//				for (NodePair p : pairs) {
//					if (!myPairs.get(p.from()).contains(p)) {
//						System.err.println("fuu: " + p.from() + " " + p.to()
//								+ " is missing" + " from myPairs.get("
//								+ p.from() + ")!");
//						System.err.println("reduction = " + p.reduction());
//					}
//					if (!myPairs.get(p.to()).contains(p)) {
//						System.err.println("fuu: " + p.from() + " " + p.to()
//								+ " is missing" + " from myPairs.get(" + p.to()
//								+ ")!");
//						System.err.println("reduction = " + p.reduction());
//					}
//				}

				// END CHECK

				NodePair bestPair = pairs.first();
				pairs.remove(bestPair);
				int best = bestPair.from();
				int bestMerge = bestPair.to();

				// double realReduction = reduction(best, bestMerge);
				//			  
				// if (realReduction != bestPair.reduction())
				// System.err.println("adsf");
				//			 
				// System.err.println("merge " + best + " with " + bestMerge +
				// "reduction=" + bestPair.reduction() + " == " +
				// realReduction);

				//System.out.println("best pair is " + bestPair);
				if (bestPair.reduction() <= 0.0) {
					break;
				}

				if (cgraph.alive(best) == false
						|| cgraph.alive(bestMerge) == false) {
					// System.err.println(bestMerge + " is dead");
					continue;
				}

				cgraph.merge(best, bestMerge);
				aliveCount--;

				TreeSet<NodePair> removedPairs = new TreeSet<NodePair>();
				// update neighbor-pairs
				for (int neighbor : cgraph.neighbors(best)) {
					assert neighbor != best && neighbor != bestMerge : "lol?";
					// pairs.removeAll(myPairs.get(neighbor));
					removedPairs.addAll(myPairs.get(neighbor));
					// TreeSet<NodePair> myPairsNew = new TreeSet<NodePair>();

					HashSet<Integer> twohops = new HashSet<Integer>();
					for (int one : cgraph.neighbors(neighbor)) {
						for (int two : cgraph.neighbors(one)) {

							twohops.add(two);
						}
					}
					twohops.remove(neighbor);

					// for (NodePair np : myPairs.get(neighbor)) {
					// int o = np.other(neighbor);
					// if (o == bestMerge)
					// o = best;
					for (int o : twohops) {

						double s = cgraph.reduction(neighbor, o).s;
						if (s > 0) {
							NodePair nnp = new NodePair(neighbor, o, s);
							pairs.add(nnp);
							myPairs.get(o).add(nnp);
							myPairs.get(neighbor).add(nnp);
							removedPairs.remove(nnp);
						} else {
							//System.out.println("C(" + best + ")="
									//+ cgraph.superNodeCost(best));
							//System.out.println("C(" + o + ")="
									//+ cgraph.superNodeCost(o));
						}
					}
					// myPairs.set(neighbor, myPairsNew);
				}
				//System.out.println("removedPairs = " + removedPairs);
				pairs.removeAll(removedPairs);
				for (NodePair np : removedPairs) {
					myPairs.get(np.from()).remove(np);
					myPairs.get(np.to()).remove(np);
				}

				{
					// NOT update pairs containing best or bestMerge
					myPairs.get(best).addAll(myPairs.get(bestMerge));
					pairs.removeAll(myPairs.get(best));
					// BUT recalc pairs for best
					Collection<Integer> twohops = cgraph.hopNeighbors(best, 2);
					twohops.addAll(cgraph.hopNeighbors(best, 1));

					myPairs.get(bestMerge).clear();

					TreeSet<NodePair> myPairsNew = new TreeSet<NodePair>();
					for (int o : twohops) {
						double s = cgraph.reduction(best, o).s;
						if (s > 0) {
							NodePair nnp = new NodePair(best, o, s);
							pairs.add(nnp);
							myPairs.get(o).add(nnp);
							myPairsNew.add(nnp);
						} 
					}
					myPairs.set(best, myPairsNew);
				}

			}
		}
		// System.err.println("fails = "+fails);

		cgraph.performGrouping(useAuto());
		lastCosts = cgraph.getCosts();

		long totalMillis = System.currentTimeMillis() - startTime;
		System.out.println("time spent: " + (0.001 * totalMillis));
		lastTime = 0.001 * totalMillis;
		lastCGraph = cgraph;
		return 	"supernodes:"+lastCosts.supernodes+"\n" +
				"superedges:"+lastCosts.superedges+"\n" + 
				"corrections:"+lastCosts.corrections+"\n";
	}

	private void d(Object o) {
		System.err.println(o);
		// TODO Auto-generated method stub

	}

	private double lastTime;

	public double getLastTime() {
		return lastTime;
	}

	public void setLastTime(double lastTime) {
		this.lastTime = lastTime;
	}

	CompressorCosts lastCosts;
	CompressorCosts startCosts;

	public CompressorCosts getStartingCosts() {
		return startCosts;
	}

	public CompressorCosts getResultCosts() {
		return lastCosts;
	}

	@Override
	public SimpleVisualGraph getUncompressedGraph() {
		return lastCGraph.uncompressedGraph();
	}
	@Override
	public SimpleVisualGraph getOriginalGraph(){
		return lastCGraph.getSimpleGraph();
	}
}
