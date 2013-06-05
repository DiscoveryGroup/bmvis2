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

package discovery.compression.kdd2011.old;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.group.Grouper;

public abstract class Compressor extends Grouper{

	private double dev=0.1;
	public abstract CompressorCosts getStartingCosts();
	public abstract CompressorCosts getResultCosts();
	public abstract String makeGroups(VisualGroupNode group);
	
	protected CompressedGraph createCompressedGraph(VisualGroupNode group){
		CompressedGraph ret = new CompressedGraph(group,getDeviation());
		ret.setLabeledCost(isLabeled());
		ret.setK(getK());
		return ret;
	}
	private double k=1.0;
	
	public final double getK() {
		return k;
	}
	public final void setK(double k) {
		this.k = k;
	}
	public final void setDeviation(double deviation){
		dev=deviation;
	}
	public final double getDeviation(){
		return dev;
	}
	private boolean useAuto=true;
	public final void setUseAuto(boolean ua){
		useAuto=ua;
	}
	public boolean useAuto(){
		return useAuto;
	}
	private boolean labeled = false;
	public boolean isLabeled() {
		return labeled;
	}
	public void setLabeled(boolean labeled) {
		this.labeled = labeled;
	}
	public abstract SimpleVisualGraph getUncompressedGraph();
	public abstract SimpleVisualGraph getOriginalGraph();
		
	@Override
	public boolean settingsDialog(Component parent, VisualGroupNode group) {
		// TODO Auto-generated method stub
		// JCheckBox box = new JCheckBox();
		JCheckBox autobox = new JCheckBox();
		autobox.setSelected(useAuto);
		autobox.setText("Don't remove edges");
		
		JCheckBox labelBox = new JCheckBox();
		labelBox.setSelected(isLabeled());
		labelBox.setText("Labeled costs");
		

		// box.setText("Take edge weights into account");
		// box.setSelected(this.isWeightedEdges());
		// JSlider edgeSlide = new JSlider(0, 100);
		// edgeSlide.setValue((int) aggressiveness);
		// edgeSlide.setPaintLabels(true);
		// edgeSlide.setMajorTickSpacing(25);
		// edgeSlide.setMinorTickSpacing(5);
		// edgeSlide.setPaintTicks(true);
		JSpinner devSpin = new JSpinner(new SpinnerNumberModel(getDeviation(),
				0.0, 1.0, 0.01));
		JSpinner kSpin = new JSpinner(new SpinnerNumberModel(getK(),
				1.0, 10.0, 0.10));

		Object[] params = { "Settings for compression",

		// box,
				autobox,labelBox,devSpin, "Max deviation",
				kSpin,"Correction cost multiplier"
				
				};

		int n = JOptionPane.showConfirmDialog(parent, params,
				"Greedy compression", JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			// setWeightedEdges(box.isSelected());
			// aggressiveness = edgeSlide.getValue();
			setDeviation((Double) devSpin.getValue());
			useAuto = autobox.isSelected();
			setLabeled(labelBox.isSelected());
			setK((Double)kSpin.getValue());
			return true;
		}
		return false;
	}
	
//	public static class CompressionResult{
//		SimpleVisualGraph graph;
//		ArrayList<ArrayList<VisualNode>> groups;
//	}
	
}
