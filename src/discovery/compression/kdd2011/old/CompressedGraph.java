package discovery.compression.kdd2011.old;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import biomine.bmvis2.GroupEdgeUtils;
import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualGroupNodeAutoEdges;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;

public final class CompressedGraph {

	private double deviation = 0.1;
	private double k;

	public double getK() {
		return k;
	}

	public void setK(double k) {
		this.k = k;
		recalcCost();
	}

	public double getDeviation() {
		return deviation;

	}

	private VisualGroupNode vgn;
	private SimpleVisualGraph sg;

	private boolean labeledCost;

	public boolean isLabeledCost() {
		return labeledCost;
	}

	public void setLabeledCost(boolean labeledCost) {
		this.labeledCost = labeledCost;
	}

	public void mergeParallelNeighbors() {

		if (labeledCost == false) {
			for (int i = 0; i < sg.n; i++) {
				int count = 0;
				ArrayList<Integer> suitable = new ArrayList<Integer>();
				for (SimpleEdge se : sg.getEdges(i)) {
					int t = se.to;
					if (sg.getEdges(t).size() == 1) {
						suitable.add(t);
					}
				}
			}
		}
	}

	int getLabelCosts(int supernode) {

		int max = 0;
		HashMap<String, Integer> labels = new HashMap<String, Integer>();
		for (int c : groups.get(supernode)) {
			VisualNode vn = sg.getVisualNode(c);
			String lbl = vn.getType();
			Integer oc = labels.get(lbl);
			if (oc == null)
				oc = 0;
			oc++;
			labels.put(lbl, oc);
			if (oc > max)
				max = oc;
		}
		int labelCost = groups.get(supernode).size() - max;
		return labelCost;
	}

	public CompressedGraph(VisualGroupNode g, double dev) {
		deviation = dev;
		vgn = g;
		sg = new SimpleVisualGraph(g);
		groups = new Vector<Set<Integer>>(sg.n);
		edges = new Vector<HashMap<Integer, Double>>(sg.n);
		neighbors = new Vector<HashSet<Integer>>(sg.n);

		groups.setSize(sg.n);
		edges.setSize(sg.n);
		neighbors.setSize(sg.n);

		nodeGroup = new int[size()];
		groupCost = new NodeCost[size()];
		for (int i = 0; i < sg.n; i++) {
			nodeGroup[i] = i;
			groups.set(i, new HashSet<Integer>(Collections.singleton(i)));
		}
		for (int i = 0; i < sg.n; i++) {
			edges.set(i, new HashMap<Integer, Double>());
			neighbors.set(i, new HashSet<Integer>());
			for (SimpleEdge se : sg.getEdges(i)) {
				// if edge is already in set, only put the edge
				// with highest weight
				Double ow = edges.get(i).get(se.to);
				if (ow != null && ow > se.weight)
					continue;

				edges.get(i).put(se.to, se.weight);
				neighbors.get(i).add(se.to);
			}
		}
		this.recalcCost();
	}

	int countCorrections(){
		int ret = 0;
		for (int i = 0; i < size(); i++) {
			if (groups.get(i).size() > 0) {
				ArrayList<Integer> ns = new ArrayList<Integer>(neighbors(i));
				if (!neighbors(i).contains(i)) {
					// System.out.println("neighbors("+i+") did not contain "+i);
					ns.add(i);
				}
				for (int j : ns) {
					EdgeCost ec = superEdgeCost(i, j);
					// System.out.println("superEdgeCost("+i+","+j+") = "+ec.cost+" g="+ec.goodness+" c="+ec.corrections);

					if (j == i) {
						ret += 2 * ec.corrections;
					} else {
						ret += ec.corrections;
					}
				}
			}
		}
		ret /= 2;
		return ret;
	}
	CompressorCosts getCosts() {
		CompressorCosts ret = new CompressorCosts();
		for (int i = 0; i < size(); i++) {
			if (labeledCost)
				ret.labelCorrections += getLabelCosts(i);
			if (groups.get(i).size() > 0) {
				ret.supernodes++;
				ArrayList<Integer> ns = new ArrayList<Integer>(neighbors(i));
				if (!neighbors(i).contains(i)) {
					// System.out.println("neighbors("+i+") did not contain "+i);
					ns.add(i);
				}
				for (int j : ns) {
					EdgeCost ec = superEdgeCost(i, j);
					// System.out.println("superEdgeCost("+i+","+j+") = "+ec.cost+" g="+ec.goodness+" c="+ec.corrections);

					if (j == i) {
						if (ec.goodness != 0.0)
							ret.superedges += 2;
						ret.corrections += 2 * ec.corrections;
					} else {
						if (ec.goodness != 0.0)
							ret.superedges++;
						ret.corrections += ec.corrections;
					}
				}
			}
		}
		ret.superedges /= 2;
		ret.corrections /= 2;
		ret.corrections *= k;
		return ret;
	}

	int size() {
		return sg.n;
	}

	public static class EdgeCost {
		double cost;
		double goodness;
		double corrections;
	}

