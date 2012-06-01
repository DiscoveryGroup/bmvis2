package biomine.bmvis2.ui;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

public class VerticalScrollablePanel extends JPanel implements Scrollable{

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		return 5;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		return 10;
	}

}
