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

package biomine.bmvis2.group;

import java.awt.Component;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.Logging;
import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualGroupNodeAutoEdges;
import biomine.bmvis2.SimpleVisualGraph.SimpleEdge;
import biomine.bmvis2.pipeline.SettingsChangeCallback;

final class OPair<A extends Comparable<A>> {
	public OPair(A aa, A bb) {
		x = aa;
		y = bb;
		if (x.compareTo(y) < 0) {
			A t = x;
			x = y;
			y = t;
		}
	}

	private A x;

	public A getX() {
		return x;
	}

	public A getY() {
		return y;
	}

	private A y;

	public int hashCode() {
		return x.hashCode() + Integer.rotateLeft(y.hashCode(), 14);
	}

	public boolean equals(Object o) {
		if (o instanceof OPair<?>) {
			return x.equals(((OPair) o).x) && y.equals(((OPair) o).y);
		}
		return false;
	}
}
/**
 * This Grouper implements the traditional Kernighan-Lin partitioning.  See
 * http://en.wikipedia.org/wiki/kernighan-Lin_Algorithm
*/
public class KernighanLin extends Grouper {
	public static String NAME = "Kernighan-Lin partitioning";
	public static String SIMPLE_NAME = "By partition (Kernighan-Lin)";
	
	private void d(Object o){
		Logging.debug("grouping", this.getClass().getSimpleName() + ":" + o);
	}
	private boolean subdivide;

    /*
	1 function Kernighan-Lin(G(V,E)):
	2 determine a balanced initial partition of the nodes into sets A and B
	3 do
	4 A1 := A; B1 := B
	5 compute D values for all a in A1 and b in B1
	6 for (i := 1 to |V|/2)
	7 find a[i] from A1 and b[i] from B1, such that g[i] = D[a[i]] + D[b[i]]
	- 2*c[a[i]][b[i]] is maximal
	8 move a[i] to B1 and b[i] to A1
	9 remove a[i] and b[i] from further consideration in this pass
	10 update D values for the elements of A1 = A1 / a[i] and B1 = B1 / b[i]
	11 end for
	12 find k which maximizes g_max, the sum of g[1],...,g[k]
	13 if (g_max > 0) then
	14 Exchange a[1],a[2],...,a[k] with b[1],b[2],...,b[k]
	15 until (g_max <= 0)
	16 return G(V,E)
	*/

