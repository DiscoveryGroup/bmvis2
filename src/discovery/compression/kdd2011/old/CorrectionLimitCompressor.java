package discovery.compression.kdd2011.old;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGroupNode;
import discovery.compression.kdd2011.old.CompressedGraph.Reduction;

public class CorrectionLimitCompressor extends Compressor {

	CompressedGraph lastCGraph;
	CompressorCosts lastCosts;
	CompressorCosts startCosts;

	public CompressorCosts getResultCosts() {
		return lastCosts;
	}

	@Override
	public CompressorCosts getStartingCosts() {
		return startCosts;
	}

	@Override
	public SimpleVisualGraph getUncompressedGraph() {
		return lastCGraph.uncompressedGraph();
	}

	@Override
	public SimpleVisualGraph getOriginalGraph() {
		return lastCGraph.getSimpleGraph();
	}

	private int correctionLimit = 0;

	private int compressABit(CompressedGraph cgraph, int maxerrs, double thld) {

		int aliveCount = cgraph.size();
		int[] alive = new int[aliveCount];
		for (int i = 0; i < aliveCount; i++)
			alive[i] = i;
		// do it the randomized way
		Random rand = new Random();
		int unsuccess = 0;

		int e = 0;
		while (e <= maxerrs && aliveCount > 1) {
			final int M = 35;
			double bestScore = -Double.MAX_VALUE;
			int ba = -1;
			int bb = 0;
			int bc = 0;
			ba = alive[rand.nextInt(aliveCount)];
			ArrayList<Integer> ns = new ArrayList<Integer>();

			ns.addAll(cgraph.hopNeighbors(ba, 2));
			ns.addAll(cgraph.hopNeighbors(ba, 1));

			for (int b : ns) {
				Reduction red = cgraph.reduction(ba, b);
				double score = red.s / red.newCorrections;// inf?
				if (score > bestScore && e + red.newCorrections <= maxerrs) {
					bb = b;
					bc = red.newCorrections;
					bestScore = score;
				}
			}

			if (bestScore > thld ) {
				cgraph.merge(ba, bb);
				aliveCount--;
				alive[bb] = alive[aliveCount];
				e += bc;
				unsuccess = 0;
			} else {
				aliveCount--;
				alive[ba] = alive[aliveCount];
			}

			// System.out.println("e = " + e + " corrCount = "
			// + cgraph.countCorrections());
		}
		return e;
	}

	@Override
	public String makeGroups(VisualGroupNode group) {
		setK(0);// remove corrections from costs
		CompressedGraph cgraph = this.createCompressedGraph(group);
		lastCGraph = cgraph;
		lastCGraph = cgraph;
		startCosts = cgraph.getCosts();
		if (dblLimit >= 0) {
			correctionLimit = (int) (cgraph.getSimpleGraph().getEdgeCount() * dblLimit);
		}
		// System.out.println("limit is " + correctionLimit);

		//double[] steps = { 10, 5, 2.5, 1, 0.5, 0.25, 0.125, 0 };
		double[] steps = {8,4,2,1,0.5,0.25,0};

		int e = 0;

		for (double thld : steps) {
			// int currentLimit = (int)(i*0.1*correctionLimit);
			// int ne =
			// compressABit(cgraph,currentLimit-e,0);//Math.pow(2,-i)*10);
			int ne = compressABit(cgraph, correctionLimit - e, thld);
			e += ne;
			if (e == correctionLimit)
				break;
		}

		cgraph.performGrouping(useAuto());
		lastCosts = cgraph.getCosts();
		return null;
	}

	private double dblLimit = -1;

	public void setLimit(double eps) {
		dblLimit = eps;
		System.out.println("dblLimit = " + dblLimit);
		assert (eps < 1);
	}

	@Override
	public boolean settingsDialog(Component parent, VisualGroupNode group) {
		// TODO Auto-generated method stub
		// JCheckBox box = new JCheckBox();
		JCheckBox autobox = new JCheckBox();
		autobox.setSelected(useAuto());
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
		JSpinner eSpin = new JSpinner(new SpinnerNumberModel(dblLimit, 0.0,
				1.0, 0.10));

		Object[] params = { "Settings for greedy compression",
				// box,
				autobox, labelBox, devSpin, "Max deviation", eSpin,
				"Maximum error"

		};

		int n = JOptionPane.showConfirmDialog(parent, params,
				"Correction limit compression", JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			// setWeightedEdges(box.isSelected());
			// aggressiveness = edgeSlide.getValue();
			setDeviation((Double) devSpin.getValue());
			setUseAuto(autobox.isSelected());
			setLabeled(labelBox.isSelected());
			setLimit((Double) eSpin.getValue());
			return true;
		}
		return false;
	}
}
