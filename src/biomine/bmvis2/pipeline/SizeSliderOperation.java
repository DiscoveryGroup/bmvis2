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

package biomine.bmvis2.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.simple.JSONObject;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualNode;

public class SizeSliderOperation implements GraphOperation{
	int targetSize=0;
	public SizeSliderOperation(){
		targetSize=10000;
	}
	@Override
	public void doOperation(VisualGraph g) throws GraphOperationException {
		System.out.println("sizeSlider.doOperation");
		updateSizeSlider(g);
	}


	@Override
	public JComponent getSettingsComponent(final SettingsChangeCallback v,
			final VisualGraph graph) {
		final JSlider sizeSlider;
		sizeSlider = new JSlider(0, graph.getAllNodes().size() + 10);
		sizeSlider.setValue(graph.getNodes().size());
		
		sizeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				targetSize = sizeSlider.getValue();
				v.settingsChanged(false);
			}
		});
		
		return sizeSlider;
	}

	@Override
	public String getTitle() {
		return "Size slider";
	}

	@Override
	public String getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateSizeSlider(VisualGraph visualGraph) {
		int minCount = targetSize;
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
		System.out.println("openGroups.size() = "+openGroups.size());
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
				System.out.println("closing "+toClose);
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
				System.out.println("opening "+toOpen);
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

	@Override
	public void fromJSON(JSONObject o) throws Exception {
		Number n = (Number) o.get("target");
		this.targetSize = n.intValue();
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		ret.put("target", targetSize);
		return ret;
	}

}