	public String makeGroups(VisualGroupNode groups) {
		SimpleVisualGraph graph = new SimpleVisualGraph(groups);

		HashSet<Integer> a, b;
		a = new HashSet<Integer>();
		b = new HashSet<Integer>();

		HashSet<Integer>[] ab = new HashSet[] { a, b };
		int partition[] = new int[graph.n];
		for (int i = 0; i < graph.n; i++) {
			partition[i] = i % 2;
			ab[i % 2].add(i);
		}
		assert (Math.abs(ab[0].size() - ab[1].size()) <= 1);

		class MyMap extends HashMap<OPair<Integer>, Double> {
			public double get(int a, int b) {
				Double ret = super.get(new OPair<Integer>(a, b));
				if (ret == null)
					return 0.0;
				return ret;
			}
		}
		MyMap edgeweight = new MyMap();
		for (int i = 0; i < graph.n; i++) {
			for (SimpleEdge e : graph.edges[i]) {
				edgeweight.put(new OPair<Integer>(i, e.to), edgeweight.get(i,
						e.to)
						+ e.weight);
			}
		}

		do {
			double icost[] = new double[graph.n];
			double ecost[] = new double[graph.n];
			double g[] = new double[graph.n];

			for (int i = 0; i < graph.n; i++) {
				int part = partition[i];
				icost[i] = ecost[i] = 0;
				for (SimpleEdge e : graph.edges[i]) {
					if (partition[e.to] == part) {
						// same partition (internal costs)
						icost[i] += e.weight;
					} else {
						ecost[i] += e.weight;
					}
				}
			}

			double[] d = new double[graph.n];
			for (int i = 0; i < graph.n; i++)
				d[i] = ecost[i] - icost[i];
			int[][] change = new int[graph.n / 2][2];
			int changeCount = 0;
			HashSet<Integer> used = new HashSet<Integer>();
			for (int j = 0; j < graph.n / 2; j++) {
				double maxg = Integer.MIN_VALUE;
				int bai, bbi;
				bai = bbi = -1;
				for (int ai : ab[0]) {
					if (used.contains(ai))
						continue;
					for (int bi : ab[1]) {
						if (used.contains(bi))
							continue;
						double mg = d[ai] + d[bi] - edgeweight.get(ai, bi);
						if (mg > maxg) {
							bai = ai;
							bbi = bi;
							maxg = mg;
						}
					}
				}
				if (bbi == bai && bai == -1)
					break;

				change[j][0] = bai;
				change[j][1] = bbi;
				changeCount++;
				for (int k = 0; k < 2; k++) {
					int i = change[j][k];
					for (SimpleEdge e : graph.edges[i]) {
						if (partition[e.to] == partition[i]) {
							// same partition (move to ecost)
							icost[e.to] -= e.weight;
							ecost[e.to] += e.weight;
						} else {
							ecost[e.to] -= e.weight;
							icost[e.to] += e.weight;
						}
					}
				}
				for (int i = 0; i < graph.n; i++)
					d[i] = ecost[i] - icost[i];

				g[j] = maxg;
				partition[bai] = 1;
				partition[bbi] = 0;
				ab[0].remove(bai);
				ab[1].remove(bbi);
				ab[0].add(bbi);
				ab[1].add(bai);
				used.add(bai);
				used.add(bbi);
				d("bai = " + bai + " bbi = " + bbi + " g  = "
						+ g[j]);
				assert (Math.abs(ab[0].size() - ab[1].size()) <= 1);
			}
			int k = -1;
			double gmax = Integer.MIN_VALUE;
			double gcur = 0.0;
			for (int i = 0; i < changeCount; i++) {
				gcur += g[i];
				if (gcur > gmax) {
					gmax = gcur;
					k = i;
				}
			}
			d("gmax = " + gmax + " k = " + k + " / " + graph.n);
			for (int j = 0; j < 2; j++) {
				for (int i : ab[j]) {
					d(i+" ");
				}
				d("");
			}
			if (gmax > 0.01) {
				// reverse changes starting from k+1

				for (int i = k + 1; i < changeCount; i++) {
					for (int j = 0; j < 2; j++) {
						int bi = change[i][j];
						partition[bi] = j;
						ab[1 ^ j].remove(bi);
						ab[j].add(bi);
					}
				}
				assert (Math.abs(ab[0].size() - ab[1].size()) <= 1);
			} else {
				break;
			}
		} while (true);
		
		
		{
			double icost[] = new double[graph.n];
			double ecost[] = new double[graph.n];
			
			for (int i = 0; i < graph.n; i++) {
				int part = partition[i];
				icost[i] = ecost[i] = 0;
				for (SimpleEdge e : graph.edges[i]) {
					if (partition[e.to] == part) {
						// same partition (internal costs)
						icost[i] += e.weight;
					} else {
						ecost[i] += e.weight;
					}
				}
			}
			int k = graph.n / 3;
			Integer[] ind = new Integer[graph.n];

			final double[] d = new double[graph.n];
			for (int i = 0; i < graph.n; i++) {
				d[i] = ecost[i] - icost[i];
				ind[i] = i;
			}
			Arrays.sort(ind, new Comparator<Integer>() {
				public int compare(Integer o1, Integer o2) {
					// TODO Auto-generated method stub
					return Double.compare(d[o2], d[o1]);
				}
			});

			for (int i = 0; i < k; i++) {

				int bi = ind[i];
				if (d[bi] <= 0) {
					d("postfixed " + i + " nodes (max was "
							+ k + ")");
					break;
				}
				int j = partition[bi];
				partition[bi] = j ^ i;
				ab[j].remove(bi);
				ab[1 ^ j].add(bi);
			}

		}

		VisualGroupNodeAutoEdges nodeA = new VisualGroupNodeAutoEdges(groups);
		VisualGroupNodeAutoEdges nodeB = new VisualGroupNodeAutoEdges(groups);

		for (int ai : ab[0])
			graph.getVisualNode(ai).setParent(nodeA);
		for (int bi : ab[1])
			graph.getVisualNode(bi).setParent(nodeB);
		if(subdivide ){
			if(ab[0].size()>10)
				makeGroups(nodeA);

			if(ab[1].size()>10)
				makeGroups(nodeB);
		}
		return null;
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

    public JComponent getSettingsComponent(final SettingsChangeCallback changeCallback, final VisualGroupNode groupNode) {
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

    public String getByName() {
        return "partitioning";
    }

}
