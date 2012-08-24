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

package biomine.bmvis2.group;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualGroupNodeAutoEdges;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.pipeline.SettingsChangeCallback;

/**
 * Nodetype-edgetype-nodetype grouper with manually set types.
 * @author alhartik
 *
 */
public class ManualEdgeTripleGrouper extends Grouper {
	public static String NAME = "Manual Node-edge-node triples (edges)";
	public static String SIMPLE_NAME = "By edge type (manually defined)";
    private Set<VisualEdge> edges;
	private Set<EdgeTriple> uniqueTriples;
	private ArrayList<EdgeTriple> triples = new ArrayList<EdgeTriple>();
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
	public class EdgeTriple{
		String fromType;
		String type;
		String toType;
        double threshold;

		EdgeTriple(String f,String et,String t,double thld) {
			fromType = f;
			type=et;
			toType = t;
            threshold=thld;
		}
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


        Set<EdgeTriple> reducables = new HashSet<EdgeTriple>();
		for (EdgeTriple et : triples)
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
            
            double bestProb = 0 ; 
            VisualEdge bestE=null;
			for (EdgeTriple et : this.triples) {
				// High degree nodes, common edge types; only create group nodes
				// of single edge type
				if (vn.getParent() != group)
					continue;
				List<VisualEdge> nodeEdges = new ArrayList(vn.getEdges());
				for (VisualEdge ve : nodeEdges) {
                    if(skippables.contains(ve.getOther(vn)))
                        continue;
					iters++;
					EdgeTriple t = new EdgeTriple(ve);
					if (!t.equals(et))
						continue;
                    double p = ve.getGoodness();
                    if(p<et.threshold)continue;
                    if(p>bestProb){
                        bestProb = p;
                        bestE = ve;
                    }
                }
                VisualEdge ve = bestE;
                if(ve!=null){
					d("  - edge " + ve + " / " + et);

					VisualGroupNodeAutoEdges groupNode;
					if (groupNodes.containsKey(vn))
						groupNode = groupNodes.get(vn);
					else {
						groupNode = new VisualGroupNodeAutoEdges(group,
								//"EdgetypeGroup" + " " + et.toString() + " "
                                    ve.getFrom()+" "+et.type+" "+ve.getTo()
                                        );

						group.addChild(groupNode);
						groupNodes.put(vn, groupNode);
						// groupNode.setParent(group);
						//group.addChild(groupNode);
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
                    skippables.add(vn);
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
	public JComponent getSettingsComponent(final SettingsChangeCallback changeCallback,VisualGroupNode groupNode){
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());
		final JTable table = new JTable();
		
		table.setModel(new AbstractTableModel() {

			public Object getValueAt(int y,int x) {
				if(y>=triples.size())return null;
				EdgeTriple et = triples.get(y);
				assert(0<=x && x<3);
				if(x==0)return et.fromType;
				if(x==1)return et.type;
				if(x==2)return et.toType;
				if(x==3)return ""+et.threshold;
				return null;
			}
			
			public int getRowCount() {
				return triples.size();
			}
			
			public int getColumnCount() {
				return 4;
			}
		});
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=1;
		c.fill=c.BOTH;
		ret.add(table,c);
		c.gridy++;
		ret.add(new JButton(new AbstractAction("Add triple") {
			
			public void actionPerformed(ActionEvent arg0) {
				JTextField from = new JTextField();
				JTextField edgeType = new JTextField();
				JTextField to = new JTextField();
                JSpinner thld = new JSpinner(new SpinnerNumberModel(0.8,0.0,1.0,0.05));
				Object[] params = { "From-type:",from,"Edgetype:",edgeType,"To-type:",to,"Threshold:",thld};
				int n = JOptionPane.showConfirmDialog(table, params,
						"Add new edge triple", JOptionPane.OK_CANCEL_OPTION);
				if (n == JOptionPane.OK_OPTION) {
                    double t = (Double)thld.getValue();
					triples.add(new EdgeTriple(from.getText(), 
                            edgeType.getText(), to.getText(),t));
					table.revalidate();
					changeCallback.settingsChanged(true);
				}
			}
		}
			
		));
		return ret;
	}
}
