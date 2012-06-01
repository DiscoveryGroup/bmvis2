package biomine.bmvis2;

import java.util.Collection;

/**
 * Interface to observe changes in graphs.
 * @author alhartik
 *
 */
public interface GraphObserver {
	public void graphStructureChanged(VisualGraph g);
	public void pointsOfInterestsChanged(VisualGraph g);
	public void visibleNodesChanged(VisualGraph g);
	public void selectionChanged(VisualGraph g);
	public void colorsChanged(VisualGraph g);
    public void zoomRequested(VisualGraph g, Collection<LayoutItem> items);
}
