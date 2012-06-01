package biomine.bmvis2.color;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JPanel;

import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.VisualGraph.Change;
import biomine.bmvis2.graphcontrols.NodeGrader;

public class NodeGraderColoring implements NodeColoring {

	Color getGradientColor(double x) {

		double c1[] = { 0.4, 0, 0.7 };
		double c2[] = { 1, 1, 0 };
		double cret[] = { c1[0] * (1 - x) + c2[0] * x,
				c1[1] * (1 - x) + c2[1] * x, c1[2] * (1 - x) + c2[2] * x, };

		return new Color((float) cret[0], (float) cret[1], (float) cret[2]);

	}

	public NodeGraderColoring(NodeGrader gr) {
		grader = gr;
	}

	private NodeGrader grader;

	private VisualGraph currentGraph;
	private long lastVersion = 0;
	private HashMap<VisualNode, Color> cachedColors = new HashMap<VisualNode, Color>();
	double maxGrade;
	public void init(VisualGraph vg) {

		HashMap<VisualNode, Double> grades = new HashMap<VisualNode, Double>();

		maxGrade = 0;
		for (VisualNode n : vg.getAllNodes()) {
			double g = grader.getNodeGoodness(n);
			grades.put(n, g);
			maxGrade = Math.max(g, maxGrade);
		}
		if(maxGrade>0){
			for (Entry<VisualNode, Double> ent : grades.entrySet()) {
				cachedColors.put(ent.getKey(), getGradientColor(ent.getValue()
						/ maxGrade));
			}
		}else{
			for (Entry<VisualNode, Double> ent : grades.entrySet()) {
				cachedColors.put(ent.getKey(), getGradientColor(0.8));
			}
		}
		currentGraph = vg;
		lastVersion = vg.getCombinedVersion();
		legend.repaint();
	}

	public Color getFillColor(VisualNode n) {
		
		if (n.getGraph() != currentGraph
		||  n.getGraph().getCombinedVersion()!=lastVersion){
			init(n.getGraph());
		}
		return cachedColors.get(n);
	}

	class ColorLegend extends JPanel{
		@Override
		public Dimension getMaximumSize(){
			return new Dimension(Short.MAX_VALUE, 20);
			
		}
		public Dimension getPreferredSize(){
			return new Dimension(Short.MAX_VALUE, 20);
			
		}
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			Paint grad = new GradientPaint(0, 5, getGradientColor(0),
					getWidth(), 5, getGradientColor(1),false);
			
			
			
			g2d.setPaint(grad);
			g2d.fillRect(0,0, getWidth(),getHeight());
			g2d.setColor(getGradientColor(1));
			g2d.drawString("0", 5, getHeight()-2);
			g2d.setColor(getGradientColor(0));
			g2d.drawString(""+maxGrade, getWidth()-20, getHeight()-2);
		}
	}
	private ColorLegend legend = new ColorLegend();
	@Override
	public JComponent colorLegendComponent() {
		return legend;
	}

    public String getByName() {
        return "by " + this.grader.getReadableAttribute();
    }

}
