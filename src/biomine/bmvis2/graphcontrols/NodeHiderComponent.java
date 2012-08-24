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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
/**
 * OBSOLETE
 * @author alhartik
 *
 */
public class NodeHiderComponent extends JPanel{

	private VisualGraph visualGraph;

	JSlider hideSlider;

	JComboBox hiders;

	Box hiderComponent=new Box(BoxLayout.X_AXIS);

	NodeHider hider;

	public NodeHider getHider() {
		return hider;
	}

	public void setHider(NodeHider h){
		hider = h;
		hiderComponent.removeAll();
		Component comp = hider.getComponent(visualGraph);
		if(comp!=null)
			hiderComponent.add(comp);
		updateHidden();
	}
	public NodeHiderComponent(VisualGraph vg){
		visualGraph = vg;


		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=1;
		c.weightx=1;
		c.fill=c.HORIZONTAL;

		this.add(new JLabel("Hide uninteresting nodes by:"),c);

		final String[] hiderNames = {
				"Best path reliability (path to all nodes)",
				"Best path reliability (path to two closest nodes)" };



		final NodeHider[] hiderImpl = {
				new BestPathHider()
		};


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
		add(hiders,c);

		c.gridy++;
		add(new JLabel("Number of shown nodes"),c);
		hideSlider = new JSlider(0, vg.getAllNodes().size() + 10);
		hideSlider.setValue(vg.getAllNodes().size());

		c.gridy++;
		this.add(hideSlider,c);
		hideSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateHidden();

			}
		});
		c.gridy++;
		add(hiderComponent,c);

		setHider(hiderImpl[hiders.getSelectedIndex()]);

		this.setBorder(BorderFactory.createEtchedBorder());
	}

	int oldcount =-1;
	boolean updating= false;
	public void updateHidden() {
		if(updating)return;
		updating=true;
		Collection<VisualNode> nodes =visualGraph.getRootNode().getDescendants();
		int count = nodes.size();
		System.out.println("val = "+hideSlider.getValue());
		if(oldcount!=-1){
			if(oldcount!=count)
				hideSlider.setValue(hideSlider.getValue()+count-oldcount);
		}
		System.out.println("val = "+hideSlider.getValue()+" count = "+count+" oldcount = "+oldcount);
		oldcount = count;
		hideSlider.setMaximum(count);

		hider.hideNodes(visualGraph, hideSlider.getValue());
		updating=false;
	}
}
