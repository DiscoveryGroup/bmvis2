package biomine.bmvis2.layout;

import java.util.ArrayList;

import biomine.bmvis2.Vec2;

class StressEdge {
	int to;
	double d;
}

/**
 * Implements Distance scaling using stress minimization
 * TODO: fast implementation for large graphs (use pivot-MDS)
 * @author Aleksi Hartikainen
 */
public class StressMinimizer {
	private boolean planarFit = false;

	public StressMinimizer(Matrix edges, Vec2[] startPositions) {
		Matrix sp = new Matrix(LayoutUtils.shortestPaths(edges.getArray()));

		p = ClassicMDS.solve(sp, startPositions);
		double error = 0;

		for (int i = 0; i < p.length; i++) {
			p[i] = p[i].plus(0.001 * Math.random(), 0.001 * Math.random());
		}
		p2 = new Vec2[p.length];
		edgeMatrix = sp;
		setEdges();
	}

	public StressMinimizer(Matrix edges) {
		Matrix sp = new Matrix(LayoutUtils.shortestPaths(edges.getArray()));
		p = ClassicMDS.solve(sp);
		for (int i = 0; i < p.length; i++) {
			p[i] = p[i].plus(0.001 * Math.random(), 0.001 * Math.random());
		}
		p2 = new Vec2[p.length];
		edgeMatrix = sp;
		setEdges();
	}

	private double weight(int i, int j) {
		double d = dist(i, j);
		return 1 / (d * d);
	}

	private double dist(int i, int j) {
		return edgeMatrix.get(i, j);
	}

	private void setEdges() {
		edges = new StressEdge[edgeMatrix.cols()][];
		for (int i = 0; i < edgeMatrix.cols(); i++) {
			ArrayList<StressEdge> ed = new ArrayList<StressEdge>();
			for (int j = 0; j < edgeMatrix.cols(); j++) {
				if (i == j)
					continue;
				if (edgeMatrix.get(i, j) != 0.0) {
					StressEdge e = new StressEdge();
					e.to = j;
					e.d = edgeMatrix.get(i, j);
					ed.add(e);

				}
			}
			edges[i] = new StressEdge[ed.size()];
			edges[i] = ed.toArray(edges[i]);

		}

	}

	private Matrix edgeMatrix;

	private StressEdge[][] edges;
	private Vec2[] p;

	private Vec2[] p2;

	public Vec2[] getPositions() {
		return p;
	}

	private double s(int i, int j) {
		if (p[i].minus(p[j]).length2() < 0.000001)
			return 0;
		return dist(i, j) / (p[i].dist(p[j]));
	}

	double enhance() {
		if (planarFit)
			return 0;
		int n = p.length;
		assert n == edges.length;

		for (int i = 0; i < n; i++) {
			// Vec2 x = new Vec2(0, 0);
			double xx, xy;
			xx = xy = 0;
			double wsum = 0;
			for (int k = 0; k < edges[i].length; k++) {
				int j = edges[i][k].to;
				// for(int j=0;j<n;j++){
				if (j == i)
					continue;
				double w = weight(i, j);
				wsum += w;
				double ss = s(i, j);

				xx += w * (p[j].x + (p[i].x - p[j].x) * ss);
				xy += w * (p[j].y + (p[i].y - p[j].y) * ss);

				/*
				 * x.add(p[j].plus( p[i].minus(p[j]).scaled(s(i,j)) )
				 * .scaled(weight(i,j)) );
				 */
			}
			// x = x.scaled(1 / wsum);
			xx /= wsum;
			xy /= wsum;
			p2[i] = new Vec2(xx, xy);
		}
		double diff = 0;
		for (int i = 0; i < n; i++) {
			diff += p[i].minus(p2[i]).length();
			p[i] = p2[i];
		}
		// GraphUtils.normalize(p);
		return diff;
	}

	public void iterate(double epsilon) {
		if (planarFit)
			return;
		int it = 0;
		// long time = System.currentTimeMillis();

		while (enhance() > epsilon)
			it++;

		// double t = (System.currentTimeMillis() - time) * 0.001;
		// System.out.println(it + " iterations of stress minimization needed");
		// System.out.println("took " + t + "seconds\nit/s=" + it / t);
	}

	public void iterate() {
		iterate(0.005);
	}
}
