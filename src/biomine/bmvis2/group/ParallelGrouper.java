package biomine.bmvis2.group;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualGroupNodeAutoEdges;
import biomine.bmvis2.VisualNode;

public class ParallelGrouper extends Grouper {
	public static String NAME = "Parallel node grouper";
	public static String SIMPLE_NAME = "By structure (parallel nodes)";

	/**
	 * Development debug printing method.
	 * 
	 * @param s
	 */
	private void d(String s) {
		// System.out.println("D: " + s);
	}

	/**
	 * General debug printout.
	 * 
	 * @param s
	 */
	private void p(String s) {
		System.err.println("Parallel grouper: " + s);
	}

	class ParallelPair {
		ParallelPair(VisualNode f, VisualNode t) {
			if (f.hashCode() < t.hashCode()) {
				from = (f);
				to = (t);
			} else {
				from = (t);
				to = (f);
			}
		}

		private VisualNode from;
		private VisualNode to;

		public boolean equals(Object o) {
			if (o instanceof ParallelPair) {
				ParallelPair p = (ParallelPair) o;
				return p.from == from && p.to == to;
			}
			return false;
		}

		public int hashCode() {
			return (from.hashCode() - 1) * (to.hashCode() - 1);
		}

		public VisualNode getFrom() {
			return from;
		}

		public VisualNode getTo() {
			return to;
		}
	}

	@Override
	public String makeGroups(VisualGroupNode group) {
		long startedAt = System.currentTimeMillis();
		int createdGroups = 0;
		HashMap<ParallelPair, HashSet<VisualNode>> sets = new HashMap<ParallelPair, HashSet<VisualNode>>();
		double prog = 0;
		for (VisualNode n : group.getChildren()) {
			prog += 1.0 / group.getChildren().size();
			setProgress(prog);
			int i = 0;
			d("node:" + n);
			VisualNode[] neighbors = new VisualNode[3];
			for (VisualEdge e : n.getEdges()) {
				if (i > 2)
					break;
				VisualNode no = e.getOther(n);

				if (no instanceof VisualGroupNode) {
					continue;
				}
				neighbors[i] = no;
				d("l: " + neighbors[i]);
				if (i >= 1) {
					if (neighbors[i] == neighbors[i - 1]) {
						neighbors[i] = null;
						i--;
					}
				}
				i++;
			}
			if (neighbors[2] != null)
				continue;
			d("" + neighbors[0]);
			d("" + neighbors[1]);
			if (neighbors[0] == null || neighbors[1] == null
					|| neighbors[0] == neighbors[1])
				continue;

			ParallelPair p = new ParallelPair(neighbors[0], neighbors[1]);
			HashSet<VisualNode> pairs = sets.get(p);
			if (pairs == null) {
				pairs = new HashSet<VisualNode>();
				sets.put(p, pairs);
			}
			pairs.add(n);

		}
		ArrayList<Entry<ParallelPair, HashSet<VisualNode>>> setarr;
		setarr = new ArrayList<Entry<ParallelPair, HashSet<VisualNode>>>();
		setarr.addAll(sets.entrySet());
		Comparator<Entry<ParallelPair, HashSet<VisualNode>>> comp;
		comp = new Comparator<Entry<ParallelPair, HashSet<VisualNode>>>() {
			public int compare(Entry<ParallelPair, HashSet<VisualNode>> a,
					Entry<ParallelPair, HashSet<VisualNode>> b) {

				return b.getValue().size() - a.getValue().size();
			}
		};
		Collections.sort(setarr, comp);
		HashSet<VisualNode> used = new HashSet<VisualNode>();
		for (Entry<ParallelPair, HashSet<VisualNode>> ent : setarr) {
			ParallelPair pp = ent.getKey();
			d("" + ent.getValue().size());
			if (ent.getValue().size() > 1) {
				if (used.contains(pp.getFrom()) || used.contains(pp.getTo())) {
					continue;
				}
				VisualGroupNodeAutoEdges vgnae = new VisualGroupNodeAutoEdges(
						group);
				vgnae.setName("Parallel");
				for (VisualNode n : ent.getValue()) {
					used.add(n);
					n.setParent(vgnae);
				}
				createdGroups++;
			} else {
				// no-op
			}
		}
		p(createdGroups + " groups created.");
		p("grouping took " + (System.currentTimeMillis() - startedAt) + " ms.");
		return null;
	}

	@Override
	public boolean settingsDialog(Component parent, VisualGroupNode group) {
		// TODO Auto-generated method stub
		return true;
	}

}
