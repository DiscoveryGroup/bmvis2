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

package biomine.bmvis2.graphcontrols;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.color.NodeGraderColoring;

public class BestPathHider implements NodeHider {
	BestPathGrader grader;

	public BestPathHider() {
		grader = new BestPathGrader();
	}

	public Component getComponent(VisualGraph vg) {
		final VisualGraph visualGraph = vg;

		JPanel ret = new JPanel();
		
		JButton useMatchingColoring = new JButton();
		ret.add(useMatchingColoring);
		
		useMatchingColoring.setAction(new AbstractAction(
				"Use matching coloring") {
			public void actionPerformed(ActionEvent arg0) {
				visualGraph.setNodeColoring(new NodeGraderColoring(grader));
			}
		});

		useMatchingColoring.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		

		return ret;
	}

	public void hideNodes(VisualGraph vg, int target) {
		HashSet<VisualNode> hidden = new HashSet<VisualNode>();

		int maxShow = target;
		ArrayList<VisualNode> arr = new ArrayList<VisualNode>(vg
				.getRootNode().getDescendants());
		// ArrayList<VisualNode> arr = new
		// ArrayList<VisualNode>(vg.getAllNodes());
		if (grader == null || arr.size() <= maxShow) {
			vg.setHiddenNodes(hidden);
			return;
		}

		Collections.sort(arr, new Comparator<VisualNode>() {
			public int compare(VisualNode o1, VisualNode o2) {
				return Double.compare(grader.getNodeGoodness(o2), grader
						.getNodeGoodness(o1));
			}
		});

		for (int i = maxShow; i < arr.size(); i++) {
			hidden.add(arr.get(i));
		}
		vg.setHiddenNodes(hidden);
	}

}