	private void hopGather(int n, int h, Set<Integer> set) {
		if (h == 0)
			set.add(n);
		else
			for (int i : neighbors(n)) {
				hopGather(i, h - 1, set);
			}

	}

	/**
	 * 
	 * @param n
	 * @param hops
	 * @return
	 */
	Collection<Integer> hopNeighbors(int n, int hops) {
		HashSet<Integer> ret = new HashSet<Integer>();
		hopGather(n, hops, ret);
		ret.remove(n);
		return ret;
	}

	final EdgeCost superEdgeCost(int f, int t) {
		EdgeCost ret = new EdgeCost();

		ArrayList<Double> weights = new ArrayList<Double>();
		for (int x : groups.get(f)) {
			for (int y : groups.get(t)) {
				if (t == f)
					if (y <= x)
						continue;
				double goodness = 0;
				if (edges.get(x).containsKey(y)) {
					goodness = edges.get(x).get(y);
				}
				weights.add(goodness);
			}
		}

		if (weights.size() == 0) {
			ret.corrections = 0;
			ret.corrections = 0;
			ret.goodness = 0;
			return ret;
		}
		int z = weights.size();
		Collections.sort(weights);
		assert (z == weights.size());
		int li = 0;
		double bestWeight = 0;
		int bestCount = 0;

		for (int i = 0; i < weights.size(); i++) {
			if (weights.get(i) < deviation)
				bestCount++;
			else
				break;
		}

		for (int i = 0; i < weights.size(); i++) {
			
			double ub = weights.get(i);
			double lb = weights.get(li);
			
			while (ub - lb > deviation * 2) {
				// move lower index
				li++;
				lb = weights.get(li);
			}
			
			int c = i - li + 1;
			
			if (bestCount <= c) {
				bestCount = c;
				bestWeight = (lb + ub) / 2;
			}
		}

		ret.corrections = (weights.size() - bestCount);
		ret.cost = ret.corrections * k;// corrections
		// System.out.println("bestWeight = "+bestWeight);
		// System.out.println("s="+weights.size());
		// System.out.println(weights.get(0));
		if (bestWeight != 0)
			ret.cost += 1;
		ret.goodness = bestWeight;

		return ret;
	}

	public void setDeviation(double deviation) {
		this.deviation = deviation;
		recalcCost();
	}

	private void recalcCost() {
		for (int i = 0; i < groups.size(); i++)
			groupCost[i] = superNodeCost(i);
	}

	public static class NodeCost {
		double cost;
		int corrections;
	}

	public NodeCost superNodeCost(int supernode) {
		NodeCost ret = new NodeCost();
		double total = 1; // start with 1 = supernode cost
		HashSet<Integer> visited = new HashSet<Integer>();

		HashSet<Integer> ns = new HashSet<Integer>(neighbors(supernode));
		ns.add(supernode);

		for (Integer z : ns) {
			EdgeCost ec = superEdgeCost(supernode, z);
			double a = ec.cost;
			ret.corrections += ec.corrections;
			total += a;
		}

		if (labeledCost) {
			total += getLabelCosts(supernode);
		}

		ret.cost = total;
		return ret;
	}

	private NodeCost[] groupCost;
	private Vector<HashMap<Integer, Double>> edges;
	private Vector<Set<Integer>> groups;
	private Vector<HashSet<Integer>> neighbors;
	private int[] nodeGroup;

	public Set<Integer> neighbors(int supernode) {
		return Collections.unmodifiableSet(neighbors.get(supernode));
	}

	/**
	 * Merges node "from" into node "to" node "from" becomes empty.
	 * 
	 * @param to
	 * @param from
	 */
	public void merge(int to, int from) {
		// System.out.println("merging "+sg.visualNodes[to]+" "+sg.visualNodes[from]);
		// System.out.println("merging "+to+" "+from);
		Set<Integer> ofg = groups.get(from);
		groups.set(from, Collections.EMPTY_SET);
		for (int g : ofg) {
			nodeGroup[g] = to;
		}
		// update neighbors
		for (int fn : neighbors(from)) {
			neighbors.get(fn).remove(from);
		}
		
		neighbors.get(to).addAll(neighbors.get(from));
		neighbors.get(to).remove(to);
		neighbors.get(to).remove(from);

		for (int tn : neighbors(to)) {
			neighbors.get(tn).add(to);
		}

		groups.get(to).addAll(ofg);

		// update costs of neighbors
		for (int tn : neighbors(to)) {
			groupCost[tn] = superNodeCost(tn);
		}
		groupCost[to] = superNodeCost(to);
	}

	boolean alive(int supernode) {
		return groups.get(supernode).size() != 0;
	}

	public static class Reduction {
		public Reduction(double a, int corrections) {
			s = a;
			newCorrections = corrections;
		}

		double s;
		int newCorrections;
	}

