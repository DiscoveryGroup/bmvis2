package biomine.bmvis2.layout;

import java.util.HashSet;

import biomine.bmvis2.LayoutItem;

public abstract class LayoutManager {
	
	private boolean active;
	
	
	//abstract public void posChanged(LayoutItem item);
	abstract public void update();
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isActive() {
		return active;
	}
	
}
