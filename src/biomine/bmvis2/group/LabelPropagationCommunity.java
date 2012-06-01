package biomine.bmvis2.group;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualGroupNodeAutoEdges;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.pipeline.SettingsChangeCallback;

/**
 * Near linear -time algorithm for community detection. See
 * http://pre.aps.org/abstract/PRE/v76/i3/e036106
 * @author alhartik
 *
 */
public class LabelPropagationCommunity extends Grouper {
	public static String NAME = "Label propagation community detection";
	public static String SIMPLE_NAME = "By community (Label Propagation)";

	private boolean subdivide = false;

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
		System.err.println("Label propagation community: " + s);
	}

	public static <T> void inc(HashMap<T, Integer> m, T t, Integer d) {
		Integer in = m.get(t);
		if (in == null)
			in = 0;
		m.put(t, in + d);
	}

	@Override
	public String makeGroups(VisualGroupNode group) {
		VisualGraph graph = group.getGraph();
		HashSet<VisualNode> nodes = new HashSet<VisualNode>(group.getChildren());
		ArrayList<VisualNode> nodeArr = new ArrayList<VisualNode>(nodes);
		HashMap<VisualNode, Integer> label = new HashMap<VisualNode, Integer>();

		int lab = 0;
		for (VisualNode n : nodes) {
			label.put(n, lab);
			lab++;
		}
		d("jee");
		int iterCount=0;
		labelLoop: while (true) {
			for (int i = 0; i < 2; i++) {
				setProgress(0.1 + i * 0.4);
				iterCount++;
				if(iterCount>10000){
					System.out.println("we seem to be stuck in loop");
					break labelLoop;
				}
				Collections.shuffle(nodeArr);
				for (VisualNode vn : nodeArr) {

					HashMap<Integer, Integer> ncount = new HashMap<Integer, Integer>();
					for (VisualNode nn : graph.getNodeNeighbors(vn)) {
						if (nodes.contains(nn))
							inc(ncount, label.get(nn), 1);
					}
					int bestLabel = 0;
					int best = 0;
					ArrayList<Entry<Integer, Integer>> entarr = new ArrayList<Entry<Integer, Integer>>(
							ncount.entrySet());
					Collections.shuffle(entarr);
					for (Entry<Integer, Integer> ent : entarr) {
						if (ent.getValue() > best) {
							bestLabel = ent.getKey();
							best = ent.getValue();
						}
					}

					if (i == 0) {
						Integer curC = ncount.get(label.get(vn));
						if (curC == null || curC < best) {
							label.put(vn, bestLabel);
						}
					} else {
						Integer curC = ncount.get(label.get(vn));
						if (curC == null || curC < best) {
							continue labelLoop;
						}
					}
				}
			
			}
			break;
		}

		HashMap<Integer, VisualGroupNode> labelGroups = new HashMap<Integer, VisualGroupNode>();
		int c = 0;
		for (VisualNode n : nodes) {
			c++;
			setProgress(c / ((double) nodes.size()));
			int nl = label.get(n);
			VisualGroupNode myGroup = labelGroups.get(nl);
			if (myGroup == null) {
				myGroup = new VisualGroupNodeAutoEdges(group);
				labelGroups.put(nl, myGroup);
			}
			myGroup.addChild(n);

		}

		if (subdivide && labelGroups.size() > 1) {
			for (VisualGroupNode ng : labelGroups.values()) {
				makeGroups(ng);
			}
		}
		if (labelGroups.size() == 1) {
			for (VisualGroupNode vgn : labelGroups.values()) {
				graph.destroyGroup(vgn);
			}
		}
		return null;
	}

	public boolean getSubdivide() {
		return subdivide;
	}

	public void setSubdivide(boolean subdivide) {
		this.subdivide = subdivide;
	}

	public boolean settingsDialog(Component parent, VisualGroupNode group) {
		JCheckBox subd = new JCheckBox("Subdivide groups");
		subd.setSelected(this.subdivide);
		Object[] params = { subd };

		int n = JOptionPane.showConfirmDialog(parent, params,
				"Communities by label propagation",
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			this.subdivide = subd.isSelected();
			return true;
		}

		return true;
	}

    public String getByName() {
        return "community";
    }

    public JComponent getSettingsComponent(final SettingsChangeCallback changeCallback, final VisualGroupNode n) {
        JPanel ret = new JPanel();
        final JCheckBox cBox = new JCheckBox("Subdivide groups");
        cBox.setSelected(this.subdivide);
        ret.add(cBox);

        cBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (cBox.isSelected() != subdivide) {
                    subdivide = cBox.isSelected();
                    changeCallback.settingsChanged(true);
                }
            }
        });

        return ret;
    }


}