	public Reduction reduction(int f, int t) {
		if (!alive(f) || !alive(t)) {
			// System.out.println("FUASD");
			return new Reduction(-100,0);
		}

		// try merging
		double cf = groupCost[f].cost;
		double ct = groupCost[t].cost;
		int oldCorrections = groupCost[f].corrections+groupCost[t].corrections;
		//these are counted twice
		oldCorrections -= superEdgeCost(f, t).corrections;
		// System.out.println("C("+f+")="+cf);
		// System.out.println("C("+t+")="+ct);

		ArrayList<Integer> ns = new ArrayList<Integer>(groups.get(f));

		groups.get(f).removeAll(ns);
		for (int node : ns) {
			groups.get(t).add(node);
			nodeGroup[node] = t;
		}

		HashSet<Integer> on = neighbors.get(t);
		neighbors.set(t, new HashSet<Integer>(on));
		neighbors.get(t).addAll(neighbors(f));
		neighbors.get(t).remove(f);
		NodeCost snc = superNodeCost(t);
		double cw = snc.cost;
		// System.out.println("neighbors of w(="+t+") are "+neighbors.get(t));
		// System.out.println("C(w)="+cw);

		int nc = snc.corrections;
		neighbors.set(t, on);

		for (int node : ns) {
			groups.get(t).remove(node);
			nodeGroup[node] = f;
		}

		groups.get(f).addAll(ns);
		double s =  (cf + ct - cw) / (cf + ct);
		Reduction ret = new Reduction(s, nc-oldCorrections);
		return ret;

	}

	public void performGrouping(boolean useAuto) {
		final int n = groups.size();

		class GroupClosure {
			VisualNode[] newGroups = new VisualNode[n];

			private VisualGroupNode createNode(final int i, boolean useAuto) {
				if (useAuto) {
					newGroups[i] = new VisualGroupNodeAutoEdges(vgn);
				} else {

					newGroups[i] = new VisualGroupNode(vgn) {
						@Override
						public void createEdges() {
							ArrayList<Integer> ns = new ArrayList<Integer>(
									neighbors(i));
							ns.add(i);
							for (int j : ns) {
								// for (int j : neighbors.get(i)) {
								VisualNode vj = newGroups[j];
								EdgeCost ec = superEdgeCost(i, j);
								if (ec.goodness != 0.0) {
									// adding edges
									// System.err.println("ec.goodness = " +
									// ec.goodness);
									GroupEdgeUtils.makeGroupEdge(
											(VisualGroupNode) newGroups[i], vj,
											ec.goodness);
								}
							}
						}
					};// anonymous visualgroupnode end

				}
				VisualGroupNode ret = (VisualGroupNode) newGroups[i];
				for (int j : groups.get(i)) {
					sg.getVisualNode(j).setParent(ret);
					// nodes.get(j
				}
				ret.setName("Compressed Group "+ret.getChildren().size());
				return ret;
			}
		}
		try {
			vgn.getGraph().disableObservers();
			GroupClosure cl = new GroupClosure();
			for (int i = 0; i < n; i++) {
				int g = i;
				if (groups.get(g).size() > 1) {
					VisualGroupNode nn = cl.createNode(g, useAuto);
				} else if (groups.get(g).size() == 1) {
					cl.newGroups[i] = sg.getVisualNode(i);
				}
			}
		} finally {
			vgn.getGraph().enableObservers();
		}
	}

	public SimpleVisualGraph uncompressedGraph() {
		SimpleVisualGraph ret = new SimpleVisualGraph(sg.getVisualNodes());

		ArrayList<HashMap<Integer, Double>> superedges = new ArrayList<HashMap<Integer, Double>>();
		for (int i = 0; i < ret.n; i++) {
			superedges.add(new HashMap<Integer, Double>());
		}
		for (int i = 0; i < ret.n; i++) {
			if (alive(i)) {
				ArrayList<Integer> ns = new ArrayList<Integer>(neighbors(i));
				ns.add(i);
				for (int n : ns) {
					EdgeCost ec = superEdgeCost(i, n);
					if (ec.goodness != 0.0) {
						superedges.get(i).put(n, ec.goodness);
					}
				}
			}
		}
		// remove all edges
		for (int i = 0; i < ret.n; i++) {
			ArrayList<Integer> remove = new ArrayList<Integer>();
			for (SimpleEdge se : ret.getEdges(i)) {
				remove.add(se.to);
			}
			for (int to : remove)
				ret.removeEdge(i, to);

		}

		for (int i = 0; i < ret.n; i++) {
			int supernode = this.nodeGroup[i];
			for (Entry<Integer, Double> ent : superedges.get(supernode)
					.entrySet()) {
				int superto = ent.getKey();
				for (int to : groups.get(superto)) {
					if (to == i)
						continue;
					VisualEdge fe = new VisualEdge(sg.getVisualNode(i), sg
							.getVisualNode(to), ent.getValue(), false,
							"uncompressed");

					ret.addEdge(i, to, fe);
				}
			}
		}
		return ret;
	}

	public SimpleVisualGraph getSimpleGraph() {
		return sg;
	}

}
