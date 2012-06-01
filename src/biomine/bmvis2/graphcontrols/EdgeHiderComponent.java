package biomine.bmvis2.graphcontrols;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.VisualEdge;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;

public class EdgeHiderComponent extends JPanel {

	private VisualGraph visualGraph;

	JSlider hideSlider;

	JComboBox hiders;

	Box hiderComponent = new Box(BoxLayout.X_AXIS);

	EdgeHider hider;
	JCheckBox enable;

	public EdgeHider getHider() {
		return hider;
	}

	public void setHider(EdgeHider h) {
		hider = h;
		hiderComponent.removeAll();
		// Component comp = hider.getComponent(visualGraph);
		// if(comp!=null)
		// hiderComponent.add(comp);
		updateHidden();
	}

	public EdgeHiderComponent(VisualGraph vg) {
		visualGraph = vg;

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.fill = c.HORIZONTAL;

		

		enable = new JCheckBox("Enable edge hiding");
		this.add(enable,c);
		c.gridy++;
		
		this.add(new JLabel("Hide uninteresting nodes by:"), c);

		final String[] hiderNames = { "Path Simplification (PS)" };

		final EdgeHider[] hiderImpl = { new KappaEdgeHider() };

		hiders = new JComboBox(hiderNames);
		hiders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(hiders.getSelectedIndex());

				setHider(hiderImpl[hiders.getSelectedIndex()]);
			}
		});
		// graders.setFont(new
		// Font(graders.getFont().getName(),0,graders.getFont().getSize()-9));

		c.gridy++;
		add(hiders, c);

		c.gridy++;
		add(new JLabel("Number of shown edges"), c);
		hideSlider = new JSlider(0,10000);
		hideSlider.setValue(10000);

		c.gridy++;
		this.add(hideSlider, c);
		hideSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateHidden();

			}
		});
		c.gridy++;
		add(hiderComponent, c);

		setHider(hiderImpl[hiders.getSelectedIndex()]);

		this.setBorder(BorderFactory.createEtchedBorder());
	}

	int oldcount = -1;

	public void updateHidden() {
		if(enable.isSelected()==false){
			visualGraph.setHiddenEdges(Collections.EMPTY_SET);
			return;
		}
		HashSet<VisualEdge> edges = new HashSet<VisualEdge>();
		for(VisualNode n:visualGraph.getRootNode().getDescendants()){
			edges.addAll(n.getEdges());
		}
		
		
		int count = edges.size();
		if (oldcount != -1) {
			hideSlider.setValue(hideSlider.getValue() + count - oldcount);
		}
		oldcount = count;
		hider.hideEdges(visualGraph, hideSlider.getValue());
		hideSlider.setMaximum(count);
	}
}
