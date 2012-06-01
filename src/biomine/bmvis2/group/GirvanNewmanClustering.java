package biomine.bmvis2.group;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualGroupNodeAutoEdges;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.pipeline.SettingsChangeCallback;

/**
 * Implements Girwan newman clustering.
 * see http://en.wikipedia.org/wiki/Girvan-Newman_algorithm
 * @author alhartik
 *
 */
public class GirvanNewmanClustering extends Grouper {
	public static String NAME = "Girvan-Newman clustering";
	public static String SIMPLE_NAME = "By hierarchy (Girvan-Newman)";
	private int maxNodeCount = 5;

	HashSet<VisualEdge> bannedEdges;

	/**
	 * @param nodes
	 * @return
	 */
	HashSet<VisualNode> getFirstComponent(Set<VisualNode> nodes) {
		HashSet<VisualNode> visited = new HashSet<VisualNode>();
		LinkedList<VisualNode> q = new LinkedList<VisualNode>();
		// add starting node

		for (VisualNode n : nodes) {
			q.add(n);
			break;
		}

		while (q.isEmpty() == false) {
			VisualNode n = q.poll();
			if (visited.contains(n))
				continue;
			visited.add(n);
			for (VisualEdge e : n.getEdges()) {
				if (bannedEdges.contains(e))
					continue;
				VisualNode o = e.getOther(n);
				if (nodes.contains(o)) {
					q.add(o);
				}
			}
		}
		return visited;
	}

	HashSet<VisualNode> getFirstComponent(VisualGroupNode vgn) {
		HashSet<VisualNode> visited = new HashSet<VisualNode>();
		LinkedList<VisualNode> q = new LinkedList<VisualNode>();
		// add starting node

		for (VisualNode n : vgn.getChildren()) {
			q.add(n);
			break;
		}

		while (!q.isEmpty()) {
			VisualNode n = q.poll();
			if (visited.contains(n))
				continue;
			visited.add(n);
			for (VisualEdge e : n.getEdges()) {
				if (bannedEdges.contains(e))
					continue;
				VisualNode o = e.getOther(n);
				if (o.getParent() == vgn) {
					q.add(o);
				}
			}
		}
		return visited;
	}

	boolean isConnected(VisualGroupNode vgn) {
		return getFirstComponent(vgn).size() == vgn.getChildren().size();
	}
	VisualGroupNode inputGroup;
	public String makeGroups(VisualGroupNode n) {
		inputGroup = n;
		bMakeGroups(n);
		return null;
	}

	public Set<VisualGroupNode> bMakeGroups(VisualGroupNode n) {
		if (n.getChildren().size() <= maxNodeCount)
			return new HashSet<VisualGroupNode>();
		
		bannedEdges = new HashSet<VisualEdge>();
		while (isConnected(n)) {
			double best = 0;
			VisualEdge bestE = null;
			EdgeBetweenness edgeBetweenness = new EdgeBetweenness(n,
					bannedEdges);

			HashMap<VisualEdge, Double> betw = edgeBetweenness
					.getEdgeBetweenness();
			for (VisualEdge e : betw.keySet()) {
				double be = betw.get(e);
				if (be > best) {
					best = be;
					bestE = e;
				}
			}
			
			// System.out.println(bannedEdges.size() + " edges removed");
			bannedEdges.add(bestE);
			
			if(bannedEdges.size()>5){
			//	return new HashSet<VisualGroupNode>();
			}
		}
		Set<VisualNode> first = getFirstComponent(n);

		HashSet<VisualNode> second = new HashSet<VisualNode>();
		for (VisualNode vn : n.getChildren()) {
			if (!first.contains(vn)) {
				second.add(vn);
			}
		}
		VisualGroupNodeAutoEdges firstGroup = new VisualGroupNodeAutoEdges(n);
		for (VisualNode vn : first)
			vn.setParent(firstGroup);



		VisualGroupNodeAutoEdges secondGroup = new VisualGroupNodeAutoEdges(n);
		
		for (VisualNode vn : second)
			vn.setParent(secondGroup);		
		VisualGraph graph = n.getGraph();

	//	return new HashSet<VisualGroupNode>();
		
		Set<VisualGroupNode> bf = bMakeGroups(firstGroup);
		Set<VisualGroupNode> bs = bMakeGroups(secondGroup);
		
		bs.addAll(bf);
		
		{
			for(VisualGroupNode g:bs){
				g.setParent(inputGroup);
				if(g.getChildren().size()<=1)
					graph.destroyGroup(g);
			}
			
			if(secondGroup.getChildren().size()<=1)
			graph.destroyGroup(secondGroup);
			if(firstGroup.getChildren().size()<=1)
			graph.destroyGroup(firstGroup);
		}
		
		
		HashSet<VisualGroupNode> ret =  new HashSet<VisualGroupNode>();
		ret.add(firstGroup);
		ret.add(secondGroup);
		return ret;
	}

	@Override

	public boolean settingsDialog(Component parent,VisualGroupNode group) {
		JSlider maxSlide = new JSlider(0, 100);
		maxSlide.setValue(this.maxNodeCount);
		maxSlide.setPaintLabels(true);
		maxSlide.setMajorTickSpacing(15);
		maxSlide.setMinorTickSpacing(1);
		maxSlide.setPaintTicks(true);		
		JSlider maxCost = new JSlider(0, 100);
		maxCost.setValue(this.maxNodeCount);
		maxCost.setPaintLabels(true);
		maxCost.setMajorTickSpacing(15);
		maxCost.setMinorTickSpacing(1);
		maxCost.setPaintTicks(true);

		Object[] params = { maxSlide, "Maximum node count in group" };

		int n = JOptionPane.showConfirmDialog(parent, params,
				"Betweenness clustering", JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			this.maxNodeCount = maxSlide.getValue();
			return true;
		}

		return true;
	}

    public JComponent getSettingsComponent(final SettingsChangeCallback changeCallback, final VisualGroupNode groupNode) {
        JPanel ret = new JPanel();
        ret.setLayout(new BoxLayout(ret, BoxLayout.PAGE_AXIS));

        ret.add(new JLabel("<html>Drag slider to modify<br>maximum group size</html>"));

        final int nodeCount = groupNode.getDescendants().size();
        final JSlider maxNodeCountSlide = new JSlider(1, nodeCount);
        maxNodeCountSlide.setValue(this.maxNodeCount);
        maxNodeCountSlide.setPaintLabels(true);

        if (nodeCount < 100) {
            maxNodeCountSlide.setMajorTickSpacing(10);
            maxNodeCountSlide.setMinorTickSpacing(2);
        } else {
            maxNodeCountSlide.setMajorTickSpacing(25);
            maxNodeCountSlide.setMinorTickSpacing(5);
        }

        maxNodeCountSlide.setPaintTicks(true);

        final JButton recompute = new JButton("Recompute");
        recompute.setEnabled(false);

        ret.add(maxNodeCountSlide, "Maximum node count in a group");

        if (nodeCount > 100)
            ret.add(recompute);

        maxNodeCountSlide.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                if (maxNodeCountSlide.getValue() != maxNodeCount)
                    recompute.setEnabled(true);
                else
                    recompute.setEnabled(false);

                if (nodeCount <= 100) {
                    GirvanNewmanClustering.this.maxNodeCount = maxNodeCountSlide.getValue();
                    changeCallback.settingsChanged(true);
                }
            }
        });

        recompute.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                GirvanNewmanClustering.this.maxNodeCount = maxNodeCountSlide.getValue();
                changeCallback.settingsChanged(true);
            }
        });

        return ret;
    }

    public String getByName() {
        return "clustering";
    }


}
