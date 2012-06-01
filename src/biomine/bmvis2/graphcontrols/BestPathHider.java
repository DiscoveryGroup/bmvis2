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
