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

package biomine.bmvis2.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import biomine.bmvis2.Logging;
import biomine.bmvis2.color.ColorPalette;

public class CollapsePane extends JPanel{
	private JButton expandButton;
	private JComponent comp;
	
	public boolean isCompVisible(){
		return Arrays.asList(this.getComponents()).contains(comp);
	}
	
	public void setComp(JComponent comp){
		boolean v = isCompVisible();
		if(v)setCompVisible(false);
		this.comp = comp;
		if(v)setCompVisible(true);
	}
	
	@Override
	public void setToolTipText(String text) {
		super.setToolTipText(text);
		this.comp.setToolTipText(text);
		this.expandButton.setToolTipText(text);
		System.err.println(text);
	}
	
	private void init(JComponent titleComp,JComponent comp){
		this.comp = comp;
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx=1;
		c.weighty=0;
		
		c.fill=c.HORIZONTAL;
		
		JPanel titlePane = new JPanel();
		
		titlePane.setLayout(new GridBagLayout());
		
		titlePane.add(titleComp,c);
		c.gridx=1;

        expandButton = new JButton();
		titlePane.add(expandButton);
		
		c.gridx=0;
		this.add(titlePane, c);
		
		titlePane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		Color b = this.getBackground();
		
		titlePane.setBackground(ColorPalette.SHINY_SILVER);

		expandButton.setMargin(new Insets(0,0,0,0));
		expandButton.setAction(new AbstractAction("\u21d3") {
			public void actionPerformed(ActionEvent arg0) {
				setCompVisible(true);
			}
		});
		setCompVisible(true);

	}
	
	public CollapsePane(JComponent titleComp,JComponent comp){
		init(titleComp,comp);
	}

	public CollapsePane(String title,JComponent comp){
		init(new JLabel(title),comp);
	}
	
	public void setCompVisible(boolean vis){
		if(comp==null)return;
        Logging.debug("ui", "setCompVisible(" + vis + ")");
		if(vis){
			Component[] children=this.getComponents();
			if(Arrays.asList(children).contains(comp)){
				return;
			}
			GridBagConstraints c = new GridBagConstraints();
			c.fill= c.HORIZONTAL;
			c.gridy=1;
			c.weightx=1;
			c.weighty=0;
			c.insets = new Insets(0,15, 0, 0);
			this.add(comp,c);
			expandButton.setAction(new AbstractAction("\u21d1") {
				
				public void actionPerformed(ActionEvent arg0) {
					setCompVisible(false);
				}
			});
		}else
		{
			this.remove(comp);
			expandButton.setAction(new AbstractAction("\u21d3") {
				
				public void actionPerformed(ActionEvent arg0) {
					setCompVisible(true);
				}
			});
		}
		this.updateUI();
	}
}
