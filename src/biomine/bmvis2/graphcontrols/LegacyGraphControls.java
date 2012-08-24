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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.*;

/**
 * Legacy, to be removed.
 * @author alhartik
 *
 */
public class LegacyGraphControls extends JPanel implements GraphObserver {
	JCheckBox sizeSliderEnable;
	JSlider sizeSlider;
	VisualGraph visualGraph;
	NodeHiderComponent nodeHiderComp;
	EdgeHiderComponent edgeHiderComp;

	public LegacyGraphControls(VisualGraph vg) {
		nodeHiderComp = new NodeHiderComponent(vg);
		edgeHiderComp = new EdgeHiderComponent(vg);
		visualGraph = vg;

		vg.addObserver(this);

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		sizeSliderEnable = new JCheckBox("Enable size slider");
		sizeSliderEnable.setSelected(false);
		sizeSlider = new JSlider(0, vg.getAllNodes().size() + 10);
		sizeSlider.setValue(vg.getNodes().size());
		sizeSliderEnable.setAlignmentX(CENTER_ALIGNMENT);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.fill = c.HORIZONTAL;

		add(sizeSliderEnable, c);
		c.gridy++;
		add(sizeSlider, c);

		sizeSlider.setEnabled(false);
		sizeSliderEnable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sizeSlider.setEnabled(sizeSliderEnable.isSelected());
				if (sizeSliderEnable.isSelected() == false)
					sizeSlider.setValue(sizeSlider.getMaximum());
			}
		});

		sizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateSizeSlider();
			}
		});
		c.gridy++;
		this.add(nodeHiderComp, c);
		c.gridy++;
		this.add(edgeHiderComp, c);

		this.setMaximumSize(new Dimension(Short.MAX_VALUE, this
				.getPreferredSize().height));
		this.setMinimumSize(new Dimension(1, this.getPreferredSize().height));
	}

	public void updateSizeSlider() {
		int minCount = sizeSlider.getValue();
		HashSet<VisualGroupNode> closedGroups = new HashSet<VisualGroupNode>();
		HashSet<VisualGroupNode> openGroups = new HashSet<VisualGroupNode>();
		for (VisualNode n : visualGraph.getNodes()) {
			if (n instanceof VisualGroupNode) {
				closedGroups.add((VisualGroupNode) n);
			}
			VisualGroupNode p = n.getParent();
			if (p != null && p.getParent() != null) {

				openGroups.add(p);
				openGroups.remove(p.getParent());
			}
		}
		int curCount = visualGraph.getNodes().size();
		final int order = 1;
		final double depthMult = 0.01;
		if (curCount > minCount) {
			// close as many open groups as possible while not going
			// under minCount nodes

			// start with nodes with most depth
			ArrayList<VisualGroupNode> groups = new ArrayList<VisualGroupNode>(
					openGroups);
			Collections.sort(groups, new Comparator<VisualGroupNode>() {
				public int compare(VisualGroupNode o1, VisualGroupNode o2) {
					return Double.compare((o2.getDepth() - o1.getDepth())
							* depthMult
					// + hider.getNodeGoodness(o1)
							// - hider.getNodeGoodness(o2)
							, 0)
							* order;
				}
			});

			for (VisualGroupNode toClose : groups) {
				int s = toClose.getChildren().size();
				if (curCount - s >= minCount) {
					toClose.setOpen(false);
					curCount -= s;
				}

			}
		} else {

			ArrayList<VisualGroupNode> groups = new ArrayList<VisualGroupNode>(
					closedGroups);
			Collections.sort(groups, new Comparator<VisualGroupNode>() {
				public int compare(VisualGroupNode o1, VisualGroupNode o2) {
					return Double.compare((o2.getDepth() - o1.getDepth())
							* depthMult
					// + hider.getNodeGoodness(o1)
							// - hider.getNodeGoodness(o2),
							, 0)

							* order * -1;
				}
			});
			for (VisualGroupNode toOpen : groups) {
				int s = toOpen.getChildren().size();

				toOpen.setOpen(true);
				for (VisualNode n : toOpen.getChildren())
					if (n instanceof VisualGroupNode)
						((VisualGroupNode) n).setOpen(false);
				curCount += s;

				if (curCount + s >= minCount)
					break;
			}

		}
	}

	public void updateHidden() {
		visualGraph.removeObserver(this);

		nodeHiderComp.updateHidden();
		edgeHiderComp.updateHidden();
		visualGraph.addObserver(this);
		
	}
	
	//update only once
	boolean updating = false;
	public void graphStructureChanged(VisualGraph g) {
		if(updating)return;
		updating=true;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateHidden();
				updating=false;
			}
		});
	}

	public void visibleNodesChanged(VisualGraph g) {
	}
	
	public void colorsChanged(VisualGraph g) {
	}

    public void zoomRequested(VisualGraph g, Collection<LayoutItem> items) {
    }

	public void selectionChanged(VisualGraph g) {
	}

	public void pointsOfInterestsChanged(VisualGraph g) {
	}

}
