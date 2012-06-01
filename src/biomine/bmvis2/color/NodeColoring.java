package biomine.bmvis2.color;

import java.awt.Color;

import javax.swing.JComponent;

import biomine.bmvis2.VisualNode;

public interface NodeColoring {
    public String getByName();
	
	public Color getFillColor(VisualNode n);
	
	public JComponent colorLegendComponent();
	
}
