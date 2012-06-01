package biomine.bmvis2.group;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JSlider;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualGroupNodeAutoEdges;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.pipeline.SettingsChangeCallback;

public class EdgeTripleGrouper extends Grouper {
	public static String NAME = "Node-edge-node triples (edges)";
	public static String SIMPLE_NAME = "By edge type";
    private List<EdgeTriple> triples;
	private int tripleCountMinThreshold = 8;

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
		System.err.println("Node-edge-node grouper: " + s);
	}

	/**
	 * EdgeTriple provides a convenient representation of an edge triple.
	 * (nodetype, edgetype, nodetype).
	 */
	public class EdgeTriple implements Comparable {
		String fromType;
		String type;
		String toType;
		Integer numberOf;

		EdgeTriple(VisualEdge e) {
			this.fromType = e.getFrom().getType();
			this.toType = e.getTo().getType();
			this.type = e.getType();
		}

		public String toString() {
			return this.fromType + " " + this.type + " " + this.toType;
		}
		
		public int hashCode() {
			return this.toString().hashCode();
		}

		public boolean equals(Object o) {
			if (o.getClass().equals(this.getClass()))
				return this.toString().equals(o.toString());
			else
				return super.equals(o);
		}

		public int compareTo(Object o) {
			return this.numberOf - ((EdgeTriple) o).numberOf;
		}
	}

	private Set<VisualEdge> getEdgesRecursively(VisualGroupNode group) {
		Set<VisualEdge> edges = new HashSet<VisualEdge>();

		for (VisualNode n : group.getChildren())
			edges.addAll(n.getEdges());

		return edges;
	}

	private Set<EdgeTriple> getUniqueTriples(Set<VisualEdge> allEdges) {
		Set<EdgeTriple> uniqueTriples = new HashSet<EdgeTriple>();

		for (VisualEdge e : allEdges) {
			EdgeTriple et = new EdgeTriple(e);
			// Prune groups out
			if (!et.fromType.equalsIgnoreCase("group")
					&& !et.toType.equalsIgnoreCase("group"))
				uniqueTriples.add(et);
		}

		return uniqueTriples;
	}

	private List<EdgeTriple> countAndSortUniqueTriples(
			Set<VisualEdge> allEdges, Set<EdgeTriple> uniqueTriples) {
		List<EdgeTriple> triples;
		Map<EdgeTriple, Integer> numOfTypes = new HashMap<EdgeTriple, Integer>();

		for (VisualEdge e : allEdges) {
			EdgeTriple et = new EdgeTriple(e);
			if (!numOfTypes.containsKey(new EdgeTriple(e)))
				numOfTypes.put(et, 0);
			numOfTypes.put(et, numOfTypes.get(et) + 1);
		}

		triples = new ArrayList<EdgeTriple>();
		for (EdgeTriple ut : uniqueTriples) {
			ut.numberOf = numOfTypes.get(ut);
			triples.add(ut);
			// d(ut.toString() + ": " + numOfTypes.get(ut));
		}
		Collections.sort(triples);
		Collections.reverse(triples);

		return triples;
	}

	private List<VisualNode> getNodesInDescendingDegreeOrder(
			Collection<VisualNode> allNodes) {
		List<VisualNode> nodes = new ArrayList<VisualNode>(allNodes);

		Collections.sort(nodes, Collections
				.reverseOrder(new Comparator<VisualNode>() {
					public int compare(VisualNode arg0, VisualNode arg1) {
						return arg0.getDegree() - arg1.getDegree();
					}
				}));
		return nodes;
	}

	private void updateTripleCounts(VisualGroupNode group) {
        Set<VisualEdge> edges = this.getEdgesRecursively(group);
        Set<EdgeTriple> uniqueTriples = this.getUniqueTriples(edges);

		this.triples = this.countAndSortUniqueTriples(edges,
                uniqueTriples);
	}

	/*
	 * Procedure goes as follows. Input: group node. Create new groups (use
	 * VisualGroupNodeAutoEdges if you want it to automatically create
	 * relatively sane edges). Add corresponding nodes to groups
	 * (setParent/addChild).
	 */

	/**
	 * @param group
	 *            Input group. This compressor doesn't group groups.
	 */
	public String makeGroups(VisualGroupNode group) {
		long startedAt = System.currentTimeMillis();
		this.updateTripleCounts(group);
		Set<VisualEdge> edges = this.getEdgesRecursively(group);
		Set<EdgeTriple> uniqueTriples = this.getUniqueTriples(edges);

		List<EdgeTriple> triples = this.countAndSortUniqueTriples(edges,
				uniqueTriples);

		p("minimum count for common edge reduction: "
				+ this.tripleCountMinThreshold);

        Set<EdgeTriple> reducables = new HashSet<EdgeTriple>();
		for (EdgeTriple et : triples)
			if (et.numberOf >= this.tripleCountMinThreshold)
				reducables.add(et);

		// Actual grouping
		/*
		 * 1. Iterate all nodes in the order of degree. 2. Combine node with its
		 * neighbors if one of the triples in triple list matches (take the one
		 * with highest triple count). 3. Add the neighboring nodes to a
		 * skiplist so that they are not combined again.
		 */
		d("Reducing the following:");
		List<VisualNode> nodes = this.getNodesInDescendingDegreeOrder(group
				.getChildren());
		Set<VisualNode> skippables = new HashSet<VisualNode>(); //
		Map<VisualNode, VisualGroupNodeAutoEdges> groupNodes = new HashMap<VisualNode, VisualGroupNodeAutoEdges>();
		Set<VisualNode> addedNodes = new HashSet<VisualNode>();

		long iters = 0;
		for (VisualNode vn : nodes) {
			d("Handling node " + vn);

			if (skippables.contains(vn)) {
				d(" -> in skippable, skipping...");
				continue;
			}

			for (EdgeTriple et : this.triples) {
				if (et.numberOf < this.tripleCountMinThreshold)
					break;
				// High degree nodes, common edge types; only create group nodes
				// of single edge type
				if (vn.getParent() != group)
					continue;
				List<VisualEdge> nodeEdges = new ArrayList(vn.getEdges());
				for (VisualEdge ve : nodeEdges) {
					iters++;
					EdgeTriple t = new EdgeTriple(ve);
					if (!t.equals(et))
						continue;
					d("  - edge " + ve + " / " + et);

					VisualGroupNodeAutoEdges groupNode;
					if (groupNodes.containsKey(vn))
						groupNode = groupNodes.get(vn);
					else {
						groupNode = new VisualGroupNodeAutoEdges(group,
								"EdgetypeGroup" + " " + et.toString() + " "
										+ vn.getName());

						group.addChild(groupNode);
						groupNodes.put(vn, groupNode);
						// groupNode.setParent(group);
						group.addChild(groupNode);
					}

					d("  * adding " + ve.getFrom() + " to " + groupNode);
					if (!addedNodes.contains(ve.getFrom()))
						groupNode.addChild(ve.getFrom());
					addedNodes.add(ve.getFrom());

					d("  * adding " + ve.getTo() + " to " + groupNode);
					if (!addedNodes.contains(ve.getTo()))
						groupNode.addChild(ve.getTo());
					addedNodes.add(ve.getTo());

					if (ve.getFrom().equals(vn)) {
						d("  + adding " + ve.getTo() + " to skippables");
						skippables.add(ve.getTo());
					} else if (ve.getTo().equals(vn)) {
						d("  + adding " + ve.getFrom() + " to skippables");
						skippables.add(ve.getFrom());
					}
				}
			}
		}

		// Close all groups
		for (VisualGroupNode gn : groupNodes.values())
			gn.setOpen(true);
		p(iters + " edges considered.");
		p("Compression took " + (System.currentTimeMillis() - startedAt)
				+ " ms.");
		return iters + " edges considered, " + groupNodes.size() + " groups created.";
	}

	@Override
	public boolean settingsDialog(Component parent, VisualGroupNode group) {
		this.updateTripleCounts(group);

		JComboBox selector = new JComboBox();
		p("triples.size() = "+this.triples.size());
		for (EdgeTriple et : this.triples){
			p(""+et.numberOf);
			selector.addItem(et.numberOf);
		}

		Object[] params = { selector, "Minimum edge type count" };

		int n = JOptionPane.showConfirmDialog(parent, params,
				"Common edge type compression", JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			this.tripleCountMinThreshold = (Integer) selector.getSelectedItem();

			return true;
		}

		return true;
	}
}